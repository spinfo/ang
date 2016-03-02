package de.uni_koeln.spinfo.ang.newsarchive;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class App 
{
    public static void main( String[] args ) throws IOException, ParserConfigurationException, SAXException
    {
    	Crawler crawler = new Crawler();
    	crawler.parser("zeit");
    
    }
}
