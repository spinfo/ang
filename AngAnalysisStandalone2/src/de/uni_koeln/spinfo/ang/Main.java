package de.uni_koeln.spinfo.ang;
import java.io.IOException;

import de.bkis.climate.Climate;

public class Main {

	public static void main(String[] args) {
		Climate cli = new Climate("-");
		cli.addArgument("termsFile");
		cli.addArgument("typesFile");
		cli.addArgument("corpusDir");
		cli.addArgument("matrixFile");
		cli.addArgument("nMostFreqDimensions");
		cli.addArgument("nMostFreqTerms");
		
		for (int i = 0; i < args.length; i++) {
			if (cli.hasArgument(args[i].replaceAll("-", ""))){
				cli.addArgument(args[i].replaceAll("-", ""), args[i+1]);
				i++;
			} else {
				System.err.println("DA LÄUFT WAS SCHIEF!");
			}
		}
		
		AngAnalysis ang = new AngAnalysis();
		try {
			ang.analyze(cli.getValue("termsFile"),
					cli.getValue("typesFile"),
					cli.getValue("corpusDir"),
					cli.getValue("matrixFile"),
					cli.getValue("nMostFreqDimensions"),
					cli.getValue("nMostFreqTerms"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
