package de.uni_koeln.spinfo.ang.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;


public class IO {
	
	public static String readFile(String path){
		return readFile(path, null);
	}
	
	
	public static String readFile(String path, Charset charSet){
		StringBuilder sb = new StringBuilder();

		try {
			BufferedReader in =
					charSet == null
					? getFileReader(path)
					: getFileReader(path, charSet);
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
	
	public static BufferedReader getFileReader(String filePath, Charset charset){
		try {
			return new BufferedReader(
					new InputStreamReader(
							   new FileInputStream(
									   new File(filePath)), charset));
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
	
	
	public static List<File> getAllFiles(String directoryPath, String fileNamePattern){
		List<File> files = new ArrayList<File>();
		File dir = new File(directoryPath);
		
		if (!dir.exists()){
			System.out.println("[IOERR]\t" + directoryPath + " doesn't exist!");
			return files;
		}
		
		if (!dir.isDirectory()){
			if (dir.isFile()) files.add(dir);
			return files;
		}
		
        return getAllFiles(files, dir, fileNamePattern);
	}
	
	
	private static List<File> getAllFiles(List<File> files, File dir, String fileNamePattern){
        File[] list = dir.listFiles();

        if (list == null) return files;

        for (File f : list) {
            if (f.isDirectory()) {
                files = getAllFiles(files, f, fileNamePattern);
            } else {
                if (fileNamePattern == null || fileNamePattern.length() == 0)
                	files.add(f);
                else if (f.getName().matches(fileNamePattern))
                	files.add(f);
            }
        }
        return files;
	}
	
	
	public static Properties loadProperties(String propertiesFileName, Class referenceClass){
		Properties properties = new Properties();
		BufferedInputStream stream;
		
		try {
			stream = new BufferedInputStream(
					referenceClass.getResourceAsStream(propertiesFileName));
			properties.load(stream);
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return properties;
	}
	
	
	public static void deleteFolder(File folder) {
		if (folder == null || !folder.exists() || !folder.isDirectory())
			return;
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}
	
	
	public static long folderSize(Path path) {
		final AtomicLong size = new AtomicLong(0);

		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					size.addAndGet(attrs.size());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					System.out.println("skipped: " + file + " (" + exc + ")");
					// Skip folders that can't be traversed
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
					if (exc != null)
						System.out.println("had trouble traversing: " + dir + " (" + exc + ")");
					// Ignore errors traversing a folder
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
		}

		return size.get();
	}
	
	
	public static void writeStringToFile(String string, String filePath) {
		try {
			OutputStreamWriter fw = getFileWriter(filePath);
			fw.write(string);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
