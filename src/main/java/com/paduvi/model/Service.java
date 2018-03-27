package com.paduvi.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paduvi.repository.converter.OptionToStringConverter;

@Entity
@Table(name = "serviceTbl")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Service implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6402825509218441298L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "name", unique = true)
	private String name;

	@Column(name = "pingUrl", nullable = false)
	private String pingUrl;

	@Column(name = "healthy")
	private boolean healthy = true;

	@Column(name = "watching")
	private boolean watching = true;

	@OneToOne
	@JoinColumn(name = "user", nullable = false)
	private User user;

	@Lob
	@Column(name = "advancedOption")
	@Convert(converter = OptionToStringConverter.class)
	private ServiceAdvancedOption advancedOption = new ServiceAdvancedOption();

	@Column(name = "lastChecked")
	private Long lastChecked;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ServiceAdvancedOption getAdvancedOption() {
		return advancedOption;
	}

	public void setAdvancedOption(ServiceAdvancedOption advancedOption) {
		this.advancedOption = advancedOption;
	}

	public boolean isWatching() {
		return watching;
	}

	public void setWatching(boolean watching) {
		this.watching = watching;
	}

	public boolean isHealthy() {
		return healthy;
	}

	public void setHealthy(boolean healthy) {
		this.healthy = healthy;
	}

	public String getPingUrl() {
		return pingUrl;
	}

	public void setPingUrl(String pingUrl) {
		this.pingUrl = pingUrl;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getLastChecked() {
		return lastChecked;
	}

	public void setLastChecked(Long lastChecked) {
		this.lastChecked = lastChecked;
	}
}
