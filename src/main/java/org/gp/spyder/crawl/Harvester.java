package org.gp.spyder.crawl;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.log4j.Logger;


public class Harvester {
	
	private static final String HTML_HTTP = "http";
	private final static Logger LOGGER = Logger.getLogger(Harvester.class.getName());
	
	public Harvester(){
		
	}
	
	public static String deriveFileFromUrl(String url){
		
		if (url.indexOf(HTML_HTTP) == -1){
			LOGGER.warn(String.format("Invalid URL: '%s'", url));
			
			return null;
		}
		
		int startIndex;
		String path;
		String directory;
		StringBuilder stringBuilder = new StringBuilder();
		
		startIndex = url.indexOf(HTML_HTTP);
		
		if (startIndex != -1){
			startIndex = startIndex + 6 + 1;
			
			path = url.substring(startIndex);
			startIndex = path.indexOf("/");
			path = path.substring(startIndex + 1);
			
			List<String> tokens = new ArrayList<String>(Arrays.asList(path.split("/")));
			
			String fileName = tokens.get(tokens.size() - 1);
			tokens.remove(tokens.size() - 1);
			
			for(String token: tokens){
				stringBuilder.append(token + "/");
			}
			
			directory = stringBuilder.toString();
			
			return directory + fileName;
		}else {
			return null;
		}
	}
	
	public static File downloadFile(String url, String filePath){
		
		File file = new File(filePath);
		URL webUrl = null;
		ReadableByteChannel readableByteChannel = null;
		FileOutputStream fileOutputStream = null;
		
		if (file.exists()){
			
			if (file.isFile()){
				LOGGER.info(String.format("File '%s' already exists", filePath));
				return file;
			}else if (file.isDirectory()){
				file.delete();
			}
		}	
		
		try {
			
			webUrl = new URL(url);
			HttpURLConnection httpConnection = (HttpURLConnection) webUrl.openConnection();
			httpConnection.addRequestProperty("User-Agent", "Mozilla/5.0");
			readableByteChannel = Channels.newChannel(httpConnection.getInputStream());
			fileOutputStream = new FileOutputStream(filePath);
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			
		}catch (MalformedURLException mue){
			mue.printStackTrace();
		}catch (FileNotFoundException fnfe){
			fnfe.printStackTrace();
		}catch (IOException ioe){
			ioe.printStackTrace();
		}finally {
			
			if (fileOutputStream != null){
				
				LOGGER.info(String.format("Downloaded file '%s'", filePath));
				
				try{
					fileOutputStream.close();
				}catch (IOException ioe){
					ioe.printStackTrace();
				}
			}
		}
		
		return file;
	}
	
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
