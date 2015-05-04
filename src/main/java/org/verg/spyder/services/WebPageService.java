package org.verg.spyder.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.verg.spyder.domain.WebPage;
import org.verg.spyder.repositories.WebPageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WebPageService {
	
	@Autowired
	private WebPageRepository webPageRepository;
	
	public long getNumberWebPages() {
		return this.webPageRepository.count();
	}
	
	public void save(WebPage webPage){
		this.webPageRepository.save(webPage);
	}
	
	public WebPage createWebPage(String url, boolean crawled, int status) {
		return this.webPageRepository.save(new WebPage(url, crawled, status));
	}
	
	public Iterable<WebPage> getAllWebPages() {
		return this.webPageRepository.findAll();
	}
	
	public WebPage findWebPageById(Long id) {
		return this.webPageRepository.findOne(id);
	}

    // This is using the schema based index
	public WebPage findWebPageByUrl(String url) {
		return this.webPageRepository.findBySchemaPropertyValue("url", url);
	}
	
	public Iterable<WebPage> getUnprocessedWebPages(){
		return this.webPageRepository.getWebPages(false, 10);
	}

}
