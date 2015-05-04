package org.verg.spyder.repositories;

import java.util.Set;

import org.verg.spyder.domain.LinksToWebPage;
import org.verg.spyder.domain.WebPage;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

public interface WebPageLinkRepository extends GraphRepository<LinksToWebPage> {
	
	@Query("MATCH ({0})-[r :`LINKS_TO`]->({1}) RETURN r")
	Set<LinksToWebPage> getLinks(@Param("source") WebPage source, @Param("target") WebPage target);		
	
}
