package org.gp.spyder.domain;

import java.util.Set;

import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;
import org.springframework.data.neo4j.support.index.IndexType;


@NodeEntity
public class WebPage {

  @GraphId
  private Long id;

  @Indexed(unique = true, failOnDuplicate = true)
  private String url;

  @Indexed(indexType = IndexType.LABEL)
  private boolean crawled;

  @Indexed(indexType = IndexType.LABEL)
  private int status;

  @Fetch
  @RelatedToVia
  private Set<Link> linksToWebPage;

  @Fetch
  @RelatedToVia
  private Set<HasImage> linksToImage;

  public WebPage(String url, boolean crawled, int status) {
    this.url = url;
    this.crawled = crawled;
    this.status = status;
  }

  public WebPage() {
  }

  public Long getId() {
    return id;
  }

  public String getUrl() {
    return this.url;
  }

  public boolean isCrawled() {
    return this.crawled;
  }

  public void setCrawled(boolean crawled) {
    this.crawled = crawled;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public int getStatus() {
    return this.status;
  }

  public Set<Link> getReferencedWebPages() {
    return this.linksToWebPage;
  }

  public Set<HasImage> getReferencedImages() {
    return this.linksToImage;
  }

  public Link addLinkedWebPage(WebPage otherWebPage) {
    Link r = new Link(this, otherWebPage);
    this.linksToWebPage.add(r);
    return r;
  }

  public HasImage addLinkedImage(Image image) {
    HasImage r = new HasImage(this, image);
    this.linksToImage.add(r);
    return r;
  }
    
    /*public boolean isLinksTo(WebPage otherWebPage) {
      return this.linksToWebPage.contains(otherWebPage);
    }*/

  @Override
  public int hashCode() {
    return (url == null) ? 0 : url.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    WebPage other = (WebPage) obj;
    if (this.url == null) {
      return other.url == null;
    }
    return this.url.equals(other.url);
  }

  @Override
  public String toString() {
    return String.format("WebPage{url='%s'}", this.url);
  }

}
