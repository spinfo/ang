package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import de.uni_koeln.spinfo.ang.utils.AngStringUtils;
import de.uni_koeln.spinfo.ang.utils.IO;
import de.uni_koeln.spinfo.ang.utils.Patterns;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObject;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObjectField;

public class WackyPreProcessor extends AbstractPreProcessor {

	@Override
	protected void transformCorpusObjects(File inputFile, int fileCount) {
		BufferedReader br = IO.getFileReader(
				inputFile.getAbsolutePath(),
				Charset.forName("windows-1252"));
		
		String line;
		try {
			while ((line = br.readLine()) != null){
				if (line.matches(Patterns.WACKY_URL_LINE)
						|| line.length() < 10) continue;
				
				line = AngStringUtils.cleanStringFromInvalidChars(line);
				line = AngStringUtils.normalize(line);
				
				int idHash = AngStringUtils.generateID(line);
				
				CorpusObject obj = new CorpusObject();
				obj.addData(CorpusObjectField.ID_STRING, "wacky-" + idHash);
				obj.addData(CorpusObjectField.TEXT_STRING, line);
				obj.addData(CorpusObjectField.SOURCE_STRING, "wacky");
				obj.addData(CorpusObjectField.SOURCE_FILE_STRING, "dewac_preproc");
				obj.addData(CorpusObjectField.SOURCE_ARCHIVE_STRING, "dewac_preproc.gz");
				obj.addData(CorpusObjectField.LENGTH_INT, line.length());
				
				mongo.addDocument(obj.getBsonDocument());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
