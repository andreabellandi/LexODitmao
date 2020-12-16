/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 *
 * @author andrea
 */
@Entity
public class InternalAttestation extends Attestation {

    private Manuscript manuscript;
//    private String listEntryLetter;
//    private String listEntryNumber;
    private String sectionNumber;
    private String chapterNumber;
    private String listEntry;
    private String paragraphNumber;
    private String pageNumber;
    private String lineNumber;
    

    @ManyToOne(fetch = FetchType.LAZY)
    public Manuscript getManuscript() {
        return manuscript;
    }

    public void setManuscript(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

//    public String getListEntryLetter() {
//        return listEntryLetter;
//    }
//
//    public void setListEntryLetter(String listEntryLetter) {
//        this.listEntryLetter = listEntryLetter;
//    }
//
//    public String getListEntryNumber() {
//        return listEntryNumber;
//    }
//
//    public void setListEntryNumber(String listEntryNumber) {
//        this.listEntryNumber = listEntryNumber;
//    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public String getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(String chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public String getListEntry() {
        return listEntry;
    }

    public void setListEntry(String listEntry) {
        this.listEntry = listEntry;
    }

    public String getParagraphNumber() {
        return paragraphNumber;
    }

    public void setParagraphNumber(String paragraphNumber) {
        this.paragraphNumber = paragraphNumber;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

}
