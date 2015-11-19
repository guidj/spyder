package org.gp.spyder.repositories;

import java.util.Set;

import org.gp.spyder.domain.WebPage;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

public interface WebPageRepository extends GraphRepository<WebPage> {
	
    // This is using the schema based index
	@Query("MATCH (n {crawled: {crawled}}) RETURN n LIMIT {limit}")
	Set<WebPage> getWebPages(@Param("crawled") boolean crawled, @Param("limit")int limit);	
	
	public WebPage findById(Long id);

    public WebPage findByUrl(String url);
}
