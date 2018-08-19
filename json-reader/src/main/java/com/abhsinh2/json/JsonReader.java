package com.abhsinh2.json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonReader {

	private static final List<String> IGNORE_LIST = Arrays.asList("__metadata", "__deferred");

	public static void main(String[] args) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(new File(
				"/Users/abhsinh2/Abhishek/Study/workspace-sts-3.9.0.RELEASE/json-reader/src/main/resources/test.json"));

		Collection<String> headers = new LinkedHashSet<>();
		List<List<String>> records = new ArrayList<>();

		readResult(rootNode.get("d").get("results"), null, headers, records, null, 0);
		
		System.out.println("================================");
		System.out.println(headers);
		records.forEach(record -> System.out.println(record));
		
		// Write to csv
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(
				"/Users/abhsinh2/Abhishek/Study/workspace-sts-3.9.0.RELEASE/json-reader/src/main/resources/test.csv"));
		CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

		csvPrinter.printRecord(headers);
		records.forEach(record -> {
			try {
				csvPrinter.printRecord(record);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		/*
		for (List<String> record : records) {
			csvPrinter.printRecord(record);
		}*/

		csvPrinter.flush();
		csvPrinter.close();		
	}

	private static void readResult(JsonNode resultNode, String parentKey, Collection<String> headers, List<List<String>> records, List<String> record, int recordIndex) {		
		if (resultNode.isArray()) {			
			Iterator<JsonNode> resultNodeIter = resultNode.iterator();
			while (resultNodeIter.hasNext()) {
				
				List<String> newRecord = null;
				if (record == null) {
					newRecord = new ArrayList<>();					
				} else {
					newRecord = new ArrayList<>();
					newRecord.addAll(record);
				}
				
				records.add(newRecord);
				
				JsonNode result = resultNodeIter.next();

				if (result.isObject()) {
					Iterator<Map.Entry<String, JsonNode>> entries = result.fields();
					while (entries.hasNext()) {
						Map.Entry<String, JsonNode> entry = entries.next();
						String key = entry.getKey();
						JsonNode value = entry.getValue();

						if (!IGNORE_LIST.contains(key)) {
							if (value.isArray()) {
								readArray(value, result);
							} else if (value.isObject()) {
								if (parentKey == null) {
									readObject(value, key, headers, records, newRecord, recordIndex);
								} else {
									readObject(value, parentKey + "_" + key, headers, records, newRecord, recordIndex);
								}
							} else {
								if (parentKey == null) {
									System.out.println(key + ":" + value);
									newRecord.add(value.asText());
									headers.add(key);
								} else {
									System.out.println(parentKey + "_" + key + ":" + value);
									newRecord.add(value.asText());
									headers.add(parentKey + "_" + key);
								}
							}
						}
					}
				}			
				
				System.out.println("-------------------------------------------");
			}
		}
	}

	private static void readObject(JsonNode obj, String parentKey, Collection<String> headers, List<List<String>> records, List<String> record, int recordIndex) {
		Iterator<Map.Entry<String, JsonNode>> entries = obj.fields();
		while (entries.hasNext()) {
			Map.Entry<String, JsonNode> entry = entries.next();
			String key = entry.getKey();
			JsonNode value = entry.getValue();

			if (key.equals("results")) {
				readResult(value, parentKey + "_" + key, headers, records, record, recordIndex);
			}
		}
	}

	private static void readArray(JsonNode obj, JsonNode parent) {

	}
}
