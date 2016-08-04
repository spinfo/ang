package ang.analyzer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.uni_koeln.spinfo.ang.utils.IO;

public class Main {
	
	public static void main(String[] args) {
		if (args.length > 0 && args[0].equalsIgnoreCase("-help")){
			argsError();
		} else if (args.length > 1 && args[0].equalsIgnoreCase("-batch")){
			String[] cmds = IO.readFile(args[1]).split("\n");
			//go through all the batch commands
			for (String cmd : cmds){
				cmd = cmd.trim();
				//filter invalid lines
				if (cmd.length() < 5
						|| !cmd.startsWith("-")
						|| cmd.startsWith("#")) continue;
				System.out.println("\n******************\n"
						+ "STARTING BATCH RUN\n"
						+ cmd + "\n"
						+ "******************\n\n");
				//call program with current command
				run(cmd.split(" "));
			}
		} else {
			//if no "-batch" command, just pass the command to program
			run(args);
		}
	}
	
	/*
	 * process commands and run analysis
	 */
	private static void run(String[] args){
		Analyzer analyzer = new Analyzer();
		String resultsPath = null;
		Map<String, String> params = processParams(args);
		if (params == null) System.exit(0);
		
		//create analysis profile
		AnalysisProfile profile = new AnalysisProfile();
		profile.setTerms(params.get("terms").split("\\,"));
		profile.setSources(params.get("sources").split("\\,"));
		profile.setYearFrom(Integer.parseInt(params.get("from")));
		profile.setYearTo(Integer.parseInt(params.get("to")));
		if (params.get("stopwords") != null)
			profile.setUseStopwords(Boolean.parseBoolean(params.get("stopwords")));
		if (params.get("compounds") != null)
			profile.setUseCompounds(Boolean.parseBoolean(params.get("compounds")));
		if (params.get("context") != null)
			profile.setContextSize(Integer.parseInt(params.get("context")));
		
		try {
			resultsPath = analyzer.runAnalysis(profile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("\n>>> RESULTS: " + resultsPath + "\n\n");
	}
	
	/*
	 * process parameters and return commands map
	 */
	private static Map<String, String> processParams(String[] args){
		Map<String, String> params = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++){
			if (args[i].startsWith("-")){
				if (args.length > i+1 && !args[i+1].startsWith("-")){
					params.put(args[i].replaceFirst("\\-", ""), args[i+1]);
				} else {
					params.put(args[i].replaceFirst("\\-", ""), "true");
				}
			}
		}
		return params;
	}
	
	/*
	 * print commands help table
	 */
	private static void argsError(){
		System.err.println(
				"*********************************************************\n"
				+ "[ERROR]\tinvalid set of parameters! see usage directions:\n"
				+ "*********************************************************\n"
				+ "PARAMETER\t\tVALUE\n"
				+ "-batch [path to text file containing commands list]\n"
				+ "-terms\tterms to analyze (separated by ',' without whitespaces)\n"
				+ "-sources\tsources to build word-space from (separated by ',' without whitespaces)\n"
				+ "-from\tfrom year (number) to limit used corpora to certain time range (optional)\n"
				+ "-to\tto year (number) to limit used corpora to certain time range (optional)\n"
				+ "-compounds\tfind word as substring, too (optional, default = false)\n"
				+ "-context\tcontext window size in one direction (number, optional. default = 5)\n"
				+ "-stopwords\tdecides whether stopwords are excluded (optional, default = true)\n"
				+ "-help\tshows this help table\n"
				);
	}
	

}
