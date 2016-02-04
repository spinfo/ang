package de.uni_koeln.spinfo.ang.preprocess;

import java.io.InputStream;
import java.util.Scanner;


public class StringRangeScanner {
	
	private Scanner scanner;
	private String startPattern;
	private String endPattern;
	private InputStream inputStream;
	
	
	public StringRangeScanner(String startPattern, String endPattern, InputStream inputStream) {
		super();
		this.startPattern = startPattern;
		this.endPattern = endPattern;
		this.inputStream = inputStream;
		this.scanner = new Scanner(inputStream);
		this.scanner.useDelimiter(startPattern);
		if (hasNext()) scanner.next(); //skip first hit
	}
	
	
	public boolean hasNext(){
		return scanner.hasNext();
	}
	
	
	public String next(){
		if (endPattern != null){
			return scanner.next().split(endPattern)[0];
		} else {
			return scanner.next();
		}
	}


	public String getStartPattern() {
		return startPattern;
	}


	public void setStartPattern(String startPattern) {
		this.startPattern = startPattern;
	}


	public String getEndPattern() {
		return endPattern;
	}


	public void setEndPattern(String endPattern) {
		this.endPattern = endPattern;
	}


	public InputStream getInputStream() {
		return inputStream;
	}


	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	

}
