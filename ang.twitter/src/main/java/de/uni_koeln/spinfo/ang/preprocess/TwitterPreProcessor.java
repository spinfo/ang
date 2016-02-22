package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
		String tempPath = cleanFile(path);
		String fileName = path.substring(path.lastIndexOf('/')+1);
		BufferedReader br = IO.getFileReader(tempPath);
		String jsonObject;
		JsonFactory factory = new JsonFactory();
		IGermanDetector deDetector = new TikaGermanDetector();
		
		System.out.print("[PRCSS]\t" + tempPath + " ...");
		long id = 0;
		
		try {
			while ((jsonObject = br.readLine()) != null){
//				JsonParser parser = factory.createParser(jsonObject);
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> map = new HashMap<String, Object>();
				map = mapper.readValue(jsonObject, new TypeReference<Map<String, Object>>(){});
				
				String text = (String) map.get("text");
				String textCleaned = cleanTweet(text);
				
				if (textCleaned.matches(Patterns.PATTERN_HAS_LATIN_CHARS)
						&& deDetector.isGerman(textCleaned)){
					
					//make output directory
					File dir = new File(path + "_output" + File.separator);
					if (!dir.exists()) dir.mkdir();
					
					JsonGenerator g = factory.createGenerator(
							IO.getFileWriter(dir.getAbsolutePath() 
									+ File.separator 
									+ id++ + ".json"));

					g.writeStartObject();
					g.writeStringField("text", text);
					g.writeStringField("text_clean", textCleaned);
					g.writeStringField("id", "twitter-" + (String)map.get("id_str"));
					g.writeStringField("date", (String)map.get("created_at"));
					g.writeStringField("source", "twitter");
					g.writeStringField("source_file", fileName);
					g.writeEndObject();
					g.close();
					//benchmark step
					bMark.newStep();
				}
				
				
//				//parse json object
//				while(!parser.isClosed()){
//					String fieldName = parser.nextFieldName();
//					if (fieldName != null && fieldName.equals("text")){
//						text = normalize(parser.nextTextValue());
//						textCleaned = cleanTweet(text);
//						if (textCleaned.matches(Patterns.PATTERN_HAS_LATIN_CHARS)
//								&& deDetector.isGerman(textCleaned)){
//							//TODO process
//							//System.out.println("text = " + text);
//							
//							//make output directory
//							File dir = new File(path + "_output" + File.separator);
//							dir.mkdir();
//							
//							JsonGenerator g = factory.createGenerator(
//									IO.getFileWriter(dir.getAbsolutePath() 
//											+ File.separator 
//											+ id++ + ".json"));
//
//							g.writeStartObject();
//							g.writeStringField("text", text);
//							g.writeEndObject();
//							g.close();
//							
////							System.out.println(text);
//							
//							//benchmark step
//							bMark.newStep();
//							
//							//abort processing for this object (text found)
//							parser.close();
//							break;
//						}
//					}
//				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//delete temp file
		IO.deleteFile(tempPath);
		
		System.out.println(" DONE");
		return bMark.stopBenchMark();
	}
	
	
	private String normalize(String input){
		return Normalizer.normalize(input, Form.NFC);
	}
	
	
	private String cleanTweet(String input){
		return input
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
