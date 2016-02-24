package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_koeln.spinfo.ang.benchmark.BenchmarkData;
import de.uni_koeln.spinfo.ang.benchmark.SimpleBenchmark;
import de.uni_koeln.spinfo.ang.data.CorpusObject;
import de.uni_koeln.spinfo.ang.data.CorpusObjectField;
import de.uni_koeln.spinfo.ang.langdetect.IGermanDetector;
import de.uni_koeln.spinfo.ang.langdetect.TikaGermanDetector;
import de.uni_koeln.spinfo.ang.util.FormatConvert;
import de.uni_koeln.spinfo.ang.util.IO;
import de.uni_koeln.spinfo.ang.util.MongoWrapper;

public class TwitterPreProcessor {
	
	private SimpleBenchmark bMark;
	private MongoWrapper mongo;
	
	
	public TwitterPreProcessor(MongoWrapper mongo){
		bMark = new SimpleBenchmark();
		this.mongo = mongo;
	}
	
	
	public BenchmarkData process(File file){
		String path = file.getAbsolutePath();
		bMark.startNewBenchmark("processing of " + file.getName());
		String fileName = file.getName();
		BufferedReader br = IO.getFileReader(path);
		String jsonObject;
//		JsonFactory factory = new JsonFactory();
		IGermanDetector deDetector = new TikaGermanDetector();
		Map<String, Object> map;
		
		System.out.println("[PRCSS]\t" + path + " ...");
		
		try {
			while ((jsonObject = br.readLine()) != null){
				if ((jsonObject = getValidatedJsonObject(jsonObject)) == null) continue;
				ObjectMapper mapper = new ObjectMapper();
				map = mapper.readValue(jsonObject, new TypeReference<Map<String, Object>>(){});
				
				String text = normalize((String)map.get("text"));
				String textCleaned = cleanTweet(text);
				
				if (textCleaned.matches(Patterns.HAS_LATIN_CHARS)
						&& deDetector.isGerman(textCleaned)){
					
					CorpusObject corpusObject = new CorpusObject();
					corpusObject.addData(CorpusObjectField.TEXT_STRING, text);
					corpusObject.addData(CorpusObjectField.ID_STRING,
							"twitter-" + map.get("id_str").toString());
					corpusObject.addData(CorpusObjectField.DATE_YEAR_INT,
							FormatConvert.yearFromTwitterDateString(
									map.get("created_at").toString()));
					corpusObject.addData(CorpusObjectField.DATE_MONTH_INT,
							FormatConvert.monthFromTwitterDateString(
									map.get("created_at").toString()));
					corpusObject.addData(CorpusObjectField.SOURCE_STRING, "twitter");
					corpusObject.addData(CorpusObjectField.SOURCE_FILE_STRING, fileName);
					corpusObject.addData(CorpusObjectField.SOURCE_ARCHIVE_STRING, fileName + ".gz");
					corpusObject.addData(CorpusObjectField.LENGTH_INT, text.length());
					
					mongo.addDocument(corpusObject);
					
					//make output directory
//					File dir = new File(path + "_output" + File.separator);
//					if (!dir.exists()) dir.mkdir();
//					
//					JsonGenerator g = factory.createGenerator(
//							IO.getFileWriter(dir.getAbsolutePath() 
//									+ File.separator 
//									+ map.get("id_str") + ".json"));
//
//					g.writeStartObject();
//					g.writeStringField(CorpusObjectField.TEXT.v(), text);
//					g.writeStringField(CorpusObjectField.ID.v(),
//							"twitter-" + map.get("id_str").toString());
//					g.writeStringField(CorpusObjectField.DATE_YEAR.v(),
//							FormatConvert.yearFromTwitterDateString(
//									map.get("created_at").toString())+"");
//					g.writeStringField(CorpusObjectField.DATE_MONTH.v(),
//							FormatConvert.monthFromTwitterDateString(
//									map.get("created_at").toString())+"");
//					g.writeStringField(CorpusObjectField.SOURCE.v(), "twitter");
//					g.writeStringField(CorpusObjectField.SOURCE_FILE.v(), fileName);
//					g.writeStringField(CorpusObjectField.SOURCE_ARCHIVE.v(), fileName + ".gz");
//					g.writeStringField(CorpusObjectField.LENGTH.v(), text.length()+"");
//					g.writeEndObject();
//					g.close();
					//benchmark step
					bMark.newMarker();
				}
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//delete temp file
//		IO.deleteFile(tempPath);
		return bMark.stopBenchMark();
	}
	
	
	private String normalize(String input){
		return Normalizer.normalize(input, Form.NFC);
	}
	
	
	private String cleanTweet(String input){
		return input
				.replaceAll(Patterns.TWITTER_HASHTAG, "")
				.replaceAll(Patterns.TWITTER_RETWEET, "")
				.replaceAll(Patterns.TWITTER_MENTION, "")
				.replaceAll(Patterns.URL, "");
	}
	
	
//	private String cleanFile(String path){
//		System.out.print("[CLEAN]\t" + path + " ...");
//		String outPath = path + ".tmp";
//		
//		try {
//			BufferedReader br = IO.getFileReader(path);
//			OutputStreamWriter bw = IO.getFileWriter(outPath);
//			String line;
//			
//			//skip lines without json data and lines not tagged "de"
//			//then clean lines from invalid chars etc.
//			while ((line = br.readLine()) != null) {
//				if (!line.matches(Patterns.PATTERN_JSON_OBJECT_LANG_DE)) continue;
//				line = cleanStringFromInvalidChars(line);
//				bw.write(line);
//				bw.write("\n");
//			}
//			
//			br.close();
//			bw.flush();
//			bw.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println(" DONE");
//		return outPath;
//	}
	
	
	private String getValidatedJsonObject(String data){
		if (!data.matches(Patterns.JSON_OBJECT_LANG_DE)) return null;
		return cleanStringFromInvalidChars(data);
	}
	
	
	private String cleanStringFromInvalidChars(String string){
		return string.replaceAll(Patterns.CONTROL_CHARS, "")
			      	 .replaceAll(Patterns.INCOMPLETE_UNICODE, "")
					 .replaceAll("\n", " ");
	}

}
