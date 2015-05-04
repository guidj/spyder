package org.verg.spyder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
//import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.apache.log4j.Logger;

import org.verg.spyder.crawl.ContentGrabber;
import org.verg.spyder.crawl.Harvester;
import org.verg.spyder.crawl.Parser;
import org.verg.spyder.domain.Image;
import org.verg.spyder.domain.WebPage;
import org.verg.spyder.services.ImageService;
import org.verg.spyder.services.WebPageService;

public class Application {

	private final static Logger LOGGER = Logger.getLogger(Application.class.getName());
	
	private final static String DOWNLOAD_DIR = "data";
	
	@Autowired
    private static WebPageService webPageService;
    
    @Autowired
    private static ImageService imageService;
   
	private Parser parser;
    
    private String baseUrl;
    
    public Application(){
    	
    }
    
    public Application(String baseUrl){
    	this.initialize(baseUrl);
    }
    
    private void initialize(String baseUrl){
    	
    	this.baseUrl = baseUrl;
    	this.parser = new Parser(this.baseUrl);
    	
    	this.seed();   	
    	
    }
     
    private void seed(){
    	if (this.baseUrl != null){
			WebPage wp = webPageService.findWebPageByUrl(this.baseUrl);
			
			if (wp == null){
				
				webPageService.createWebPage(this.baseUrl, false, -1);
				
				LOGGER.info(String.format("Seeded Start WebPage '%s'", this.baseUrl));	
				
			}else {
				
				LOGGER.info(String.format("Start WebPage '%s' already exists", this.baseUrl));
				
			}
    	}
    }
    
    private void crawl(){
    	
    	boolean stop;
    	
    	do {
    		
    		stop = true;
    		
    		Iterable<WebPage> webPages = webPageService.getUnprocessedWebPages();
    		
    		Iterator<WebPage> iterator = webPages.iterator();
    		
    		while(iterator.hasNext()){
    			
    			WebPage webPage = iterator.next();
    			
    			stop = false;
    			
    			//get pages
    			String text = ContentGrabber.crawl(webPage.getUrl());
    			
    			if(text != null){

	    			Set<String> urls = new HashSet<String>(this.parser.extractUrls(text));
	    			Set<String> imageUrls = new HashSet<String>(this.parser.extractImageUrls(text));
	    			
	    			LOGGER.info(String.format("Extracted [%d] URLs and [%d] images from [%s]", urls.size(), imageUrls.size(), webPage.getUrl()));
	    			
	    			for(String url: urls){
	
	    				WebPage wp = webPageService.findWebPageByUrl(url);
	    				
	    				if (wp == null){
	    					
	    					try {
	    						wp = webPageService.createWebPage(url, false, -1);
	    						webPage.addLinkedWebPage(wp);
	    						//LOGGER.info(String.format("Added WebPage '%s'", url));	
	    						
	    					}catch (DataIntegrityViolationException exc){
	    						LOGGER.info(String.format("WebPage {%s} already exists", url));
	    					}
	    					    					
	    				}else{
	    					
	    				}
	    			}
	    			
	    			for(String imageUrl: imageUrls){
	
	    				Image img = imageService.findImageByUrl(imageUrl);
	    				
	    				if (img == null){
	    					
	    					try{
		    					img = imageService.createImage(imageUrl, false);
		    					webPage.addLinkedImage(img);
		    					//LOGGER.info(String.format("Added Image '%s'", imageUrl));
	    					}catch (DataIntegrityViolationException exc){
	    						LOGGER.info(String.format("Image {%s} already exists", imageUrl));
	    					}
	    					
	    				}else{
	    					
	    				}
	    			}
	    			
	    			webPage.setCrawled(true);
	    			webPage.setStatus(200);
    			}else{        			
    				webPage.setStatus(404);
    			}
    			
    			webPageService.save(webPage);
    			
    			LOGGER.info(String.format("Crawled '%s'", webPage.getUrl()));    			
    		}
    		
    	} while (stop == false);
    	
    	return;
    }
    
    private void downloadImages(){
    	
    	while(true){
    		
    		Iterable<Image> images = imageService.getUnprocessedImages();
    		
    		Iterator<Image> iterator = images.iterator();
    		
    		if (iterator.hasNext() == false){
    			break;
    		}    		
    		
    		while(iterator.hasNext()){
    			
    			Image image = iterator.next();
    			
    			String filePath = DOWNLOAD_DIR + "/" + Harvester.deriveFileFromUrl(image.getUrl());
    			
    			if (filePath != null){
    				
    				String parentDir = filePath.substring(0, filePath.lastIndexOf("/"));
    				(new File(parentDir)).mkdirs();
    			
	    			File file = Harvester.downloadFile(image.getUrl(), filePath);
	    			
	    			if (file.exists()){
	    				image.setDownloded(true);
	    				imageService.save(image);
	    				
	    				LOGGER.info(String.format("Downloaded image '%s'", file.getPath()));
	    			}else{
	    				LOGGER.error(String.format("Failed to download file '%s'", file.getPath()));
	    			}
    			}
    		}    			
    		
		}
    	
    	return;
    }
    
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
    
    private void serialRun(String baseUrl){
         
        Application app = new Application(baseUrl);
        
		//app.crawl();       
		app.downloadImages(); 
    }
    
    private void parallelRun(final String baseUrl){
    	
        final List<Thread> crawlerThreads = new ArrayList<Thread>();
        final List<Thread> downloadThreads = new ArrayList<Thread>();
        final int nThreads =  2;
        
        LOGGER.info(String.format("Initiating parallel crawling with %d threads", nThreads));
        
        for (int i = 0; i < nThreads; i++){
        	
        	crawlerThreads.add(new Thread(){
        			@Override
        			public void run(){
       				
        				Application app = new Application(baseUrl);
        				
        				app.crawl();        				
        			}
        	});
        	
        	downloadThreads.add(new Thread(){
        		@Override
        		public void run(){
    				
    				Application app = new Application(baseUrl);
        			
        			app.downloadImages();
        		}
        	});
        }
        
        for (int i = 0; i < nThreads; i++){
        	crawlerThreads.get(i).start();
        	downloadThreads.get(i).start();
        }
        
        for (int i = 0; i < nThreads; i++){
        	
        	try{
        		crawlerThreads.get(i).wait();
        	}catch (InterruptedException ie){
        		ie.printStackTrace();
        	}
        	
        	try{
        		downloadThreads.get(i).wait();
        	}catch (InterruptedException ie){
        		ie.printStackTrace();
        	}        	
        }    	
    }
    
    public static void main(String[] args){
    	
    	//deleteDatabase();
    	
        @SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("spring/context.xml");

        webPageService = (WebPageService)context.getBean("webPageService");
        imageService = (ImageService)context.getBean("imageService");
        //webPageLinkService = (WebPageLinkService)context.getBean("webPageLinkService");
        
        String baseUrl = "http://www.mangareader.net";
        
        Application app = new Application(baseUrl);
        
        boolean parallel = true;
        
        if (parallel){
        	app.parallelRun(baseUrl);
        }else{
        	app.serialRun(baseUrl);
        }
		
		LOGGER.info("Exiting");
    }
}
