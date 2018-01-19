package org.gp.spyder.domain;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;


@RelationshipEntity(type = "LINKS_TO")
public class Link {

  @GraphId
  Long id;
  @StartNode
  WebPage source;
  @EndNode
  WebPage target;

  public Link(WebPage source, WebPage target) {
    this.source = source;
    this.target = target;
  }

  public Link() {

  }

  public WebPage getSource() {
    return this.source;
  }

  public WebPage getTarget() {
    return this.target;
  }

  @Override
  public String toString() {
    return String.format("%s links to %s", this.source, this.target);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Link linksToWebPage = (Link) o;
    if (id == null) {
      return super.equals(o);
    }
    return id.equals(linksToWebPage.id);

  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : super.hashCode();
  }
}