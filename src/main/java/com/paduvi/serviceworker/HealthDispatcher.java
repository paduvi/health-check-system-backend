package com.paduvi.serviceworker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import javax.annotation.PostConstruct;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.paduvi.config.Constant;
import com.paduvi.model.HealthJob;
import com.paduvi.model.Service;
import com.paduvi.repository.HealthJobRepository;
import com.paduvi.repository.ServiceRepository;

@Component
public class HealthDispatcher {

	@Autowired
	ServiceRepository serviceRepository;

	@Autowired
	HealthJobRepository healthJobRepository;

	private ArrayBlockingQueue<HealthJob> queue = new ArrayBlockingQueue<>(Constant.MAX_SIZE_QUEUE);
	private HealthCheckWorker[] workerPool = new HealthCheckWorker[Constant.MAX_WORKER];
	private PoolingHttpClientConnectionManager httpConnectionPool = new PoolingHttpClientConnectionManager();

	private List<NotificationService> notificationManager = new ArrayList<>();

	@Autowired
	private MailNotificationService mailNotificationService;

	@Autowired
	private SmsNotificationService smsNotificationService;

	@PostConstruct
	public void init() {
		httpConnectionPool.setMaxTotal(Constant.MAX_WORKER);
		for (int i = 0; i < Constant.MAX_WORKER; i++) {
			workerPool[i] = new HealthCheckWorker();
			workerPool[i].start();
		}

		Iterable<Service> services = serviceRepository.findAll();
		for (Service service : services) {
			insertJobToQueue(new HealthJob(service), 0);
		}

		notificationManager.add(smsNotificationService);
		notificationManager.add(mailNotificationService);
	}

	public synchronized void insertJobToQueue(HealthJob job, long delay) {
		if (delay == 0) {
			try {
				queue.put(job);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return;
		}
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(delay);
					queue.put(job);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		// t.setDaemon(true);
		t.start();
	}

	public void sendNotification(Service service, HealthJob job) {
		for (NotificationService notifyService : notificationManager) {
			notifyService.sendNotification(service, job);
		}
	}

	class HealthCheckWorker extends Thread {

		@Override
		public void run() {
			while (true) {
				try {
					HealthJob job = queue.take();

					Service service = null;
					try {
						service = checkService(job);
					} catch (Exception e) {
						e.printStackTrace();

						service = serviceRepository.findById(job.getServiceId()).orElse(null);
						if (service == null) {
							job.setMessage("Service is removed!");
							healthJobRepository.save(job);
							continue;
						}

						long duration = Math.min(
								Constant.DEFAULT_RETRY_DURATION * (1 + Constant.MAX_RETRY - job.getRetry()),
								service.getAdvancedOption().getPollDurationInSeconds() * 1000);
						insertJobToQueue(job, duration);
					}

					if (!service.isWatching()) {
						insertJobToQueue(new HealthJob(service),
								service.getAdvancedOption().getPollDurationInSeconds() * 1000);
						continue;
					}

					if (job.isDifferent()) {
						healthJobRepository.save(job);
						sendNotification(service, job);
					}
					
					if (job.getRetry() == 0) {
						insertJobToQueue(new HealthJob(service),
								service.getAdvancedOption().getPollDurationInSeconds() * 1000);
						continue;
					}
					long duration = Math.min(
							Constant.DEFAULT_RETRY_DURATION * (1 + Constant.MAX_RETRY - job.getRetry()),
							service.getAdvancedOption().getPollDurationInSeconds() * 1000);
					insertJobToQueue(job, duration);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = TransactionException.class)
		private Service checkService(HealthJob job) throws Exception {
			job.setExecutedAt(new Date().getTime());

			Service service = serviceRepository.findById(job.getServiceId()).orElse(null);
			if (service == null) {
				throw new Exception("Service not found: " + job.getServiceId());
			}

			if (!service.isWatching()) {
				return service;
			}
			service.setLastChecked(job.getExecutedAt());

			CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(httpConnectionPool).build();
			HttpGet httpGet = new HttpGet(service.getPingUrl());
			CloseableHttpResponse httpResponse = null;
			try {
				httpResponse = httpClient.execute(httpGet);

				boolean healthy = false;

				String content = "(Empty entity)";

				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					healthy = true;
				}

				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					content = EntityUtils.toString(entity);
					EntityUtils.consume(entity);
				}

				if (healthy) {
					healthy = checkJsonFormat(content, service.getAdvancedOption().getPayloadSchema());
					if (healthy) {
						job.setMessage("Service is running");
					} else {
						job.setMessage("Invalid response format");
					}
				} else {
					job.setMessage("Service has stopped. Message: " + content);
				}

				job.setHealthy(healthy);
				job.setRetry(0);
				if (service.isHealthy() != healthy) {
					job.setDifferent(true);
					service.setHealthy(healthy);
				}
			} catch (HttpHostConnectException e) {
				job.setMessage(e.getMessage());
				job.setRetry(0);
				job.setHealthy(false);
				if (service.isHealthy()) {
					job.setDifferent(true);
					service.setHealthy(false);
				}
			} catch (IOException e) {
				e.printStackTrace();
				job.setMessage(e.getMessage());

				job.decreaseRetry();

				if (job.getRetry() == 0) {
					job.setHealthy(false);
					if (service.isHealthy()) {
						job.setDifferent(true);
						service.setHealthy(false);
					}
				}
			} finally {
				if (httpResponse != null) {
					try {
						httpResponse.close();
					} catch (IOException e) {
					}
				}
			}

			serviceRepository.save(service);

			return service;
		}

		private boolean checkJsonFormat(String content, String schemaString) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
			try {
				JsonNode schemaNode = mapper.readTree(schemaString);
				JsonNode contentNode = mapper.readTree(content);

				JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

				JsonSchema schema = factory.getJsonSchema(schemaNode);
				return schema.validate(contentNode).isSuccess();
			} catch (IOException | ProcessingException e) {
				return false;
			}
		}

	}
}
