package org.verg.spyder.services;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.verg.spyder.domain.LinksToWebPage;
import org.verg.spyder.domain.WebPage;
import org.verg.spyder.repositories.WebPageLinkRepository;


@Service
@Transactional
public class WebPageLinkService {
	
	@Autowired
	private WebPageLinkRepository webPageLinkRepository;
	
	public LinksToWebPage linkPages(WebPage source, WebPage target){
		
		LinksToWebPage r = new LinksToWebPage(source, target);
		return this.webPageLinkRepository.save(r);
		
	}
	
	public Set<LinksToWebPage> findLinksByWebPages(WebPage source, WebPage target){
		return this.webPageLinkRepository.getLinks(source, target);
	}

}
