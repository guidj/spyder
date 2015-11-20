package org.gp.spyder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.apache.log4j.Logger;
import org.gp.spyder.crawl.Crawler;
import org.gp.spyder.crawl.Harvester;
import org.gp.spyder.crawl.Parser;
import org.gp.spyder.domain.Image;
import org.gp.spyder.domain.WebPage;
import org.gp.spyder.repositories.ImageRepository;
import org.gp.spyder.repositories.LinkRepository;
import org.gp.spyder.repositories.WebPageRepository;

public class App {

	private final static Logger LOGGER = Logger.getLogger(App.class.getName());
	
	private final static String DOWNLOAD_DIR = "data";
//	
//	@Autowired
//    private WebPageRepository webPageRepository;
//    
//    @Autowired
//    private static ImageRepository imageRepository;
    
    private static void recursiveDelete(File file) {
        //to end the recursive loop
        if (!file.exists())
            return;
         
        //if directory, go inside and call recursively
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                //call recursively
                recursiveDelete(f);
            }
        }
        //call delete to delete files and empty directory
        file.delete();
    }
    
    private static void deleteDatabase(){
    	File file = new File("target/neo4j-db-plain");
    	
    	recursiveDelete(file);
    }
    
    public static void main(String[] args) {
    	
//    	deleteDatabase();   	
        @SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("spring/context.xml");

        final String baseUrl = "http://www.mangareader.net";
        final BlockingQueue<String> dataQueue = new LinkedBlockingQueue<String>();
        final BlockingQueue<String> urlQueue = new LinkedBlockingQueue<String>();
        final BlockingQueue<String> imgQueue = new LinkedBlockingQueue<String>();

        urlQueue.add(baseUrl);
        
        DBContext dbContext = new DBContext(
        		(WebPageRepository)context.getBean("webPageRepository"),
        		(ImageRepository)context.getBean("imageRepository")
        		);
        
        for (WebPage webPage: dbContext.getWebPageRepository().getWebPages(false, 100)) {
        	urlQueue.add(webPage.getUrl());
        }
        
        Thread crawler = new Crawler(dbContext, urlQueue, dataQueue);
        Thread parser = new Parser(dbContext, baseUrl, urlQueue, dataQueue, imgQueue);
        Thread harvester = new Harvester(dbContext, DOWNLOAD_DIR, imgQueue);      
        
        LOGGER.info(String.format("Starting with URL Queue of with %d entries", urlQueue.size()));
        LOGGER.info("Starting Crawler, Parser and Harvester workers...");
        
        crawler.start();
        parser.start();
        harvester.start();
        
        //TODO: when to stop?
    }
}
