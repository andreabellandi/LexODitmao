/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.manager;

/**
 *
 * @author andrea
 */
public class InternalAttestationData {

    private Long id;
    private String attestationUri;

    // it is called attestationUriLemma but it holds also for forms
    private String attestationUriLemma;

    private String abbreviationId;
    private String siglum;
//    private String listEntryLetter;
//    private String listEntryNumber;
    private String listEntry;
    private String sectionNumber;
    private String chapterNumber;
    private String paragraphNumber;
    private String pageNumber;
    private String lineNumber;
    private String docType;
    private String docAbbreviation;
    private String manSigla;

    private boolean saveButtonEnabled = true;
    private boolean deleteButtonEnabled = false;

    public boolean isSaveButtonEnabled() {
        return saveButtonEnabled;
    }

    public void setSaveButtonEnabled(boolean saveButtonEnabled) {
        this.saveButtonEnabled = saveButtonEnabled;
    }

    public boolean isDeleteButtonEnabled() {
        return deleteButtonEnabled;
    }

    public void setDeleteButtonEnabled(boolean deleteButtonEnabled) {
        this.deleteButtonEnabled = deleteButtonEnabled;
    }

    public String getAttestationUriLemma() {
        return attestationUriLemma;
    }

    public void setAttestationUriLemma(String attestationUriLemma) {
        this.attestationUriLemma = attestationUriLemma;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getListEntry() {
        return listEntry;
    }

    public void setListEntry(String listEntry) {
        this.listEntry = listEntry;
    }

    public String getAbbreviationId() {
        return abbreviationId;
    }

    public void setAbbreviationId(String abbreviationId) {
        this.abbreviationId = abbreviationId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttestationUri() {
        return attestationUri;
    }

    public void setAttestationUri(String attestationUri) {
        this.attestationUri = attestationUri;
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

    public String getSiglum() {
        return siglum;
    }

    public void setSiglum(String siglum) {
        this.siglum = siglum;
    }

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

    public String getDocAbbreviation() {
        return docAbbreviation;
    }

    public void setDocAbbreviation(String docAbbreviation) {
        this.docAbbreviation = docAbbreviation;
    }

    public String getManSigla() {
        return manSigla;
    }

    public void setManSigla(String manSigla) {
        this.manSigla = manSigla;
    }

}
