package com.example.Raavan;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// Given a document, returns the hashmap of all words to their count in the document.
public class DocumentWorker implements Runnable {

	private String document = "";
	private ConcurrentLinkedQueue<HashMap<String, Integer>> queue = null;
	private String patternStr = "[a-zA-Z]+";

	public DocumentWorker(String document, ConcurrentLinkedQueue<HashMap<String, Integer>> queue) {
		this.document = document;
		this.queue = queue;
	}

	public void run() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(document);
		while (matcher.find()) {
			String word = matcher.group();
			if (map.containsKey(word)) {
				map.put(word, map.get(word) + 1);
			} else {
				map.put(word, 1);
			}
		}
		queue.add(map);
	}

}
