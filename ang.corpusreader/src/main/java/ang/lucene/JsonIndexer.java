package ang.lucene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.SimpleFSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class JsonIndexer {
	// String startPath = "../";
	private IndexWriter writer;
	private File f;
	
	public JsonIndexer(String[] input) throws Exception {
		if (input.length != 2) {
			throw new Exception("Usage: java " + Indexer.class.getName() + " <index dir> <data dir>");
		}
		String indexDir = input[0];
		String dataDir = input[1];
		long start = System.currentTimeMillis();
		JsonIndexer indexer = new JsonIndexer(indexDir);
		int numIndexed = indexer.index(dataDir);
		indexer.close();
		long end = System.currentTimeMillis();
		System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
	}

	public JsonIndexer(String indexDir) throws IOException {
		writer = new IndexWriter(new SimpleFSDirectory(Paths.get(indexDir)),
				new IndexWriterConfig(new StandardAnalyzer()));
	}

	public void close() throws IOException {
		writer.close(); // 4
	}

	public int index(String dataDir) throws Exception {
		File[] files = new File(dataDir).listFiles();
		for (int i = 0; i < files.length; i++) {
			f = files[i];
			List<String> JsonList = getJsonContent();
			for (String singleTweet : JsonList) {
				indexFile(singleTweet);
			}
		}
		return writer.numDocs();
	}


	@SuppressWarnings("deprecation")
	protected Document getDocument(File f, String content) throws Exception {
		Document doc = new Document();

		Field notvField = new Field("notv", content, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO);
		doc.add(notvField);
		doc.add(new Field("tv", f.getCanonicalPath(), Field.Store.YES, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS_OFFSETS));

		return doc;
	}

	private void indexFile(String tweetText) throws Exception {
		Document doc = getDocument(f, tweetText);
		if (doc != null) {
			writer.addDocument(doc); // 9
		}

	}

	public List<String> getJsonContent() throws FileNotFoundException, IOException, ParseException {
		JSONParser parser = new JSONParser();
		Object object = parser.parse(new FileReader(f.getPath()));
		String text = "nichts gefudnen";
		JSONArray a = (JSONArray) object;
		List<String> contentList = new ArrayList<String>();
		for (Object o : a) {
			JSONObject jsonObj = (JSONObject) o;
			text = (String) jsonObj.get("text");
			contentList.add(text);
			// System.out.println("tweetText: " + text);
		}
		System.out.println("Es wurden " + contentList.size() + " Tweets in der Datei " + f.getName() + " gefunden!");
		return contentList;
	}

	// public void indexJson(File f) {
	// InputStream jsonFile = getClass().getResourceAsStream(f.getPath());
	// Reader readerJson = new InputStreamReader(jsonFile);
	// // Parse the json file using simple-json library
	// Object fileObjects = JSONValue.parse(readerJson);
	// JSONArray arrayObjects = (JSONArray) fileObjects;
	// }

}
