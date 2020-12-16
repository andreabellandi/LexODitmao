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
public class ExternalAttestationData {

    private Long id;
    private String attestationUri;
    private String attestationUriLemma;
    private String abbreviationId;
    private String volume;
    private String subvolume;
    private String pageNumber;
    private String columnNumber;
    private String articleNumber;
    private String lineNumber;
    private String glossaryNumber;
    private String numberOfMap;
    private String footnoteNumber;
    private String folioNumber;
    private String numberOfGeographicPoint;
    private String entryNumber;
    private String chapterNumberRoman;
    private String fascicle;
    private String svSublemma;
    private String part;
    private String paragraphNumber;
    private String book;
    private String docType;
    private String url;
    private String form;
    private String sec;
    private String other;

    private boolean saveButtonEnabled = true;
    private boolean deleteButtonEnabled = false;

    public String getSec() {
        return sec;
    }

    public void setSec(String sec) {
        this.sec = sec;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDocType() {
        return docType;
    }

    public String getAttestationUriLemma() {
        return attestationUriLemma;
    }

    public void setAttestationUriLemma(String attestationUriLemma) {
        this.attestationUriLemma = attestationUriLemma;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAbbreviationId() {
        return abbreviationId;
    }

    public void setAbbreviationId(String abbreviationId) {
        this.abbreviationId = abbreviationId;
    }

    public String getAttestationUri() {
        return attestationUri;
    }

    public void setAttestationUri(String attestationUri) {
        this.attestationUri = attestationUri;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getSubvolume() {
        return subvolume;
    }

    public void setSubvolume(String subvolume) {
        this.subvolume = subvolume;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(String columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getGlossaryNumber() {
        return glossaryNumber;
    }

    public void setGlossaryNumber(String glossaryNumber) {
        this.glossaryNumber = glossaryNumber;
    }

    public String getNumberOfMap() {
        return numberOfMap;
    }

    public void setNumberOfMap(String numberOfMap) {
        this.numberOfMap = numberOfMap;
    }

    public String getFootnoteNumber() {
        return footnoteNumber;
    }

    public void setFootnoteNumber(String footnoteNumber) {
        this.footnoteNumber = footnoteNumber;
    }

    public String getFolioNumber() {
        return folioNumber;
    }

    public void setFolioNumber(String folioNumber) {
        this.folioNumber = folioNumber;
    }

    public String getNumberOfGeographicPoint() {
        return numberOfGeographicPoint;
    }

    public void setNumberOfGeographicPoint(String numberOfGeographicPoint) {
        this.numberOfGeographicPoint = numberOfGeographicPoint;
    }

    public String getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(String entryNumber) {
        this.entryNumber = entryNumber;
    }

    public String getChapterNumberRoman() {
        return chapterNumberRoman;
    }

    public void setChapterNumberRoman(String chapterNumberRoman) {
        this.chapterNumberRoman = chapterNumberRoman;
    }

    public String getFascicle() {
        return fascicle;
    }

    public void setFascicle(String fascicle) {
        this.fascicle = fascicle;
    }

    public String getSvSublemma() {
        return svSublemma;
    }

    public void setSvSublemma(String svSublemma) {
        this.svSublemma = svSublemma;
    }

     public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getParagraphNumber() {
        return paragraphNumber;
    }

    public void setParagraphNumber(String paragraphNumber) {
        this.paragraphNumber = paragraphNumber;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

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

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

}
