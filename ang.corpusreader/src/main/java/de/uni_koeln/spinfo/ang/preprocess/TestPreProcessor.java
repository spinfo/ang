package de.uni_koeln.spinfo.ang.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import de.uni_koeln.spinfo.ang.utils.AngStringUtils;
import de.uni_koeln.spinfo.ang.utils.IO;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObject;
import de.uni_koeln.spinfo.ang.utils.data.CorpusObjectField;

public class TestPreProcessor extends AbstractPreProcessor {

	@Override
	protected void transformCorpusObjects(File inputFile) {
		BufferedReader br = IO.getFileReader(inputFile.getAbsolutePath());
		
		String line;
		try {
			while ((line = br.readLine()) != null){
				line = AngStringUtils.cleanStringFromInvalidChars(line);
				line = AngStringUtils.normalize(line);
				
				CorpusObject obj = new CorpusObject();
				obj.addData(CorpusObjectField.ID_STRING, "test-" + line.hashCode());
				obj.addData(CorpusObjectField.TEXT_STRING, line);
				obj.addData(CorpusObjectField.LENGTH_INT, line.length());
				mongo.addDocument(obj.getBsonDocument()); //MUST be called for every created document!
				bMark.newMarker();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
