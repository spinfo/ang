package ang.corpusreader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.uni_koeln.spinfo.ang.utils.FileUtils;

public class CorpusReader {

	private String inputPath = "ang/Data/corpora/DSA-Struktur/Export";
	private String outputPath;
	private List<List<String>> allContents = new ArrayList<List<String>>();

	public void Reader(String query) throws IOException {
		if (inputPath.contains("DSA-Struktur")) {
			File f = new File(inputPath);
			File[] subFiles = f.listFiles();
			for (File file : subFiles) {
				if (file.isFile()) {
					allContents.add(FileUtils.fileToList(file.getAbsolutePath()));
				}
			}
		}
	}

	
	// public void xmlReader(){
	// try {
	// // XMLReader erzeugen
	// XMLReader xmlReader = XMLReaderFactory.createXMLReader();
	//
	// // Pfad zur XML Datei
	// FileReader reader = new FileReader("X:\\personen.xml");
	// InputSource inputSource = new InputSource(reader);
	//
	// // DTD kann optional übergeben werden
	// // inputSource.setSystemId("X:\\personen.dtd");
	//
	// // PersonenContentHandler wird übergeben
	// xmlReader.setContentHandler(new PersonenContentHandler());
	//
	// // Parsen wird gestartet
	// xmlReader.parse(inputSource);
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (SAXException e) {
	// e.printStackTrace();
	// }
	// }
	// }
}
