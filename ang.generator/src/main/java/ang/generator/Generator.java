package ang.generator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import de.uni_koeln.spinfo.ang.utils.FileUtils;

public class Generator {
//	liste der englischen Tokens
	private List<String> englInput;
//	Liste der deutschen Präfixe
	private List<String> praefix;
//	Liste der deutschen Suffixe
	private List<String> suffix;
	private List<String> stopWords;
	private List<String> englStemms;
	private List<String> anglizismenPrae = new ArrayList<String>();
	private List<String> anglizismenSuf = new ArrayList<String>();
	private List<String> anglizismenZirk = new LinkedList<String>();

	
	/*
	 * Füllt die Listen mit Inhalt. 
	 * Liest Input Text-Files mit Präfixen, Suffixen, 
	 * englischen Tokens und stopwords aus und serialisiert diese.
	 * */
	
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

	@SuppressWarnings("unchecked")
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
		System.out.println(result.size());
		return result;
	}

	/*Setzt Stämme und Suffixe/Praefixe zusammen und fügt diese der Liste anglizismenSuf/anglizismenPrae hinzu*/
	
	public void generateSufPrae() throws ClassNotFoundException, IOException {
		read();
		
		anglizismenSuf = generateConcatList(suffix, false);
		anglizismenPrae = generateConcatList(praefix, true);

		FileUtils.serializeList(anglizismenSuf, "anglizismenSuf");
		FileUtils.serializeList(anglizismenPrae, "anglizismenPrae");

	}
	
	private List<String> generateConcatList(List<String> affixList, boolean putInFront){
		List<String> anglizismenAff = new ArrayList<String>();
		
		englStemms = getStems();
		
		for (String englStrig : englStemms) {
			
			for (String praString : affixList) {
				String angP = new String();
				if(putInFront){
				angP = praString + englStrig;}
				else{
					angP = englStrig + praString;
				}
				anglizismenAff.add(angP);
			}
		}
		
		return anglizismenAff;
	}
	

	public void generateZirkumfix() throws IOException, ClassNotFoundException {
		read();
		anglizismenSuf = generateConcatList(suffix, false);
		int i=0;
		
		for (String stringSuf : anglizismenSuf) {
			System.out.println(stringSuf);
			for (String prae : praefix) {
				String s = new String();
				s = prae + stringSuf;
				System.out.println(s);
				anglizismenZirk.add(s);
			}
			System.out.println(i);
			i++;
			
		}
		System.out.println(anglizismenZirk.size());
		FileUtils.serializeList(anglizismenZirk, "anglizismenZirk");
	}

}
