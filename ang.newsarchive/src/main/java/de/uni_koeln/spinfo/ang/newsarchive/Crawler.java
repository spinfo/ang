package de.uni_koeln.spinfo.ang.newsarchive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import de.uni_koeln.spinfo.ang.utils.FileUtils;

public class Crawler {
	private Entry se = new Entry();
	private String currentAusgabe = new String();
	private int artikelNr = 1;
	private boolean firstDate = true;
	private String date = new String();
	private String[] props;

	public void parser(String newsSource) throws IOException, ParserConfigurationException, SAXException {
		/* Structure of sources.properties:
		 * 1 name, 2 url, 3 urlsuffix1, 4 urlsuffix2, 5seperator, 6 firstyear, 7 ausgabencount, 
		 * 8 ausgabenTitel, 9 articleLink, 10 urlpaefix, 11 headline, 12 dateclass, 13 dateattr, 
		 * 14 content, 15 targetfolder
		 */
		props = getProps(newsSource);

		for (int year = Integer.valueOf(props[5]); year < 2017; year++) {

			String baseUrl = props[1] + year + props[2];
			Document initDoc = Jsoup.connect(baseUrl).timeout(0).get();
			/*
			 * anzahlDerAusgaben ergibt sich durch die Seitenaufteilung des
			 * Acrhivs
			 */
			int anzahlDerAusgaben = Integer.valueOf(props[7]) + initDoc.select(props[6]).size();
			for (Integer ausgabenNr = 1; ausgabenNr <= anzahlDerAusgaben; ausgabenNr++) {
				se = new Entry();
				String ausgabenNrString = ausgabenNr.toString();
				if (ausgabenNr < 10) {
					ausgabenNrString = "0" + ausgabenNrString;
				}

				baseUrl = props[1] + year + props[4] + ausgabenNrString + props[2];
				getAusgabe(baseUrl);
			}
		}

	}

	private void getAusgabe(String urlAusgabe) throws IOException {
		System.out.println("ausgabe");
		firstDate = true;
		artikelNr = 1;
		try {
			Document docAusgabe = Jsoup.connect(urlAusgabe).timeout(0).get();
			currentAusgabe = docAusgabe.getElementsByClass(props[8]).first().text();
			Elements artikelContent = docAusgabe.getElementsByClass(props[9]);
			for (Element element : artikelContent) {
				Element aElement = element.select("a").first();
				String link = aElement.attr("href");
				// System.out.println("links: " +link);
				if(!props[10].equals("-")){
				getArticleContents(props[10] + link);}
				else{
				getArticleContents(link);
				}
				firstDate = false;
			}
		} catch (Exception e) {
			System.out.println("Fehler bei Ausgabe: " + urlAusgabe);
			return;
		}

	}

	private void getArticleContents(String artikelUrl) throws IOException {
		System.out.println(artikelUrl);
		try {
			Document docArticle = Jsoup.connect(artikelUrl).timeout(0).get();
			String content = new String();
			if (docArticle.getElementsByClass(props[11]) != null) {
				content = docArticle.getElementsByClass(props[11]).first().text();
			}

			se.setAusgabe(currentAusgabe);
			se.setUrl(artikelUrl);

			if (!props[13].equals("-")) {
				date = docArticle.getElementsByClass(props[12]).attr(props[13]);
			} else {
				date = docArticle.getElementById(props[12]).text();
			}
			se.setDate(date);

			if (docArticle.getElementsByClass(props[14]) != null) {
				content = content + docArticle.getElementsByClass(props[14]).first().text();
			} else {
				System.out.println("keine Inhalte gefunden!");
			}
			se.setContentText(content);

			serialize(se, date);
		} catch (Exception e) {
			System.out.println("Fehler bei Artikel: " + artikelUrl);
			return;
		}
		artikelNr++;
	}

	private int serializefailure = 1;

	private void serialize(Entry entry, String date) throws IOException {
		String sername = new String();
		if (date.contains("T")) {
			sername = date.split("T")[0];
		} else {
			sername = date;
		}
		if (sername == null || sername.length() < 6) {
			sername = "nodate" + serializefailure;
			serializefailure++;
		}
		String twodigitnumber = String.valueOf(artikelNr);
		if (artikelNr < 10) {
			twodigitnumber = "0" + twodigitnumber;
		}
		String path = "zeitungsarchiv/" + props[0]+"/";
		
			try {
				new File(path).mkdir();
			} catch (Exception e) {
				System.out.println("Kann Ordner nicht erstellen!");
			}
		
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				new FileOutputStream("zeitungsarchiv/" + props[0] + "/"+ sername + "nr" + twodigitnumber));
		objectOutputStream.writeObject(entry);
		objectOutputStream.close();
	}

	public Entry readArticle(String fileName) throws IOException, ClassNotFoundException {
		FileInputStream inputFileStream = new FileInputStream(fileName);
		ObjectInputStream objectInputStream = new ObjectInputStream(inputFileStream);
		Entry entry = (Entry) objectInputStream.readObject();
		objectInputStream.close();
		inputFileStream.close();
		return entry;
	}

	private String[] getProps(String newsSource) throws IOException {
		List<String> sources = FileUtils.fileToList("sources.properties");
		for (String string : sources) {
			if (string.contains(newsSource)) {
				return string.split("\t");
			}
		}
		return null;
	}
}
