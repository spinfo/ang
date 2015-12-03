package ang.corpusreader;

import java.io.File;

/**
 * Hello world!
 *
 */
public class App 
{
	
	private static String inputPath = "/Export/";
    public static void main( String[] args )
    {
		File f = new File(inputPath);
		File[] subFiles = f.listFiles();
		for (File file : subFiles) {
			if (file.isFile()) {
				System.out.println(file.getName());
			}
		}
	
    }
}
