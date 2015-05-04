package org.verg.spyder.crawl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

public class Parser {
	
	private static final String HTML_HREF = "href";
	private static final String HTML_HTTP = "http://";
	private static final String HTML_IMG = "<img";
	private static final String HTML_IMG_SRC = "src";
	
	private String baseUrl;
	
	public Parser(String baseUrl){
		this.baseUrl = baseUrl;
	}
		
	public Parser(){
		
	}
	
	public static boolean isUrlValid(String url){
		
		String pattern = "(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http[s]?://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?";
		
		Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		
		Matcher matcher = regex.matcher(url);
		
		return matcher.matches();
	}
	
	public List<String> extractUrls(String text){
		
		List<String> urls = new ArrayList<String>();

		int beginning;
		int index;
		int openingTagIndex;
		int closingTagIndex;
		char openingTag;
		String url;
		
		beginning = 0;
		index = text.indexOf(HTML_HREF, beginning);
		
		while (index != -1){
			
			openingTagIndex = index + 5;
			openingTag = text.charAt(openingTagIndex);
			closingTagIndex = text.indexOf(openingTag, openingTagIndex + 1);
			
			url = text.substring(openingTagIndex + 1, closingTagIndex);
			
			if (url.indexOf(HTML_HTTP) == -1){
				url = this.baseUrl + url;
			}
			
			if (isUrlValid(url)) {
				if (url.contains("mangareader") && url.contains("naruto")) {
					urls.add(url);
				}
				
			}
			
			beginning = closingTagIndex + 1;
			index = text.indexOf(HTML_HREF, beginning);
		}
		
		return urls;	
	}
	
	public List<String> extractImageUrls(String text){
		
		List<String> urls = new ArrayList<String>();

		int beginning;
		int index;
		int openingTagIndex;
		int closingTagIndex;
		int sourceIndex;
		char openingTag;
		String url;
		
		
		beginning = 0;
		index = text.indexOf(HTML_IMG, beginning);
		
		while (index != -1){
			sourceIndex = text.indexOf(HTML_IMG_SRC, index);
			
			openingTagIndex = sourceIndex + 4;
			openingTag = text.charAt(openingTagIndex);
			closingTagIndex = text.indexOf(openingTag, openingTagIndex + 1);
			
			url = text.substring(openingTagIndex + 1, closingTagIndex);
			
			if (isUrlValid(url)) {
				if (url.contains("naruto")) {
					urls.add(url);
				}				
			}
			
			beginning = closingTagIndex + 1;
			index = text.indexOf(HTML_IMG, beginning);
		}
		
		return urls;	
	}	
}
