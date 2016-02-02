package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import de.uni_koeln.spinfo.ang.benchmark.BenchmarkData;
import de.uni_koeln.spinfo.ang.benchmark.SimpleBenchmark;


public class TwitterJsonPreProcessing {
	
	private StringRangeScanner srs;
	private SimpleBenchmark bMark;
	
	private static final String PATTERN_START = "\\{\\\"created_at\\\"\\:.+\\\"text\\\"\\:\\\"";
	private static final String PATTERN_END   = "(?<!\\\\)\\\"";
	
	private static final String PATTERN_TWITTER_MENTION = "(?<=^|(?<=[^a-zA-Z0-9-\\.]))@([A-Za-z0-9_]+)([^a-zA-Z0-9_]|$)";
	private static final String PATTERN_TWITTER_HASHTAG = "(?<=^|(?<=[^a-zA-Z0-9-_\\.]))#([A-Za-z]+[A-Za-z0-9_]+)";
	private static final String PATTERN_TWITTER_RETWEET = "RT\\s" + PATTERN_TWITTER_MENTION + "\\s";
	private static final String PATTERN_UNICODES		= "\\\\u(?=[a-fA-F0-9]{4})";
	private static final String PATTERN_STR_HAS_WORDS	= ".*\\p{L}.*";
	
	private static final String FILE_PATH     = "/Users/bkiss/Documents/testdata/test.json";
	
	
	public static void main(String[] args) {
		TwitterJsonPreProcessing pre = new TwitterJsonPreProcessing();
		
		try {
			pre.preProcess(FILE_PATH).printReport();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public BenchmarkData preProcess(String path) throws FileNotFoundException{
		srs = createScanner(path);
		bMark = new SimpleBenchmark();
		bMark.startNewBenchmark("pre-processing of " + FILE_PATH);
		StringBuilder sb = new StringBuilder();
		
		//TODO pre-process
		while (srs.hasNext()){
			String s = normalize(srs.next());
			if (s.matches(PATTERN_STR_HAS_WORDS)) sb.append(s + "\n");
			bMark.newStep();
		}
		
		//write output file
		try {
			FileWriter fw = new FileWriter("output.txt");
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bMark.stopBenchMark();
	}
	
	
	private StringRangeScanner createScanner(String filePath) throws FileNotFoundException{
		File file = new File(filePath);
		
		if (!file.exists() || !file.isFile()){
			System.err.println("[ERROR]\t\"" + filePath + " \"could not be found or is not a regular file!");
			return null;
		}
		
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		return new StringRangeScanner(PATTERN_START, PATTERN_END, bis);
	}
	
	
	private String normalize(String input){
		return Normalizer.normalize(input, Form.NFC)
				.replaceAll(PATTERN_TWITTER_HASHTAG, "")
				.replaceAll(PATTERN_TWITTER_RETWEET, "")
				.replaceAll(PATTERN_TWITTER_MENTION, "");
	}
	
	
}
