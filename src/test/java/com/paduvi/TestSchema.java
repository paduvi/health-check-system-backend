package com.paduvi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class TestSchema {

	public static void main(String[] args) {
		try {
			String schemaString = FileUtils.readFileToString(
					new File(TestSchema.class.getResource("/default-schema.json").getFile()),
					StandardCharsets.UTF_8.name());
			
			String goodJson = FileUtils.readFileToString(
					new File(TestSchema.class.getResource("/good.json").getFile()),
					StandardCharsets.UTF_8.name());
			
			String badJson = FileUtils.readFileToString(
					new File(TestSchema.class.getResource("/bad.json").getFile()),
					StandardCharsets.UTF_8.name());
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
			JsonNode schemaNode = mapper.readTree(schemaString);
			JsonNode goodNode = mapper.readTree(goodJson);
			JsonNode badNode = mapper.readTree(badJson);

			JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

			JsonSchema schema = factory.getJsonSchema(schemaNode);
			
			ProcessingReport report = schema.validate(goodNode);
			System.out.println(report);
			
			report = schema.validate(badNode);
			System.out.println(report);

		} catch (ProcessingException | IOException e) {
			e.printStackTrace();
		}
	}
}
