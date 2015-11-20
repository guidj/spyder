package org.gp.spyder.test.crawl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gp.spyder.crawl.Parser;
import org.junit.Test;


public class ParserTest {

	
	@Test
	public void shouldIndetifyValidUrls(){
		
		List<String> urls = new ArrayList<String>(
				Arrays.asList(
						"http://google.com",
						"cnn.edition.com",
						"cnn.edition.com/world",
						"cnn.edition.com/world?id=aXyre",
						"192.34.35.65",
						"https://192.34.35.65/greetings"));
		
		for(String url: urls){
			assertTrue(Parser.isUrlValid(url));
		}
		
	}
	
	@Test
	public void shouldIdentifyInvalidUrls(){
		
		List<String> urls = new ArrayList<String>(
				Arrays.asList(
						"myhouse",
						"living_in_oasis",
						"htp://some.domain.com",
						"java://some.domain.com"));
		
		for(String url: urls){
			assertFalse(Parser.isUrlValid(url));
		}		
	}
	
}
