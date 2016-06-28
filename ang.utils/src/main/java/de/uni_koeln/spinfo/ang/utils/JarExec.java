package de.uni_koeln.spinfo.ang.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class JarExec {
	
	
	public static String runJar(String jarPath,
			File workingDir,
			String[] args,
			int memoryMb,
			boolean printErrors) {
		// scanner for output of external process
		Scanner scanner = null;
		Scanner scannerErr = null;
		StringBuilder sb = new StringBuilder();

		// prepare process commands
		String[] commands = new String[args.length + 5];
		commands[0] = "java";
		commands[1] = "-Xmx" + memoryMb + "m";
		commands[2] = "-Xms" + memoryMb + "m";
		commands[3] = "-jar";
		commands[4] = jarPath;
		for (int i = 5; i < commands.length; i++)
			commands[i] = args[i - 5];

		// prepare process object
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(workingDir);
		Process p = null;

		// start process, connect scanner
		try {
			p = pb.start();
			scanner = new Scanner(new BufferedInputStream(p.getInputStream()));
			scannerErr = new Scanner(new BufferedInputStream(p.getErrorStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// wait for process to terminate
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// get process output
		while (scanner.hasNext()) {
			sb.append(scanner.nextLine() + "\n");
		}
		scanner.close();

		// get error output
		if (printErrors){
			while (scannerErr.hasNext()) {
				System.err.println(scannerErr.nextLine());
			}
		}
		scannerErr.close();
		
		// return process output
		return sb.toString();
	}
	

}
