package com.paduvi.model;

import java.io.Serializable;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotFoundException extends RuntimeException implements Serializable, GraphQLError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6906432039295016303L;

	public NotFoundException() {
		super("Not found element");
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(Throwable ex) {
		super(ex);
	}

	@Override
	public ErrorType getErrorType() {
		return ErrorType.DataFetchingException;
	}

	@Override
	public List<SourceLocation> getLocations() {
		return null;
	}

}