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
import org.gp.spyder.crawl.Harvester;
import org.gp.spyder.crawl.Parser;
import org.gp.spyder.domain.Image;
import org.gp.spyder.domain.WebPage;
import org.gp.spyder.repositories.ImageRepository;
import org.gp.spyder.repositories.LinkRepository;
import org.gp.spyder.repositories.WebPageRepository;

public class Application {

	private final static Logger LOGGER = Logger.getLogger(Application.class.getName());
	
	private final static String DOWNLOAD_DIR = "data";
	
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

//        webPageRepository = (WebPageRepository)context.getBean("webPageRepository");
//        imageRepository = (ImageRepository)context.getBean("imageRepository");
//        webPageLinkRepository = (WebPageLinkRepository)context.getBean("webPageLinkRepository");
        
        final String baseUrl = "http://www.mangareader.net";
        final BlockingQueue<String> dataQueue = new LinkedBlockingQueue<String>();
        final BlockingQueue<String> urlQueue = new LinkedBlockingQueue<String>();
        final BlockingQueue<String> imgQueue = new LinkedBlockingQueue<String>();

        urlQueue.add(baseUrl);
        
        Thread crawler = new Thread(){
        	public void run() {
        		try {

	        		while(true) {
	        			
						String url = urlQueue.take();
	
	            		String rawText = Harvester.crawl(url);
	            		
	            		if (rawText != null) {
	            			dataQueue.put(rawText);
	            			System.out.println("Data queue size: " + dataQueue.size());
	            		}
	            		
						Thread.sleep(1000);

	        		}
        		}catch (InterruptedException e) {
        			System.out.println(e);
        		}
        	}
        };
        
        Thread miner = new Thread() {
        	public void run() {
        		
        		try {
	        		
        			Parser parser = new Parser(baseUrl);
        			
	        		while(true) {
	        			String rawText = dataQueue.take();
	        			List<String> urls = parser.extractUrls(rawText);
	        			List<String> imgUrls = parser.extractImageUrls(rawText);
	        			
	        			for(String url: urls) {
	        				//TODO: db check
	        				urlQueue.put(url);
	        			}
	        			
	        			for(String imgUrl: imgUrls) {
	        				//TODO: db check
	        				imgQueue.put(imgUrl);
	        			}
	        			
	        			Thread.sleep(1000);
	        		}
	        		
        		}catch (InterruptedException e) {
    	        	System.out.println(e);
        		} 
        	}
        };
        
        Thread harvester = new Thread() {
        	public void run() {
        		
        		try {
	        		
	        		while(true) {
	        			
						String imgUrl = imgQueue.take();
						
						String filename = Harvester.deriveFileFromUrl(imgUrl);
						
		    			if (filename != null){
							String filePath = DOWNLOAD_DIR + "/" + filename;
		    				
		    				String parentDir = filePath.substring(0, filePath.lastIndexOf("/"));
		    				(new File(parentDir)).mkdirs();
		    			
			    			File file = Harvester.downloadFile(imgUrl, filePath);
			    			
			    			if (file.exists()){
//			    				image.setDownloded(true);
//			    				imageRepository.save(image);
			    				
			    				LOGGER.info(String.format("Downloaded image '%s'", file.getPath()));
			    			}else{
			    				LOGGER.error(String.format("Failed to download file '%s'", file.getPath()));
			    			}
		    			}						

						Thread.sleep(1000);
	        		}
	        		
        		}catch (InterruptedException e) {
    	        	System.out.println(e);
        		} 
        	}
        };        
        
        crawler.start();
        miner.start();
        harvester.start();
		
		LOGGER.info("Exiting");
    }
}

//http://crunchify.com/how-to-run-multiple-threads-concurrently-in-java-executorservice-approach/
