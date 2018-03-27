package com.paduvi.serviceworker;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.paduvi.model.HealthJob;
import com.paduvi.model.Service;

@Component
public class MailNotificationService implements NotificationService {

	@Autowired
	private JavaMailSender sender;

	@Override
	public boolean sendNotification(Service service, HealthJob job) {
		try {
			String htmlText = "";
			htmlText += "<h3>Service: " + service.getName() + "</h3>";
			htmlText += "<p>URL: " + service.getPingUrl() + "</p>";
			htmlText += "<p>Status: " + (service.isHealthy() ? "OK" : "Stopped") + "</p>";
			htmlText += "<p>Message: " + job.getMessage() + "</p>";

			sendEmail(service.getUser().getMail(), "Notify service status", htmlText);
			return true;
		} catch (MessagingException | NoSuchMethodError e) {
			e.printStackTrace();
			return false;
		}
	}

	public void sendEmail(String toEmail, String subject, String body) throws MessagingException, NoSuchMethodError {
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		helper.setTo(toEmail);
		helper.setText(body, true);
		helper.setSubject(subject);
		System.out.println("Message is ready");
		
		// may throw NoSuchMethodError
		// if not have SMTP Transport jar in classpath
		sender.send(message);
		System.out.println("EMail Sent Successfully!!");
	}

}
