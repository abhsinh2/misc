package com.abhsinh2.json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonReaderToCSV {

	private static final List<String> IGNORE_LIST = Arrays.asList("__metadata", "__deferred");

	public static void main(String[] args) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(new File(
				"/Users/abhsinh2/Abhishek/github/misc/json-reader/src/main/resources/test.json"));

		List<Map<String, String>> records = readRoot(rootNode);

		System.out.println("================================");
		records.forEach(record -> System.out.println(record));

		// Write to csv
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(
				"/Users/abhsinh2/Abhishek/github/misc/json-reader/src/main/resources/test.csv"));
		CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

		Collection<String> headers = new LinkedHashSet<>();
		records.forEach(record -> {
			headers.addAll(record.keySet());
		});

		System.out.println("================================");
		System.out.println(headers);
		csvPrinter.printRecord(headers);

		System.out.println("================================");
		records.forEach(record -> {
			List<String> values = new ArrayList<>();
			headers.forEach(header -> {
				values.add(record.get(header));
			});
			System.out.println(values);
			try {
				csvPrinter.printRecord(values);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		csvPrinter.flush();
		csvPrinter.close();
	}

	private static List<Map<String, String>> readRoot(JsonNode root) {
		List<Map<String, String>> records = new ArrayList<>();
		if (root.isObject()) {
			readObject(root, null, records, null);
		} else if (root.isArray()) {
			readArray(root, null, records, null);
		}
		return records;
	}

	private static void readObject(JsonNode node, String parentKey, List<Map<String, String>> records,
			Map<String, String> record) {
		Iterator<Map.Entry<String, JsonNode>> childNodeIter = node.fields();
		while (childNodeIter.hasNext()) {
			Map.Entry<String, JsonNode> childEntry = childNodeIter.next();
			String childKey = childEntry.getKey();
			JsonNode childNode = childEntry.getValue();

			if (childKey.equals("results")) {
				if (parentKey == null) {
					readResult(childNode, null, records, record);
				} else {
					readResult(childNode, parentKey + "_" + childKey, records, record);
				}				
			} else {
				if (node.isObject()) {
					readObject(childNode, parentKey, records, record);
				} else if (node.isArray()) {
					readArray(childNode, parentKey, records, record);
				}
			}
		}
	}

	private static void readArray(JsonNode node, String parentKey, List<Map<String, String>> records,
			Map<String, String> record) {
		Iterator<JsonNode> nodeIter = node.iterator();
		while (nodeIter.hasNext()) {
			JsonNode childNode = nodeIter.next();
			if (childNode.isObject()) {
				readObject(childNode, parentKey, records, record);
			} else if (childNode.isArray()) {
				readArray(childNode, parentKey, records, record);
			}
		}
	}

	private static void readResult(JsonNode resultNode, String parentKey, List<Map<String, String>> records,
			Map<String, String> record) {
		if (resultNode.isArray()) {
			Iterator<JsonNode> resultNodeIter = resultNode.iterator();
			while (resultNodeIter.hasNext()) {

				Map<String, String> newRecord = null;
				if (record == null) {
					newRecord = new LinkedHashMap<>();
				} else {
					newRecord = new LinkedHashMap<>();
					newRecord.putAll(record);
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
								if (parentKey == null) {
									readArray(value, key, records, newRecord);
								} else {
									readArray(value, parentKey + "_" + key, records, newRecord);
								}
							} else if (value.isObject()) {
								if (parentKey == null) {
									readObject(value, key, records, newRecord);
								} else {
									readObject(value, parentKey + "_" + key, records, newRecord);
								}
							} else {
								if (parentKey == null) {
									System.out.println(key + ":" + value);
									newRecord.put(key, value.asText());
								} else {
									System.out.println(parentKey + "_" + key + ":" + value);
									newRecord.put(parentKey + "_" + key, value.asText());
								}
							}
						}
					}
				}

				System.out.println("-------------------------------------------");
			}
		}
	}

}
