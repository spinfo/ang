package de.uni_koeln.spinfo.ang;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.endpoint.StatusesSampleEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import de.uni_koeln.spinfo.ang.utils.FileUtils;

public class App {

	public static void main(String[] args) throws IOException, TwitterException, InterruptedException {

		App app = new App();

		Properties prop = app.getAccess();

		String consumerKey = prop.getProperty("consumerKey");
		String consumerSecret = prop.getProperty("consumerSecret");
		String token = prop.getProperty("token");
		String tokenSecret = prop.getProperty("tokenSecret");

		app.run(consumerKey, consumerSecret, token, tokenSecret);

	}

	public void listenStream(String consumerKey, String consumerSecret, String token, String secret)
			throws InterruptedException, IOException {
		BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
		StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();

		// add some track terms

		List<String> ang_cleaned = FileUtils.fileToList(FileUtils.inputPath + "ang_cleaned.txt");

		// System.out.println(ang_cleaned.size());
		// for (String s : ang_cleaned) {
		// System.out.println(s);
		// }

		endpoint.trackTerms(ang_cleaned);
		// endpoint.languages(Lists.newArrayList("de"));

		Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);

		// Create a new BasicClient. By default gzip is enabled.
		Client client = new ClientBuilder().hosts(Constants.STREAM_HOST).endpoint(endpoint).authentication(auth)
				.processor(new StringDelimitedProcessor(queue)).build();

		// Establish a connection
		client.connect();

		while (!client.isDone()) {
			String message = queue.take();
			System.out.println(message);
		}

		client.stop();

	}

	public void search() throws TwitterException {

		Twitter twitter = TwitterFactory.getSingleton();
		Query query = new Query("Cruyff");
		QueryResult result = twitter.search(query);
		for (Status status : result.getTweets()) {
			System.out.println("@" + status.getUser().getScreenName() + ":" + status.getText());
		}

	}

	private Properties getAccess() {

		Properties prop = new Properties();
		InputStream input = null;
		try {

			input = new FileInputStream("twitter.properties");
			prop.load(input);

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

		return prop;
	}

	public void run(String consumerKey, String consumerSecret, String token, String secret)
			throws InterruptedException {
		// Create an appropriately sized blocking queue
		BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);

		// Define our endpoint: By default, delimited=length is set (we need
		// this for our processor)
		// and stall warnings are on.
		StatusesSampleEndpoint endpoint = new StatusesSampleEndpoint();
		endpoint.stallWarnings(false);
		endpoint.languages(Lists.newArrayList("de"));

		Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);
		// Authentication auth = new
		// com.twitter.hbc.httpclient.auth.BasicAuth(username, password);

		// Create a new BasicClient. By default gzip is enabled.
		BasicClient client = new ClientBuilder().name("sampleExampleClient").hosts(Constants.STREAM_HOST)
				.endpoint(endpoint).authentication(auth).processor(new StringDelimitedProcessor(queue)).build();

		// Establish a connection
		client.connect();

		// Do whatever needs to be done with messages
		for (int msgRead = 0; msgRead < 1000; msgRead++) {
			if (client.isDone()) {
				System.out.println("Client connection closed unexpectedly: " + client.getExitEvent().getMessage());
				break;
			}

			String msg = queue.poll(5, TimeUnit.SECONDS);
			if (msg == null) {
				System.out.println("Did not receive a message in 5 seconds");
			} else {
				System.out.println(msg);
			}
		}

		client.stop();

		// Print some stats
		System.out.printf("The client read %d messages!\n", client.getStatsTracker().getNumMessages());
	}

}
