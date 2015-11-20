package de.spinfo.uni_koeln.ang.htmlunit;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.conn.HttpHostConnectException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;

import de.uni_koeln.spinfo.ang.utils.FileUtils;

public class S1 {

	private static int results = 100;
	private static Sources sources = new Sources();
	private static boolean resultChecked = false;
	private static ArrayList<S1Entry> entriesList = new ArrayList<S1Entry>();
	private static boolean timeOut = false;

	void listReader() throws IOException, InterruptedException {

		int failureCount = 0;

		String fileInput = FileUtils.inputPath + "anginput.txt";

		ArrayList<String> inputList = (ArrayList<String>) FileUtils.fileToList(fileInput);

		for (String string : inputList) {
			timeOut = false;
			try {
				s1Crawler(string);

			} catch (HttpHostConnectException e) {
				timeOut = true;
				e.printStackTrace();
				s1Crawler(string);
				System.out.println("Failure number " + failureCount + "\n");
				failureCount++;
			}

		}
		System.out.println("Anzahl der Abbrüche " + failureCount + "\n");
		FileUtils.serializeList(entriesList, "serializedList" + sources.getSource(0));
	}

	public static void s1Crawler(String token) throws IOException, InterruptedException {
		S1Entry entry = new S1Entry();
		results = 100;
		resultChecked = false;
		s1Crawler(token, 1, entry);
	}

	public static void s1Crawler(String token, int cnt, S1Entry entry) throws IOException, InterruptedException {

		GregorianCalendar now = new GregorianCalendar();
		DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
		String datum = df.format(now.getTime());
		df = DateFormat.getTimeInstance(DateFormat.SHORT);
		String time = df.format(now.getTime());

		entry.setSearch_term(token);
		entry.setDate(datum + " " + time);
		entry.setPage(cnt);

		String fileName = FileUtils.outputPath + token + ".txt";

		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));

		if (timeOut) {
			Thread.sleep(5000000);
		}

		WebClient client = new WebClient(BrowserVersion.CHROME);
		client.getOptions().setThrowExceptionOnScriptError(false);
		client.getOptions().setTimeout(45000);

		String adress = sources.getSource(0) + "/search?tab=blog&p=" + cnt + "&q=\"" + token + "\"&lng=de&";

		// ///////
		logOff();
		// ///////

		try {
			HtmlPage page = client.getPage(adress);

			@SuppressWarnings("unchecked")
			List<HtmlElement> headLines = (List<HtmlElement>) page
					.getByXPath("//table/tbody/tr/td/a[@class='main_link']");

			@SuppressWarnings("unchecked")
			List<HtmlDivision> messages = (List<HtmlDivision>) page
					.getByXPath("//table/tbody/tr/td/div[@class='message']");

			@SuppressWarnings("unchecked")
			List<HtmlSpan> time1 = (List<HtmlSpan>) page
					.getByXPath("//table/tbody/tr/td/span[@class='time pull-right']");

			if (!resultChecked) {
				HtmlDivision totalResult = (HtmlDivision) page.getByXPath("//div[@class='container-fluid']").get(1);

				String resultNumberString = totalResult.asText();

				if (resultNumberString.contains(" of ")) {
					resultNumberString = resultNumberString.split(" of ")[1];
					resultNumberString = resultNumberString.split(" ")[0];
					resultNumberString = resultNumberString.replaceAll("\\D", "");

					int number = Integer.valueOf(resultNumberString);

					if (number < 901) {
						results = (number / 10) + 1;
					}
				} else {
					results = 1;
				}
				resultChecked = true;
			}

			for (int i = 0; i < messages.size(); i++) {
				if (headLines.get(i) != null) {
					if ((messages.get(i).asText().toLowerCase().contains(token.toLowerCase()))
							|| headLines.get(i).asText().toLowerCase().contains(token.toLowerCase())) {
						entry.setTitle(headLines.get(i).asText());
						entry.setPub_date(time1.get(i).asText());
						entry.setContent(messages.get(i).asText());

					}
				}
			}

		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (cnt == results) {
			oos.writeObject(entry.toString());
			entriesList.add(entry);
			client.close();
			oos.close();

		} else {
			cnt++;
			s1Crawler(token, cnt, entry);
		}
	}

	private static void logOff() {
		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		Logger.getLogger("org.apache.http").setLevel(Level.OFF);
	}

}
