package ang.ui;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFileLocation;
import static spark.Spark.stop;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
			boolean casesens = request.queryParams("casesens") != null;
			boolean useyear = request.queryParams("useyear") != null;
			String yearfrom = request.queryParams("yearfrom");
			String yearto = request.queryParams("yearto");
			String query = request.queryParams("query");
			String source = request.queryParams("source");
			String lengthlimit = request.queryParams("lengthlimit");
			String maxdistance = request.queryParams("maxdistance");
			
			//defaults
			source = (source == null ? "" : source);
			query = (query == null ? "" : query);
			lengthlimit = (lengthlimit == null ? "200" : lengthlimit);
			yearfrom = (yearfrom == null ? "1516" : yearfrom);
			yearto = (yearto == null ? "2020" : yearto);
			maxdistance = (maxdistance == null ? "100" : maxdistance);
			
			//create model
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("query", query);
			model.put("source", source);
			model.put("casesens", casesens);
			model.put("useyear", useyear);
			model.put("yearfrom", yearfrom);
			model.put("yearto", yearto);
			model.put("lengthlimit", lengthlimit);
			model.put("maxdistance", maxdistance);
			
			List<DBData> data = new ArrayList<DBData>();
			
			//BLOCK FOR TESTING PURPOSES ONLY
//			Pattern pTemp = Pattern.compile("twitter", Pattern.CASE_INSENSITIVE);
//			data.add(new DBData("twitter", "Ich \"twitter\" das jetzt so!", findMatch("Ich twitter das jetzt so!", pTemp)));
//			data.add(new DBData("dings", "Hahaha twitter! ich werd bekloppt!", findMatch("Hahaha twitter! ich werd bekloppt!", pTemp)));
//			data.add(new DBData("quelle", "Och nööööö! Nicht Twitter!", findMatch("Och nööööö! Nicht Twitter!", pTemp)));
//			model.put("results", data);
			
			
			if (query != null){
				String[] queries = splitQuery(query);
				model.put("queries", queries);
				Pattern[] patterns = generatePatterns(queries, casesens);
				FindIterable<Document> results = mongo.getSearchResults(
						query,
						source,
						casesens,
						useyear,
						Integer.parseInt(yearfrom),
						Integer.parseInt(yearto));
				if (results != null){
					for (Document doc : results){
						String text = doc.getString("text");
						String[] matches = findMatches(doc.getString("text"), patterns);
						text = trimText(
								text,
								matches,
								Integer.parseInt(lengthlimit),
								Integer.parseInt(maxdistance));
						if (text == null) continue;
						
						data.add(new DBData(doc.getString("source"), text));
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
	
	
	public Pattern[] generatePatterns(String[] queries, boolean casesens){
		List<Pattern> patterns = new ArrayList<Pattern>();
		for (String q : queries){
			Pattern p = casesens ?
					Pattern.compile(q)
					: Pattern.compile(q, Pattern.CASE_INSENSITIVE);
			patterns.add(p);
		}
		return patterns.toArray(new Pattern[patterns.size()]);
	}
	
	
	private String[] splitQuery(String query){
		return query.replaceAll("[^\\p{L}\\s]", "").split("\\s");
	}
	
	
	private String[] findMatches(String text, Pattern[] patterns){
		if (text == null || text.length() == 0) return new String[0];
		List<String> matches = new ArrayList<String>();
		for (Pattern p : patterns){
			Matcher m = p.matcher(text);
			if (m.find()){
				matches.add(m.group(0));
			}
		}
		return matches.toArray(new String[matches.size()]);
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
	
	
	private String trimText(String text,
			String[] matches,
			int length,
			int maxdistance){
		
		int min = text.length()-1;
		int max = 0;
		int minLength = 0;
		
		//find first and last match boundaries
		for (String m : matches){
			int i = text.indexOf(m);
			if (i != -1){
				if (i < min){
					min = i;
					minLength = m.length();
				}
				if (i > max){
					max = i;
				}
			}
		}
		
		//return null if distance too long
		if (max - min - minLength > maxdistance) return null;
		
		//trim text if needed
		if (text.length() > (length*2) + (max-min)) {
			int start = min - length;
			int end = max + length;
			start = start < 0 ? 0 : start;
			end = end > (text.length() - 1) ? (text.length() - 1) : end;
			text = "[...] " + text.substring(start, end) + " [...]";
		}
		
		return text;
	}
	
	
	public class DBData {
		private String source;
		private String text;
		
		public DBData(String source, String text) {
			super();
			this.source = (source == null ? "" : source);
			this.text = (text == null ? "" : text);
		}

		public String getSource() {
			return source;
		}

		public String getText() {
			return text;
		}
		
	}


}
