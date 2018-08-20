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

		//Collection<Map<String, String>> records = readRoot(rootNode, false);
		Collection<Map<String, String>> records = readRoot(rootNode.get("d"), false);
		//Collection<Map<String, String>> records = readRoot(rootNode.get("d").get("results"), true);

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

	private static Collection<Map<String, String>> readRoot(JsonNode root, boolean isResultData) {
		Collection<Map<String, String>> records = new ArrayList<>();
		if (root.isObject()) {
			readObject(root, null, records, null, isResultData);
		} else if (root.isArray()) {
			readArray(root, null, records, null, isResultData);
		}
		return records;
	}

	private static void readObject(JsonNode node, String parentKey, Collection<Map<String, String>> records,
			Map<String, String> record, boolean isResultData) {
		
		// System.out.println("Got Parent " + parentKey);
		// System.out.println("Got Record " + record);
		
		Map<String, String> newRecord = null;
		if (record == null) {
			newRecord = new LinkedHashMap<>();
		} else {
			newRecord = new LinkedHashMap<>();
			newRecord.putAll(record);
		}	
		
		if (isResultData)
			records.add(newRecord);
		
		Iterator<Map.Entry<String, JsonNode>> childNodeIter = node.fields();
		while (childNodeIter.hasNext()) {
			Map.Entry<String, JsonNode> childEntry = childNodeIter.next();
			String childKey = childEntry.getKey();
			JsonNode childNode = childEntry.getValue();

			if (!IGNORE_LIST.contains(childKey)) {				
				if (childNode.isArray()) {
					if (parentKey == null) {
						readArray(childNode, childKey, records, newRecord, childKey.equals("results"));
					} else {
						readArray(childNode, parentKey + "_" + childKey, records, newRecord, childKey.equals("results"));
					}
				} else if (childNode.isObject()) {
					if (parentKey == null) {
						readObject(childNode, childKey, records, newRecord, childKey.equals("results"));
					} else {
						readObject(childNode, parentKey + "_" + childKey, records, newRecord, childKey.equals("results"));
					}
				} else {
					if (parentKey == null) {
						System.out.println(childKey + ":" + childNode);
						newRecord.put(childKey, childNode.asText());
					} else {
						System.out.println(parentKey + "_" + childKey + ":" + childNode);
						newRecord.put(parentKey + "_" + childKey, childNode.asText());
					}
				}
			}
		}
	}

	private static void readArray(JsonNode node, String parentKey, Collection<Map<String, String>> records,
			Map<String, String> record, boolean isResultData) {
		Iterator<JsonNode> nodeIter = node.iterator();
		while (nodeIter.hasNext()) {
			JsonNode childNode = nodeIter.next();
			if (childNode.isObject()) {
				readObject(childNode, parentKey, records, record, isResultData);
			} else if (childNode.isArray()) {
				readArray(childNode, parentKey, records, record, isResultData);
			}
		}
	}

}
