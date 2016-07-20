package ang.suite;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koeln.spinfo.ang.utils.IO;

public class Main {

	
	public static void main(String[] args) {
		if (args.length > 1 && args[0].equalsIgnoreCase("-batch")){
			String[] cmds = IO.readFile(args[1]).split("\n");
			for (String cmd : cmds){
				System.out.println("\n******************\n"
						+ "STARTING BATCH RUN\n"
						+ cmd + "\n"
						+ "******************\n\n");
				run(cmd.split(" "));
			}
		} else {
			run(args);
		}
	}
	
	
	private static void run(String[] args){
		DISCOWrapper disco = new DISCOWrapper();
		File resultsFile = null;
		Map<String, String> params = processParams(args);
		if (params == null) System.exit(0);
		
		try {
			resultsFile = disco.runAnalysis(
				(String)params.get("-word1"),		//wort
				(String)params.get("-word2"),		//wort
				//(String)params.get("-query"),		//Query-Ausdruck
				(String)params.get("-word"),		//TEMP: immer wort als query verwenden
				(String)params.get("-source"),					//quelle
				Integer.parseInt(params.get("-from")),			//jahr von
				Integer.parseInt(params.get("-to")),			//jahr bis
				Boolean.parseBoolean(params.get("-substrings")),	//substrings finden
				Integer.parseInt(params.get("-context")),		//kontext-größe in eine richtung
				Integer.parseInt(params.get("-mostfreq")),		//n häufigsten wörter einbeziehen
				Boolean.parseBoolean(params.get("-keepwordspace")),
				Boolean.parseBoolean(params.get("-stopwords")));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("\n>>> RESULTS: " + resultsFile.getAbsolutePath() + "\n\n");
	}
	
	
	private static Map<String, String> processParams(String[] args){
		Map<String, String> params = new HashMap<String, String>();
		//defaults
		params.put("-word1", "required");
		params.put("-word2", null);
		params.put("-query", null);
		params.put("-source", null);
		params.put("-from", "-1");
		params.put("-to", "-1");
		params.put("-context", "5");
		params.put("-mostfreq", "30000");
		params.put("-keepwordspace", "false");
		params.put("-substrings", "false");
		params.put("-stopwords", "true");
		
		for (Entry<String, String> e : params.entrySet()){
			boolean found = false;
			for (int i = 0; i < args.length; i++){
				if (args[i].startsWith("-") && i == args.length -1){
					argsError();
					return null;
				}
				
				if (args[i].equalsIgnoreCase(e.getKey())){
					found = true;
					params.put(e.getKey(), args[i+1]);
					i++;
					break;
				}
			}
			
			if (e.getValue() != null && e.getValue().equals("required") && !found){
				argsError();
				return null;
			}
		}
		return params;
	}
	
	
	private static void argsError(){
		System.err.println(
				"*********************************************************\n"
				+ "[ERROR]\tinvalid set of parameters! see usage directions:\n"
				+ "*********************************************************\n"
				+ "PARAMETER\t\tVALUE\n"
				+ "-batch [path to text file containing commands list]\n"
				+ "-word1\tmain word to analyze (required)\n"
				+ "-word2\tword to compare word1 to (optional)\n"
				+ "-source\tsource to build word-space from (optional, default = all sources)\n"
				//+ "-query\tquery expression to build more relevant word-space (optional, but recommended)\n"
				+ "-from\tfrom year (number) to limit used corpora to certain time range (optional)\n"
				+ "-to\tto year (number) to limit used corpora to certain time range (optional)\n"
				+ "-substrings\tfind word as substring, too (optional, default = false)\n"
				+ "-context\tcontext window size in one direction (number, optional. default = 5)\n"
				+ "-mostfreq\ttake n most frequent words into account (number, optional, default = 50000)\n"
				+ "-keepwordspace\t'true' or 'false' - decides whether word-space will be kept (optional, default = false)\n"
				+ "-stopwords\t'true' or 'false' - decides whether stopwords are excluded (optional, default = true)\n"
				);
		
	}
	

}
