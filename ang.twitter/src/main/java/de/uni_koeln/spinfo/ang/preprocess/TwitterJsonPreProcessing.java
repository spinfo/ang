package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import org.jsfr.json.JacksonParser;
import org.jsfr.json.JsonPathListener;
import org.jsfr.json.JsonSurfer;
import org.jsfr.json.ParsingContext;
import org.jsfr.json.SurfingConfiguration;
import org.jsfr.json.provider.JavaCollectionProvider;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.internal.table.SqlJetTable;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.fasterxml.jackson.core.JsonParseException;

import de.uni_koeln.spinfo.ang.benchmark.BenchmarkData;
import de.uni_koeln.spinfo.ang.benchmark.SimpleBenchmark;
import de.uni_koeln.spinfo.ang.goldstnd.SqliteWrapper;


public class TwitterJsonPreProcessing {
	
//	private StringRangeScanner srs;
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
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public BenchmarkData preProcess(String path) throws JsonParseException, IOException, SqlJetException{
		bMark = new SimpleBenchmark();
		bMark.startNewBenchmark("pre-processing of " + FILE_PATH);
//		final StringBuilder sb = new StringBuilder();
		final SqliteWrapper sqlw = new SqliteWrapper();
		SqlJetDb db = sqlw.createAndOpenNewDB();
		final ISqlJetTable table = sqlw.getTable(db, "data_todo");
		sqlw.preTransaction(db);
		
		BufferedReader br = new BufferedReader(new FileReader(path));
		JsonSurfer surfer = new JsonSurfer(JacksonParser.INSTANCE, JavaCollectionProvider.INSTANCE);
        SurfingConfiguration config = surfer.configBuilder().bind("$.text", new JsonPathListener() {
                	private int count = 0;
                	
                    @Override
                    public void onValue(Object value, ParsingContext context) throws Exception {
            			String s = normalize(value.toString());
            			s = s.replaceAll("\\;", "");
            			s = s.replaceAll("\\\"", "'");
            			sqlw.insertRowTodo(table, count++, s, "archive.zip");
            			bMark.newStep();
                    }
                }).build();
        surfer.surf(br, config);
        sqlw.postTransaction(db);
        db.close();
		
		//write output file
//		try {
//			Writer out = new BufferedWriter(
//					new OutputStreamWriter(
//				    new FileOutputStream(OUTPUT_FILE), "UTF-8"));
//			out.write(sb.toString());
//			out.flush();
//			out.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		return bMark.stopBenchMark();
	}
	
	
//	private StringRangeScanner createScanner(String filePath) throws FileNotFoundException{
//		File file = new File(filePath);
//		
//		if (!file.exists() || !file.isFile()){
//			System.err.println("[ERROR]\t\"" + filePath + " \"could not be found or is not a regular file!");
//			return null;
//		}
//		
//		FileInputStream fis = new FileInputStream(file);
//		BufferedInputStream bis = new BufferedInputStream(fis);
//		return new StringRangeScanner(PATTERN_START, PATTERN_END, bis);
//	}
	
	
	private String normalize(String input){
		//input = StringEscapeUtils.unescapeJava(input);
		return Normalizer.normalize(input, Form.NFC)
				.replaceAll(PATTERN_TWITTER_HASHTAG, "")
				.replaceAll(PATTERN_TWITTER_RETWEET, "")
				.replaceAll(PATTERN_TWITTER_MENTION, "");
	}
	
	
	
	
}
