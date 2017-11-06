package de.uni_koeln.spinfo.ang.vectors;
import java.io.File;
import java.io.IOException;

import de.bkis.climate.Climate;

public class Main {

	public static void main(String[] args) {
		Climate cli = new Climate("-");
		cli.addArgument("csv");
		
		for (int i = 0; i < args.length; i++) {
			if (cli.hasArgument(args[i].replaceAll("-", ""))){
				cli.addArgument(args[i].replaceAll("-", ""), args[i+1]);
				i++;
			} else {
				System.err.println("DA LÄUFT WAS SCHIEF!");
			}
		}
		
		AngAnalysisVectors aav = new AngAnalysisVectors();
		aav.analyze(new File(cli.getValue("csv")), ";", true);
	}

}
