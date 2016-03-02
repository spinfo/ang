package ang.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.tika.Tika;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;


public class Indexer {

	private Document document = new Document();

	public Document getDoc() {
		return document;
	}

	public void setDoc(Document doc) {
		this.document = doc;
	}

	public Indexer(String[] input) throws Exception {
		if (input.length != 2) {
			throw new Exception("Usage: java " + Indexer.class.getName() + " <index dir> <data dir>");
		}
		String indexDir = input[0]; // 1
		String dataDir = input[1]; // 2
		long start = System.currentTimeMillis();
		Indexer indexer = new Indexer(indexDir);
		int numIndexed = indexer.index(dataDir);
		indexer.close();
		long end = System.currentTimeMillis();
		System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
	}

	private IndexWriter writer;

	public Indexer(String indexDir) throws IOException {
		Path indexPath = Paths.get(indexDir);
		Directory dir = new SimpleFSDirectory(indexPath);
		writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));
	}

	public void close() throws IOException {
		writer.close(); // 4
	}

	public int index(String dataDir) throws Exception {
		File[] files = new File(dataDir).listFiles();

		System.out.println(files.length);

		for (int i = 0; i < files.length; i++) {
			File f = files[i];

			if (f.isDirectory()) {
				index(f.getPath());
			} else {
				Tika tika = new Tika();
				// String string = tika.detect(f);
				if (tika.detect(f).toLowerCase().equals("application/pdf")) {
					String content = "";
					try {
						PdfReader readerObj = new PdfReader(f.getAbsolutePath());
						int n = readerObj.getNumberOfPages();
						StringBuffer sb = new StringBuffer();
						for (int j = 1; j <= n; j++) {
							sb.append(PdfTextExtractor.getTextFromPage(readerObj, j));
						}
						content = sb.toString();

						f = new File(f.getAbsolutePath().replace(".pdf", "decoded.txt"));
						f.createNewFile();
						FileWriter writer = new FileWriter(f);
						writer.write(content);
						writer.flush();
						writer.close();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				indexFile(f);
			}
		}

		return writer.numDocs(); // 5
	}

	protected boolean acceptFile(File f) { // 6
		return f.getName().endsWith(".txt");
	}

	@SuppressWarnings("deprecation")
	protected Document getDocument(File f) throws Exception {
		Document doc = new Document();
//		System.out.println("Text beginnt mit " + getFileContent(f).toString().substring(0, 40));
//		StringField notvField = new StringField("notv", getFileContent(f), Field.Store.YES);
		Field notvField = new Field("notv", getFileContent(f), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO);
		doc.add(notvField);
		doc.add(new Field("tv", f.getCanonicalPath(), Field.Store.YES, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS_OFFSETS));

		return doc;
	}

	private void indexFile(File f) throws Exception {
		Document doc = getDocument(f);
		if (doc != null) {
			writer.addDocument(doc); // 9
		}

	}

	private String getFileContent(File f) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String s;

		StringBuffer sb = new StringBuffer();
		
		while ((s = br.readLine()) != null) {
			s = Normalizer.normalize(s, Normalizer.Form.NFC);
			sb.append(s);
			
		}
		br.close();

		String content = sb.toString();
		return content;
		

		/*Version 1: Exception in thread "main" java.nio.charset.MalformedInputException: Input length = 1 */
//		List<String> list = FileUtils.fileToList(f.getPath());
//		StringBuilder sb = new StringBuilder();
//		for (String string : list) {
//			sb.append(string);
//		}
//		return sb.toString();
		
		
	}

}
