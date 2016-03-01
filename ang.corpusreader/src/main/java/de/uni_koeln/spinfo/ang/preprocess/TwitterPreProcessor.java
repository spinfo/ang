package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_koeln.spinfo.ang.benchmark.BenchmarkData;
import de.uni_koeln.spinfo.ang.benchmark.SimpleBenchmark;
import de.uni_koeln.spinfo.ang.langdetect.IGermanDetector;
import de.uni_koeln.spinfo.ang.langdetect.TikaGermanDetector;
import de.uni_koeln.spinfo.ang.utils.FormatConvert;
import de.uni_koeln.spinfo.ang.utils.IO;
import de.uni_koeln.spinfo.ang.utils.MongoWrapper;
import de.uni_koeln.spinfo.ang.utils.Patterns;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObject;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObjectField;

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
		IGermanDetector deDetector = new TikaGermanDetector();
		Map<String, Object> map = new HashMap<String, Object>();
		
		System.out.println("[PRCSS]\t" + path + " ...");
		
		try {
			while ((jsonObject = br.readLine()) != null){
				if ((jsonObject = getValidatedJsonObject(jsonObject)) == null) continue;
				ObjectMapper mapper = new ObjectMapper();
				
				try {
					map = mapper.readValue(jsonObject, new TypeReference<Map<String, Object>>(){});
				} catch (Exception e) {
					continue;
				}
				
				String text = normalize((String)map.get("text"));
				String textCleaned = cleanTweet(text);
				
				if (textCleaned.matches(Patterns.HAS_LATIN_CHARS)
						&& deDetector.isGerman(textCleaned)){
					
					//create and prepare corpus object
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
					
//					System.out.println(textCleaned);
					
					bMark.newMarker(); //set benchmark step marker
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
