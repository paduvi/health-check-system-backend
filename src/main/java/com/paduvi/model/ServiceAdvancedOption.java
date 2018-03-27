package com.paduvi.model;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.paduvi.config.Constant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceAdvancedOption implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2775711223700683971L;

	private boolean autoRestart = false;

	private String activeUrl = "";

	private int pollDurationInSeconds = Constant.DEFAULT_POLL_DURATION;

	private String payloadSchema;

	public ServiceAdvancedOption() {
		ClassPathResource classPathResource = new ClassPathResource("default-schema.json");

		try {
			byte[] bdata = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
			String defaultSchema = new String(bdata, StandardCharsets.UTF_8);
			setPayloadSchema(defaultSchema);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isAutoRestart() {
		return autoRestart;
	}

	public void setAutoRestart(boolean autoRestart) {
		this.autoRestart = autoRestart;
	}

	public String getActiveUrl() {
		return activeUrl;
	}

	public void setActiveUrl(String activeUrl) {
		this.activeUrl = activeUrl;
	}

	public int getPollDurationInSeconds() {
		return pollDurationInSeconds;
	}

	public void setPollDurationInSeconds(int pollDurationInSeconds) {
		this.pollDurationInSeconds = pollDurationInSeconds;
	}

	public String getPayloadSchema() {
		try {
			return getPayloadSchema(true);
		} catch (IOException e) {
			return payloadSchema;
		}
	}

	public String getPayloadSchema(boolean pretty) throws IOException {
		if (pretty) {
			DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
			DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
			printer.indentObjectsWith(indenter);
			printer.indentArraysWith(indenter);

			ObjectMapper mapper = new ObjectMapper();
			Object json = mapper.readValue(payloadSchema, Object.class);
			return mapper.setDefaultPrettyPrinter(printer).enable(SerializationFeature.INDENT_OUTPUT)
					.writeValueAsString(json);
		}
		return payloadSchema;
	}

	public void setPayloadSchema(String payloadSchema) throws BadRequestException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
			JsonNode schemaNode = mapper.readTree(payloadSchema);

			JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

			factory.getJsonSchema(schemaNode);

			this.payloadSchema = mapper.writeValueAsString(schemaNode);
		} catch (ProcessingException | IOException e) {
			e.printStackTrace();
			throw new BadRequestException("Invalid JSON schema");
		}
	}
}
