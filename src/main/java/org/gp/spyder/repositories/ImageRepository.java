package org.gp.spyder.repositories;

import java.util.Set;

import org.gp.spyder.domain.Image;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

public interface ImageRepository extends GraphRepository<Image> {
	
    // This is using the schema based index
	@Query("MATCH (n {downloaded: {downloaded}}) RETURN n LIMIT {limit}")
    Set<Image> getImages(@Param("downloaded") boolean downloaded, @Param("limit")int limit);

	public Image findById(Long id);

	public Image findByUrl(String url);
}
