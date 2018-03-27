package com.paduvi.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paduvi.config.Constant;

@Entity
@Table(name = "healthJobTbl")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthJob implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6551164112084721733L;

	@Id
	@GeneratedValue
	private UUID id;

	@Column(name = "serviceId")
	private Long serviceId;

	@Column(name = "healthy")
	private boolean healthy;

	@Transient
	private boolean different = false;

	@Column(name = "createdAt")
	private Long createdAt;

	@Column(name = "executedAt")
	private Long executedAt;

	@Column(name = "message")
	private String message;

	@Transient
	private int retry = Constant.MAX_RETRY;

	protected HealthJob() {

	}

	public HealthJob(Service service) {
		this.serviceId = service.getId();
		this.setCreatedAt(new Date().getTime());
	}

	public void decreaseRetry() {
		this.retry--;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isDifferent() {
		return different;
	}

	public void setDifferent(boolean different) {
		this.different = different;
	}

	public Long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Long createdAt) {
		this.createdAt = createdAt;
	}

	public Long getExecutedAt() {
		return executedAt;
	}

	public void setExecutedAt(Long executedAt) {
		this.executedAt = executedAt;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public boolean isHealthy() {
		return healthy;
	}

	public void setHealthy(boolean healthy) {
		this.healthy = healthy;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

}
