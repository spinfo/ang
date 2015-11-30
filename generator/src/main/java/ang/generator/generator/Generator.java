package ang.generator.generator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import de.uni_koeln.spinfo.ang.utils.FileUtils;

public class Generator {
	private List<String> englInput;
	private List<String> praefix;
	private List<String> suffix;
	private List<String> stopWords;
	private List<String> englStemms;
	private List<String> anglizismenPrae = new ArrayList<String>();
	private List<String> anglizismenSuf = new ArrayList<String>();
	private List<String> anglizismen = new ArrayList<String>();
	
	
	public void generateList() throws IOException {
		HashSet<String> p = new HashSet<String>();
		p.addAll(FileUtils.fileToList(FileUtils.inputPath
				+ "angGenerator/affix/praefix.txt"));
		System.out.println("Anzahl der Praefixe: " + p.size());

		HashSet<String> s = new HashSet<String>();
		s.addAll(FileUtils.fileToList(FileUtils.inputPath
				+ "angGenerator/affix/suffixe.txt"));
		System.out.println("Anzahl der Suffixe: " + s.size());

		HashSet<String> e = new HashSet<String>();
		e.addAll(FileUtils.fileToList(FileUtils.inputPath
				+ "angGenerator/corncob_lowercase.txt"));
		System.out.println("Anzahl der englischen Worte: " + e.size());

		stopWords = FileUtils.fileToList(FileUtils.inputPath
				+ "angGenerator/stopwords.txt");
		System.out.println("Anzahl der Stopwords: " + stopWords.size());

		e.removeAll(stopWords);
		System.out.println("nach Entfernung der StopWords hat englInput: "
				+ e.size());
		List<String> englInput = new ArrayList<String>(e);
		List<String> praefix = new ArrayList<String>(p);
		List<String> suffix = new ArrayList<String>(s);

		FileUtils.serializeList(englInput, "generator/lists/englishInput");
		FileUtils.serializeList(praefix, "generator/lists/praefix");
		FileUtils.serializeList(suffix, "generator/lists/suffix");

	}

	
	private void read() throws ClassNotFoundException, IOException {

		FileInputStream fis = new FileInputStream(FileUtils.outputPath
				+ "/generator/lists/englishInput");
		ObjectInputStream ois = new ObjectInputStream(fis);
		englInput = (List<String>) ois.readObject();
		ois.close();

		FileInputStream fis1 = new FileInputStream(FileUtils.outputPath
				+ "/generator/lists/praefix");
		ObjectInputStream ois1 = new ObjectInputStream(fis1);
		praefix = (List<String>) ois1.readObject();
		ois1.close();

		FileInputStream fis2 = new FileInputStream(FileUtils.outputPath
				+ "/generator/lists/suffix");
		ObjectInputStream ois2 = new ObjectInputStream(fis2);
		suffix = (List<String>) ois2.readObject();
		ois2.close();

	}

	private List<String> getStems() {

		SnowballStemmer stemmer = new englishStemmer();
		List<String> result = new ArrayList<String>();

		for (String t : englInput) {
			stemmer.setCurrent(t);
			stemmer.stem();
			result.add(stemmer.getCurrent());
		}

		return result;
	}
	

	public void generate() throws ClassNotFoundException, IOException{
		read();
		englStemms = getStems();
		
		for (String englStrig : englStemms) {
			String angP = new String();
			for (String praString : praefix) {
				angP = praString + englStrig;
				anglizismenPrae.add(angP);
			}
		}
		
		
		for (String englStrig : englStemms) {
			String angS = new String();
			for (String sufString : suffix) {
				angS = englStrig + sufString;
				anglizismenSuf.add(angS);
			}
		}
		
		
		
		FileUtils.serializeList(anglizismenSuf, "anglizismenSuf");
		FileUtils.serializeList(anglizismenPrae, "anglizismenPrae");	
		
	}
	
	
	
	
}
