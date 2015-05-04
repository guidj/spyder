package org.verg.spyder.crawl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;


public class ContentGrabber {
	
	private final static Logger LOGGER = Logger.getLogger(ContentGrabber.class.getName());
	
	public static String crawl(String url){
		
		InputStream inputStream = null;
		BufferedReader bufferedReader;
		String line;
		StringBuilder stringBuilder = new StringBuilder();
		
		try {
			
			URL webUrl = new URL(url);
			inputStream = webUrl.openStream();
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));			
			
			while ((line = bufferedReader.readLine()) != null){
				stringBuilder.append(line);
			}
			
		} catch (MalformedURLException mue){
			
			LOGGER.error(mue.getStackTrace().toString());
			
		} catch (IOException ioe){
			LOGGER.error(ioe.getStackTrace().toString());
		} finally {
			
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ioe) {
				
			}
		}
		
		if (stringBuilder.length() > 0){
			return stringBuilder.toString();
		}else{
			return null;
		}		
	}	
}
