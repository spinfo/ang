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
import de.uni_koeln.spinfo.ang.data.CorpusObjectFields;
import de.uni_koeln.spinfo.ang.langdetect.IGermanDetector;
import de.uni_koeln.spinfo.ang.langdetect.TikaGermanDetector;
import de.uni_koeln.spinfo.ang.util.FormatConvert;
import de.uni_koeln.spinfo.ang.util.IO;

public class TwitterPreProcessor {
	
	private SimpleBenchmark bMark;
	
	
	public TwitterPreProcessor(){
		bMark = new SimpleBenchmark();
	}
	
	
	public BenchmarkData process(String path){
		bMark.startNewBenchmark("pre-processing of " + path);
//		String tempPath = cleanFile(path);
		String fileName = path.substring(path.lastIndexOf('/')+1);
		BufferedReader br = IO.getFileReader(path);
		String jsonObject;
		JsonFactory factory = new JsonFactory();
		IGermanDetector deDetector = new TikaGermanDetector();
		Map<String, Object> map;
		
		System.out.print("[PRCSS]\t" + path + " ...");
		long id = 0;
		
		try {
			while ((jsonObject = br.readLine()) != null){
				if ((jsonObject = getValidatedJsonObject(jsonObject)) == null) continue;
				ObjectMapper mapper = new ObjectMapper();
				map = mapper.readValue(jsonObject, new TypeReference<Map<String, Object>>(){});
				
				String text = normalize((String)map.get("text"));
				String textCleaned = cleanTweet(text);
				
				if (textCleaned.matches(Patterns.HAS_LATIN_CHARS)
						&& deDetector.isGerman(textCleaned)){
					
					//make output directory
					File dir = new File(path + "_output" + File.separator);
					if (!dir.exists()) dir.mkdir();
					
					JsonGenerator g = factory.createGenerator(
							IO.getFileWriter(dir.getAbsolutePath() 
									+ File.separator 
									+ id++ + ".json"));

					g.writeStartObject();
					g.writeStringField(CorpusObjectFields.TEXT, text);
					g.writeStringField(CorpusObjectFields.ID, "twitter-" + map.get("id_str").toString());
					g.writeStringField(CorpusObjectFields.DATE_YEAR, map.get("created_at").toString()
							.replaceAll(".+(?=" + Patterns.DATE_YEAR + ")", "")
							.replaceAll("(?<=" + Patterns.DATE_YEAR + ").+", ""));
					g.writeStringField(CorpusObjectFields.DATE_MONTH,
							FormatConvert.monthWordShortToNumber(
								map.get("created_at").toString()
								.replaceAll(".+(?=" + Patterns.DATE_MONTH_WORD_SHORT + ")", "")
								.replaceAll("(?<=" + Patterns.DATE_MONTH_WORD_SHORT + ").+", ""))+"");
					g.writeStringField(CorpusObjectFields.SOURCE, "twitter");
					g.writeStringField(CorpusObjectFields.SOURCE_FILE, fileName);
					g.writeStringField(CorpusObjectFields.SOURCE_ARCHIVE, fileName + ".gz");
					g.writeStringField(CorpusObjectFields.LENGTH, text.length()+"");
					g.writeEndObject();
					g.close();
					//benchmark step
					bMark.newStep();
				}
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//delete temp file
//		IO.deleteFile(tempPath);
		
		System.out.println(" DONE");
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
