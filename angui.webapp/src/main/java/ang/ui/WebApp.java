package ang.ui;

import static spark.Spark.get;
//import static spark.Spark.port;
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
		//port(8080);
		
		// MAP /search
		get("/search", (request, response) -> {
			//get params
			boolean casesens = request.queryParams("casesens") != null;
			boolean useyear = request.queryParams("useyear") != null;
			boolean substrings = request.queryParams("substrings") != null;
			String yearfrom = request.queryParams("yearfrom");
			String yearto = request.queryParams("yearto");
			String query = request.queryParams("query");
			String source = request.queryParams("source");
			String lengthlimit = request.queryParams("lengthlimit");
			String maxdistance = request.queryParams("maxdistance");
			String limitresults = request.queryParams("limitresults");
			
			//defaults
			source = (source == null ? "" : source);
			query = (query == null ? "" : query);
			lengthlimit = (lengthlimit == null ? "200" : lengthlimit);
			yearfrom = (yearfrom == null ? "1516" : yearfrom);
			yearto = (yearto == null ? "2020" : yearto);
			maxdistance = (maxdistance == null ? "100" : maxdistance);
			limitresults = (limitresults == null ? "1000" : limitresults);
			
			//create model
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("query", query);
			model.put("source", source);
			model.put("casesens", casesens);
			model.put("substrings", substrings);
			model.put("useyear", useyear);
			model.put("yearfrom", yearfrom);
			model.put("yearto", yearto);
			model.put("lengthlimit", lengthlimit);
			model.put("maxdistance", maxdistance);
			model.put("limitresults", limitresults);
			
			List<DBData> data = new ArrayList<DBData>();
			
			//process query
			if (query != null){
				System.out.println("\n=============\nQUERY: " + query);
				boolean findAllQueries = query.contains("\"");
				
				//split queries
				String[] queries = splitQuery(query);
				System.out.println("SPLIT QUERIES: " + Arrays.toString(queries));
				model.put("queries", queries);
				
				//process possible wildcards via regex
				if (substrings){
					query = parseRegexQuery(query);
					System.out.println("QUERY REGEX: " + query);
				}
				
				Pattern[] patterns = generatePatterns(
						substrings ? new String[]{query} : queries, casesens);
				System.out.println("PATTERNS: " + Arrays.toString(patterns) + "\n=============\n");
				
				FindIterable<Document> results = mongo.getSearchResults(
						query,
						source,
						casesens,
						substrings,
						useyear,
						Integer.parseInt(yearfrom),
						Integer.parseInt(yearto),
						Integer.parseInt(limitresults));
				
				if (results != null){
					for (Document doc : results){
						String text = doc.getString("text");
						
						//skip if one word is missing
						boolean skip = false;
						if (findAllQueries)
							for(String q : queries)
								if (!text.toLowerCase().matches(".*(?i)" + q.toLowerCase() + ".*"))
									skip = true;
						if (skip) continue;
						
						String[] matches = findMatches(doc.getString("text"), patterns);
						text = trimText(
								text,
								matches,
								Integer.parseInt(lengthlimit),
								Integer.parseInt(maxdistance));
						if (text == null) continue;
						
						data.add(new DBData(
								doc.getString("source"),
								text,
								doc.getInteger("date_year", 0),
								doc.getInteger("date_month", 0)));
					}
					model.put("results", data);
					model.put("resultscount", data.size());
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
		    return "von wegen.";
		});
		
	}
	
	
	public void exit(){
		mongo.close();
		stop();
	}
	
	
	private String parseRegexQuery(String query){
		query = query.replaceAll("\\b(?=\\w)", "*")
				.replaceAll("(?<=\\w)\\b", "*");
		
		//extract single words and generate options
		String[] words = splitQuery(query);
		String opt = "";
		for (String s : words) opt += s + "|";
		opt = "(" + opt.substring(0, opt.length()-1) + ")";
		
		return query.replaceAll("\\*", "%")
			.replaceAll("\\%(?=\\w)", "\\\\b\\\\w*")
			.replaceAll("(?<=\\w)\\%", "\\\\w*\\\\b")
			.replaceAll("\\\"\\s\\\"", ".+")
			.replaceAll("\\\"", "")
			.replaceAll(opt, opt);
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
		return query.replaceAll("\\.\\+", " ").replaceAll("\\\\\\w", "")
				.replaceAll("[^\\p{L}\\s]", "").split("\\s");
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
		private String year;
		private String month;
		
		public DBData(String source, String text, int year, int month) {
			super();
			this.source = (source == null ? "" : source);
			this.text = (text == null ? "" : text);
			this.year = year <= 0 ? null : year+"";
			this.month = month <= 0 ? null : month+"";
		}

		public String getSource() {
			return source;
		}

		public String getText() {
			return text;
		}
		
		public String getYear(){
			return year;
		}
		
		public String getMonth(){
			return month;
		}
		
	}


}
