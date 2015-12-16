package ang.cse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;

public class CSE {

	private Customsearch search;
	private Properties props;

	public CSE() {

		search = new Customsearch.Builder(new NetHttpTransport(), new JacksonFactory(), null)
				.setApplicationName("anglizismen").build();

		props = getAccess();

	}

	public List<Result> search(String query) {
		List<Result> results;
		try {
			Customsearch.Cse.List list = search.cse().list(query);

			// ID
			list.setKey(props.getProperty("auth_key"));
			list.setCx(props.getProperty("cx"));

			// Exact terms
			// list.setExactTerms(query);

			results = list.execute().getItems();
		} catch (IOException e) {
			e.printStackTrace();
			results = new ArrayList<Result>();
		}
		return results;
	}

	private Properties getAccess() {

		props = new Properties();
		InputStream input = null;
		try {

			input = new FileInputStream("cse.properties");
			props.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return props;
	}

}
