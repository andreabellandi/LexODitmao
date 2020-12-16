package it.cnr.ilc.lc.omegalex.domain;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 *
 * @author oakgen
 */
@Entity
public class Document extends SuperEntity {

    private String abbreviation;
    private String type;
    private String title;
    private String sourceType;

    @Column(unique=true)
    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(length = 4000)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

}
