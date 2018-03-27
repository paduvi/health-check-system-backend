package com.paduvi.repository.converter;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paduvi.model.ServiceAdvancedOption;

@Converter
public class OptionToStringConverter implements AttributeConverter<ServiceAdvancedOption, String> {

	ObjectMapper mapper = new ObjectMapper();

	public String convertToDatabaseColumn(ServiceAdvancedOption data) {
		String value = "";
		try {
			value = mapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return value;
	}

	public ServiceAdvancedOption convertToEntityAttribute(String data) {
		ObjectMapper mapper = new ObjectMapper();
		ServiceAdvancedOption obj;
		try {
			obj = mapper.readValue(data, ServiceAdvancedOption.class);
		} catch (IOException e) {
			e.printStackTrace();
			obj = new ServiceAdvancedOption();
		}
		return obj;
	}

}