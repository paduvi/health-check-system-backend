package com.paduvi.serviceworker;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paduvi.model.HealthJob;
import com.paduvi.model.Service;

@Component
public class SmsNotificationService implements NotificationService {

	@Value("${esms.apikey}")
	private String API_KEY;

	@Value("${esms.secretkey}")
	private String SECRET_KEY;

	@Override
	public boolean sendNotification(Service service, HealthJob job) {
		String message = "Server: " + service.getName();
		message += ". URL: " + service.getPingUrl();
		message += ". Status: " + (service.isHealthy() ? "OK" : "Stopped");
		message += ". Detail: " + job.getMessage();

		try {
			sendSms(service.getUser().getTel(), message);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void sendSms(String phone, String message) throws Exception {
		String url = "http://rest.esms.vn/MainService.svc/json/SendMultipleMessage_V4_get?ApiKey="
				+ URLEncoder.encode(API_KEY, "UTF-8") + "&SecretKey=" + URLEncoder.encode(SECRET_KEY, "UTF-8")
				+ "&SmsType=4&Phone=" + URLEncoder.encode(phone, "UTF-8") + "&Content="
				+ URLEncoder.encode(message, "UTF-8") + "&IsUnicode=1";

		String error = null;

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = httpclient.execute(httpGet);

			System.out.println("\n" + response.getStatusLine());
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				String content = EntityUtils.toString(entity);

				System.out.println(content + "\n");

				ObjectMapper mapper = new ObjectMapper();
				JsonNode jsonNode = mapper.readTree(content);

				int code = jsonNode.get("CodeResult").asInt();
				if (code != 100) {
					switch (code) {
					case 104:
						error = "Brandname khong ton tai";
						break;
					case 118:
						error = "Loai tin nhan khong hop le";
						break;
					case 119:
						error = "Brand quang cao phai gui it nhat 20 so dien thoai";
						break;
					case 131:
						error = "Tin nhan quang cao phai co do dai toi da 142 ky tu";
						break;
					case 132:
						error = "Khong co quyen su dung tin nhan dau so co dinh 8755";
						break;
					case 99:
					default:
						error = "Loi khong xac dinh";
						break;
					}
				}
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
