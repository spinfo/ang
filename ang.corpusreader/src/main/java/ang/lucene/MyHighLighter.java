package ang.lucene;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

public class MyHighLighter {

	static String s = "Export/";
	private static String inputPath = s;
	String[] input = { "lucene/", inputPath };

	@SuppressWarnings("deprecation")
	public void testHighlighter(String[] param) throws Exception {

		Path indexPath = Paths.get(param[0]);
		Directory dir = new SimpleFSDirectory(indexPath);
		IndexReader ir = DirectoryReader.open(dir);
		// System.out.println("ir numdocs: " + ir.numDocs());
		Analyzer analyzer = new StandardAnalyzer();

		IndexSearcher searcher = new IndexSearcher(ir);

		QueryParser parser = new QueryParser("notv", analyzer);
		Query query = parser.parse(param[1]);
		TopDocs hits = searcher.search(query, 10);
		int shownResults = 5;
		// System.out.println("Anzahl der Ergebnisse für Query '" +
		// query.toString("notv") + "' : " + searcher.count(query));
		SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
		Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));
		if (searcher.count(query) > 0)
			for (int i = 0; i < searcher.count(query); i++) {
				System.out.println("hits: " + hits.scoreDocs.length);
				/* id = id des vom Searcher gefundenen Documents */
				int id = hits.scoreDocs[i].doc;

				Document doc = searcher.doc(id);
				// System.out.println(doc.getFields().size());
				// String text = getFileContent(new File(doc.get("tv")));
				String text = doc.get("notv");
				// System.out.println("der gefundene text lautet: " + text);
				TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), id, "notv",
						analyzer);

				TextFragment[] frag = highlighter.getBestTextFragments(tokenStream, text, false, shownResults);
				for (int j = 0; j < frag.length; j++) {
					if ((frag[j] != null)) {
						System.out.println("erste Ausgabe: " + (frag[j].toString()));
					}
				}
				// Term vector
				text = doc.get("tv");
				tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), hits.scoreDocs[i].doc, "tv",
						analyzer);
				frag = highlighter.getBestTextFragments(tokenStream, text, false, shownResults);
				for (int j = 0; j < frag.length; j++) {
					if ((frag[j] != null) && (frag[j].getScore() > 0)) {
						System.out.println("zweite Ausgabe: " + (frag[j].toString()));
					}
				}
				System.out.println("-------------");
			}
	}

}
