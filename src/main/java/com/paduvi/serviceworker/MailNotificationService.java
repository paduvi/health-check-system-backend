package com.paduvi.serviceworker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.paduvi.model.HealthJob;
import com.paduvi.model.Service;

@Component
public class MailNotificationService implements NotificationService {

	@Value("${mailer.url}")
	private String MAILER_URL;

	@Override
	public boolean sendNotification(Service service, HealthJob job) {
		try {
			String htmlText = "";
			htmlText += "<h3>Service: " + service.getName() + "</h3>";
			htmlText += "<p>URL: " + service.getPingUrl() + "</p>";
			htmlText += "<p>Status: " + (service.isHealthy() ? "OK" : "Stopped") + "</p>";
			htmlText += "<p>Message: " + job.getMessage() + "</p>";

			sendEmail(service.getUser().getMail(), "NEWS ENGINE NOTIFICATION", htmlText);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void sendEmail(String toEmail, String subject, String body) throws Exception {
		String error = null;

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost(MAILER_URL);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("to", toEmail));
			nvps.add(new BasicNameValuePair("subject", subject));
			nvps.add(new BasicNameValuePair("html", body));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			CloseableHttpResponse response = httpclient.execute(httpPost);

			System.out.println("\n" + response.getStatusLine());
			HttpEntity entity = response.getEntity();

			if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
				String content = EntityUtils.toString(entity);
				System.out.println(content + "\n");
			} else {
				error = EntityUtils.toString(entity);
				System.out.println(error + "\n");
			}
			EntityUtils.consume(entity);

			response.close();
		} catch (IOException e) {
			throw e;
		}

		if (error != null) {
			throw new Exception(error);
		}
	}

}
