package org.gp.spyder.repositories;

import java.util.Set;

import org.gp.spyder.domain.Link;
import org.gp.spyder.domain.WebPage;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

public interface LinkRepository extends GraphRepository<Link> {

  @Query("MATCH ({0})-[r :`LINKS_TO`]->({1}) RETURN r")
  Set<Link> getLinks(@Param("source") WebPage source, @Param("target") WebPage target);

}
