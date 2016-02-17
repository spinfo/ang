package de.uni_koeln.spinfo.ang.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class IO {
	
	
	public static String readFile(String path){
		StringBuilder sb = new StringBuilder();

		try {
			BufferedReader in = getFileReader(path);
			String str;
			      
			while ((str = in.readLine()) != null) {
			    sb.append(str);
			    sb.append("\n");
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	
	public static BufferedReader getFileReader(String filePath){
		try {
			return new BufferedReader(
					new InputStreamReader(
							   new FileInputStream(
									   new File(filePath)), StandardCharsets.UTF_8));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static OutputStreamWriter getFileWriter(String filePath){
		File f = new File(filePath);
		try {
			return new OutputStreamWriter(
					   new BufferedOutputStream(
						   new FileOutputStream(f)), StandardCharsets.UTF_8);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static boolean deleteFile(String path){
		File f = new File(path);
		if (!f.exists() || !f.canWrite() || !f.delete()){
			System.err.println("[IO]\tcannot delete file: " + path);
			return false;
		}
		return true;
	}

	
}
