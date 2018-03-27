package com.paduvi.model;

import java.io.Serializable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6906432039295016303L;

	public BadRequestException() {
		super("Bad request");
	}

	public BadRequestException(String message) {
		super(message);
	}

	public BadRequestException(Throwable ex) {
		super(ex);
	}

}