package com.paduvi.serviceworker;

import com.paduvi.model.HealthJob;
import com.paduvi.model.Service;

public interface NotificationService {

	public boolean sendNotification(Service service, HealthJob job);
}
