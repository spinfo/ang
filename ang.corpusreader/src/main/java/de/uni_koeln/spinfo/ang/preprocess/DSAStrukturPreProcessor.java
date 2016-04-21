package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import de.uni_koeln.spinfo.ang.utils.AngStringUtils;
import de.uni_koeln.spinfo.ang.utils.IO;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObject;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObjectField;

public class DSAStrukturPreProcessor extends AbstractPreProcessor {

	@Override
	protected void transformCorpusObjects(File inputFile, int fileCount) {
		BufferedReader br = IO.getFileReader(inputFile.getAbsolutePath(), Charset.forName("windows-1252"));
		
		String line;
		try {
			while ((line = br.readLine()) != null){
				line = line.replaceAll("^\\S+\\s", "")
						.replaceAll("says\\:(\\\\t|\\s)", "")
						.replaceAll("\\[.+\\]", "").trim();
				if (line.length() < 10) continue;
				
				line = AngStringUtils.cleanStringFromInvalidChars(line);
				line = AngStringUtils.normalize(line);
				
				int idHash = AngStringUtils.generateID(line);
				
				CorpusObject obj = new CorpusObject();
				obj.addData(CorpusObjectField.ID_STRING, "dsa-struktur-" + idHash);
				obj.addData(CorpusObjectField.TEXT_STRING, line);
				obj.addData(CorpusObjectField.SOURCE_STRING, "dsa-struktur");
				obj.addData(CorpusObjectField.SOURCE_FILE_STRING, inputFile.getName());
				obj.addData(CorpusObjectField.SOURCE_ARCHIVE_STRING, "DSA-Korpus-Export.zip");
				obj.addData(CorpusObjectField.LENGTH_INT, line.length());
				
				mongo.addDocument(obj.getBsonDocument());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
