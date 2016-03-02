package ang.lucene;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

public class Searcher {

	public Searcher(String[] args) throws Exception {

		System.out.println("Searching...");
		if (args.length != 2) {
			throw new Exception("Usage: java " + Searcher.class.getName() + " <index dir> <query>");
		}
		String indexDir = args[0];
		String q = args[1];
		search(indexDir, q);
	}

	public static void search(String indexDir, String q) throws Exception {
		Path indexPath = Paths.get(indexDir);
		Directory dir = new SimpleFSDirectory(indexPath);
		IndexReader ir = DirectoryReader.open(dir);
		IndexSearcher is = new IndexSearcher(ir);
		QueryParser parser = new QueryParser("notv", new StandardAnalyzer());
		Query query = parser.parse(q);
		long start = System.currentTimeMillis();
		TopDocs hits = is.search(query, 10);

		long end = System.currentTimeMillis();
		System.err.println("Found " + hits.totalHits + " document(s) (in " + (end - start)
				+ " milliseconds) that matched query '" + q + "':");
		for (int i = 0; i < hits.scoreDocs.length; i++) {
			ScoreDoc scoreDoc = hits.scoreDocs[i];
			Document doc = is.doc(scoreDoc.doc);
			System.out.println(doc.get("notv") + " ( in " + doc.get("tv") + ")");
		}

	}

}
