package com.paduvi.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "userTbl")
@DynamicUpdate
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 170074702522072058L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "name", unique = true, updatable=false)
	private String name;

	@Column(name = "tel")
	private String tel;

	@Column(name = "mail")
	private String mail;

	@Column(name = "onNotify")
	private boolean onNotify = true;

	public User() {
	}

	public User(String name, String tel, String mail) {
		this.name = name;
		this.tel = tel;
		this.mail = mail;
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

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public boolean isOnNotify() {
		return onNotify;
	}

	public void setOnNotify(boolean onNotify) {
		this.onNotify = onNotify;
	}

	@Override
	public String toString() {
		return String.format("User[id=%d, name='%s', tel='%s', mail='%s']", id, name, tel, mail);
	}
}
