package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import de.uni_koeln.spinfo.ang.benchmark.BenchmarkData;
import de.uni_koeln.spinfo.ang.benchmark.SimpleBenchmark;
import de.uni_koeln.spinfo.ang.langdetect.IGermanDetector;
import de.uni_koeln.spinfo.ang.langdetect.TikaGermanDetector;
import de.uni_koeln.spinfo.ang.util.IO;

public class TwitterPreProcessor {
	
	private SimpleBenchmark bMark;
	
	
	public TwitterPreProcessor(){
		bMark = new SimpleBenchmark();
	}
	
	
	public BenchmarkData process(String path){
		path = cleanFile(path);
		BufferedReader br = IO.getReader(path);
		String jsonObject;
		JsonFactory factory = new JsonFactory();
		IGermanDetector deDetector = new TikaGermanDetector();
		
		System.out.print("[PREPROCESSING]\t" + path + " ...");
		bMark.startNewBenchmark("pre-processing of " + path);
		try {
			while ((jsonObject = br.readLine()) != null){
				JsonParser parser = factory.createParser(jsonObject);
				String text;
				
				//parse json object
				while(!parser.isClosed()){
					String fieldName = parser.nextFieldName();
					if (fieldName != null && fieldName.equals("text")){
						if ((text = normalize(parser.nextTextValue()))
								.matches(Patterns.PATTERN_HAS_LATIN_CHARS)
								&& deDetector.isGerman(text)){
							//TODO process
							//System.out.println("text = " + text);
							
							//benchmark step
							bMark.newStep();
							
							//abort processing for this object (text found)
							parser.close();
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//delete temp file
		IO.deleteFile(path);
		
		System.out.println(" DONE");
		return bMark.stopBenchMark();
	}
	
	
	private String normalize(String input){
		//input = StringEscapeUtils.unescapeJava(input);
		return Normalizer.normalize(input, Form.NFC)
				.replaceAll("\n", " ")
				.replaceAll(Patterns.TWITTER_HASHTAG, "")
				.replaceAll(Patterns.PATTERN_TWITTER_RETWEET, "")
				.replaceAll(Patterns.TWITTER_MENTION, "")
				.replaceAll(Patterns.PATTERN_URL, "");
//				.replaceAll("[\\x0E-\\x1F]", "");
	}
	
	
	private String cleanFile(String path){
		System.out.print("[CLEANING]\t" + path + " ...");
		File fileIn = new File(path);
		File fileOut = new File(path + ".tmp");
		
		try {
			FileReader fr = new FileReader(fileIn);
			BufferedReader br = new BufferedReader(fr);
			FileWriter fw = new FileWriter(fileOut);
			BufferedWriter bw = new BufferedWriter(fw);
			String line;
			
			//skip lines without json data and lines not tagged "de"
			//then clean lines from invalid chars etc.
			while ((line = br.readLine()) != null) {
				if (!line.matches(Patterns.PATTERN_JSON_OBJECT_LANG_DE)) continue;
				line = cleanString(line);
				bw.write(line);
				bw.write("\n");
			}
			
			br.close();
			bw.flush();
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(" DONE");
		return fileOut.getAbsolutePath();
	}
	
	
	private String cleanString(String string){
		return string.replaceAll("[\\x11-\\x14]", "");
	}

}
