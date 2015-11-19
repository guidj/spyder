package org.gp.spyder;

import org.gp.spyder.repositories.ImageRepository;
import org.gp.spyder.repositories.WebPageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class Operator {
	
	@Autowired
    private WebPageRepository webPageRepository;
    
    @Autowired
    private ImageRepository imageRepository;
    
    public WebPageRepository pageRepo() {
    	return webPageRepository;
    }
    
    public ImageRepository ImageRepo() {
    	return imageRepository;
    }
}
