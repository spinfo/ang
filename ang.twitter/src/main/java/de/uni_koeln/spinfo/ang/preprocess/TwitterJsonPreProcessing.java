package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import org.apache.commons.lang3.StringEscapeUtils;

import de.uni_koeln.spinfo.ang.benchmark.BenchmarkData;
import de.uni_koeln.spinfo.ang.benchmark.SimpleBenchmark;


public class TwitterJsonPreProcessing {
	
	private StringRangeScanner srs;
	private SimpleBenchmark bMark;
	
	private static final String OUTPUT_FILE = "output_preprocessing.txt";
	
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
		long count = 0;
		
		// pre-process
		while (srs.hasNext()){
			String s = normalize(srs.next());
			s = s.replaceAll("\\;", "");
			s = s.replaceAll("\\\"", "'");
			sb.append(count++);
			sb.append(";");
			sb.append(s);
			sb.append("\n");
			bMark.newStep();
		}
		sb.deleteCharAt(sb.lastIndexOf("\n"));
		
		//write output file
		try {
			Writer out = new BufferedWriter(
					new OutputStreamWriter(
				    new FileOutputStream(OUTPUT_FILE), "UTF-8"));
			out.write(sb.toString());
			out.flush();
			out.close();
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
		//input = StringEscapeUtils.unescapeJava(input);
		return Normalizer.normalize(input, Form.NFC)
				.replaceAll(PATTERN_TWITTER_HASHTAG, "")
				.replaceAll(PATTERN_TWITTER_RETWEET, "")
				.replaceAll(PATTERN_TWITTER_MENTION, "");
	}
	
	
}
