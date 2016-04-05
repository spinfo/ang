package ang.ui;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFileLocation;
import static spark.Spark.stop;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

import com.mongodb.client.FindIterable;

import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;



public class WebApp implements spark.servlet.SparkApplication {
	
	private MongoWrapper mongo;
	
	
	@Override
	public void init() {
		this.mongo = new MongoWrapper();
		Properties props = loadProperties("db.properties");
		mongo.init(props.getProperty("user"),	//USER
				props.getProperty("pw"),		//PASS
				props.getProperty("db"),		//DB
				props.getProperty("host"),		//HOST
				props.getProperty("port"),		//PORT
				props.getProperty("collection"));//COLLECTION
		
		mapRoutes();
	}
	
	
	public void mapRoutes(){
		staticFileLocation("/spark/template/freemarker");
		
		//set port
		port(8080);
		
		// MAP /search
		get("/search", (request, response) -> {
			
			//get params
			boolean regex = request.queryParams("regex") != null;
			boolean casesens = request.queryParams("casesens") != null;
			boolean useyear = request.queryParams("useyear") != null;
			String yearfrom = request.queryParams("yearfrom");
			String yearto = request.queryParams("yearto");
			String query = request.queryParams("query");
			String source = request.queryParams("source");
			String lengthlimit = request.queryParams("lengthlimit");
			
			//defaults
			source = (source == null ? "" : source);
			query = (query == null ? "" : query);
			lengthlimit = (lengthlimit == null ? "500" : lengthlimit);
			yearfrom = (yearfrom == null ? "1516" : yearfrom);
			yearto = (yearto == null ? "2020" : yearto);
			
			//create model
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("query", query);
			model.put("source", source);
			model.put("regex", regex);
			model.put("casesens", casesens);
			model.put("useyear", useyear);
			model.put("yearfrom", yearfrom);
			model.put("yearto", yearto);
			model.put("lengthlimit", lengthlimit);
			
			System.out.println("[MODEL]\t" + model);


			
			List<DBData> data = new ArrayList<DBData>();
			
			//BLOCK FOR TESTING PURPOSES ONLY
//			Pattern pTemp = Pattern.compile("twitter", Pattern.CASE_INSENSITIVE);
//			data.add(new DBData("twitter", "Ich \"twitter\" das jetzt so!", findMatch("Ich twitter das jetzt so!", pTemp)));
//			data.add(new DBData("dings", "Hahaha twitter! ich werd bekloppt!", findMatch("Hahaha twitter! ich werd bekloppt!", pTemp)));
//			data.add(new DBData("quelle", "Och nööööö! Nicht Twitter!", findMatch("Och nööööö! Nicht Twitter!", pTemp)));
//			model.put("results", data);
			
			
			if (query != null){
				Pattern pattern = casesens ?
						Pattern.compile(query)
						: Pattern.compile(query, Pattern.CASE_INSENSITIVE);
				FindIterable<Document> results = mongo.getSearchResults(
						query,
						source,
						regex,
						casesens,
						useyear,
						Integer.parseInt(yearfrom),
						Integer.parseInt(yearto));
				if (results != null){
					for (Document doc : results){
						String text = doc.getString("text");
						String match = findMatch(doc.getString("text"), pattern);
						text = trimText(text, match, Integer.parseInt(lengthlimit));
						
						data.add(new DBData(doc.getString("source"), text, match));
					}
					model.put("results", data);
				}
			}
			
			return new ModelAndView(model, "index.ftl");
        }, new FreeMarkerEngine());
		
		//MAP /search/
		get("/search/", (request, response) -> {
			response.redirect("/search");
		    return null;
		});
		
		//MAP /
		get("/", (request, response) -> {
			response.redirect("/search");
		    return null;
		});
		
		//MAP /stopang
		get("/stopang", (request, response) -> {
			exit();
		    return null;
		});
	}
	
	
	public void exit(){
		mongo.close();
		stop();
	}
	
	
	private String findMatch(String text, Pattern pattern){
		if (text == null || text.length() == 0) return "";
		Matcher m = pattern.matcher(text);
		if (!m.find()){
			return "";
		}
		return m.group(0);
	}
	
	
	private Properties loadProperties(String propertiesFileName){
		Properties properties = new Properties();
		BufferedInputStream stream;
		
		try {
			stream = new BufferedInputStream(
					getClass().getResourceAsStream(propertiesFileName));
			properties.load(stream);
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return properties;
	}
	
	
	private String trimText(String text, String match, int length){
		if (text.length() > length + 10) {
			int find = text.indexOf(match);
			if (find == -1) return text;
			int start = find - (length/2);
			int end = find + (length/2);
			start = start < 0 ? 0 : start;
			end = end > (text.length() - 1) ? (text.length() - 1) : end;
			text = "[...] " + text.substring(start, end) + " [...]";
		}
		return text;
	}
	
	
	public class DBData {
		private String source;
		private String text;
		private String match;
		
		public DBData(String source, String text, String match) {
			super();
			this.source = (source == null ? "" : source);
			this.text = (text == null ? "" : text);
			this.match = (match == null ? "" : match);
		}

		public String getSource() {
			return source;
		}

		public String getText() {
			return text;
		}
		
		public String getMatch(){
			return match;
		}
		
	}


}
