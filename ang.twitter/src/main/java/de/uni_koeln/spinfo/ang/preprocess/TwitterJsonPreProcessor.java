package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import de.uni_koeln.spinfo.ang.benchmark.BenchmarkData;
import de.uni_koeln.spinfo.ang.benchmark.SimpleBenchmark;


public class TwitterJsonPreProcessor {
	
	private StringRangeScanner srs;
	private SimpleBenchmark bMark;
	
	private static final String PATTERN_START = "\\\"text\\\":\\\"";
	private static final String PATTERN_END   = "(?<!\\\\)\\\"";
	private static final String FILE_PATH     = "/Users/bkiss/Documents/testdata/test.json";
	
	
	public static void main(String[] args) {
		TwitterJsonPreProcessor pre = new TwitterJsonPreProcessor();
		
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
		
		//TODO pre-process
		if (srs.hasNext()){
			String s = srs.next();
			System.out.println(s);
			bMark.newStep();
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
	
	

}
