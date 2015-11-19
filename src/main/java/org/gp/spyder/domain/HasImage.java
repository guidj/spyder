package org.gp.spyder.domain;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type="HAS_IMAGE")
public class HasImage {
	@GraphId Long id;		
    @StartNode WebPage webPage;
    @EndNode Image image;
    
    public HasImage(WebPage webPage, Image image){
    	this.webPage = webPage;
    	this.image = image;
    }	    
    
    public HasImage(){
    	
    }
    
    public WebPage getWebPage() {
        return this.webPage;
    }

    public Image getImage() {
        return this.image;
    }

    @Override
    public String toString() {
        return String.format("%s has %s", this.webPage, this.image);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HasImage linksToImage = (HasImage) o;
        if (id == null) return super.equals(o);
        return id.equals(linksToImage.id);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }		    
}