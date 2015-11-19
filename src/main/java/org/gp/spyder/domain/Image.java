package org.gp.spyder.domain;

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

@NodeEntity
public class Image {
	
	private static final String LINKS_TO = "LINKS_TO";
	
	@GraphId
	private Long id;
	
	@Indexed(unique = true, failOnDuplicate = true)
	private String url;
	
	@Indexed(indexType = IndexType.LABEL)
	private boolean downloaded;
	
    @Fetch
    @RelatedTo(type = LINKS_TO, direction = Direction.INCOMING)
    private Set<WebPage> linkedBy;

    public Image(String url, boolean downloaded) {
        this.url = url;
        this.downloaded = downloaded;
    }

    public Image() {
    }

    public Long getId() {
    	return id;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public boolean isDownloaded(){
    	return this.downloaded;
    }
    
    public void setDownloded(boolean downloaded){
    	this.downloaded = downloaded;
    }

    @Override
	public int hashCode() {
        return (url == null) ? 0 : url.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Image other = (Image) obj;
		if (this.url == null) return other.url == null;
        return this.url.equals(other.url);
    }
	
	@Override
    public String toString() {
        return String.format("Image{url='%s'}", this.url);
    }	

}
