package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
		bMark.startNewBenchmark("pre-processing of " + path);
		path = cleanFile(path);
		BufferedReader br = IO.getFileReader(path);
		String jsonObject;
		JsonFactory factory = new JsonFactory();
		IGermanDetector deDetector = new TikaGermanDetector();
		
		System.out.print("[PRCSS]\t" + path + " ...");
		
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
				.replaceAll(Patterns.TWITTER_HASHTAG, "")
				.replaceAll(Patterns.PATTERN_TWITTER_RETWEET, "")
				.replaceAll(Patterns.TWITTER_MENTION, "")
				.replaceAll(Patterns.PATTERN_URL, "");
	}
	
	
	private String cleanFile(String path){
		System.out.print("[CLEAN]\t" + path + " ...");
		String outPath = path + ".tmp";
		
		try {
			BufferedReader br = IO.getFileReader(path);
			OutputStreamWriter bw = IO.getFileWriter(outPath);
			String line;
			
			//skip lines without json data and lines not tagged "de"
			//then clean lines from invalid chars etc.
			while ((line = br.readLine()) != null) {
				if (!line.matches(Patterns.PATTERN_JSON_OBJECT_LANG_DE)) continue;
				line = cleanStringFromInvalidChars(line);
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
		return outPath;
	}
	
	
	private String cleanStringFromInvalidChars(String string){
		return string.replaceAll(Patterns.PATTERN_CONTROL_CHARS, "")
			      	 .replaceAll(Patterns.PATTERN_INCOMPLETE_UNICODE, "")
					 .replaceAll("\n", " ");
	}

}
