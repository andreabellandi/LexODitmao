/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.manager;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author andrea
 */
public class ReferenceData implements Serializable {

    private boolean saveButtonDisabled;
    private boolean deleteButtonDisabled;
    private String type;
    private String title;
    private String abbrevTitle;
    private String author;
    private String publisher;
    private String city;
    private String year;
    private String editedIn;
    private String groupOfDoc;
    // this is used for storing a string describing the pages a form attested in
    private String location;

    public ReferenceData() {
        this.saveButtonDisabled = true;
        this.deleteButtonDisabled = false;
        this.type = "Dictionary";
        this.title = "";
        this.abbrevTitle = "";
        this.author = "";
        this.publisher = "";
        this.city = "";
        this.year = "";
        this.editedIn = "";
        this.groupOfDoc = "";
        this.location = "";
    }

    public ReferenceData(String type, String title, String abbrevTitle, String author, String publisher, String city, String year, String editedIn, String groupOfDoc) {
        this.saveButtonDisabled = true;
        this.deleteButtonDisabled = false;
        this.type = type;
        this.title = title;
        this.abbrevTitle = abbrevTitle;
        this.author = author;
        this.publisher = publisher;
        this.city = city;
        this.year = year;
        this.editedIn = editedIn;
        this.groupOfDoc = groupOfDoc;
        this.location = "";
    }

    public boolean isSaveButtonDisabled() {
        return saveButtonDisabled;
    }

    public void setSaveButtonDisabled(boolean saveButtonDisabled) {
        this.saveButtonDisabled = saveButtonDisabled;
    }

    public boolean isDeleteButtonDisabled() {
        return deleteButtonDisabled;
    }

    public void setDeleteButtonDisabled(boolean deleteButtonDisabled) {
        this.deleteButtonDisabled = deleteButtonDisabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbbrevTitle() {
        return abbrevTitle;
    }

    public void setAbbrevTitle(String abbrevTitle) {
        this.abbrevTitle = abbrevTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getEditedIn() {
        return editedIn;
    }

    public void setEditedIn(String editedIn) {
        this.editedIn = editedIn;
    }

    public String getGroupOfDoc() {
        return groupOfDoc;
    }

    public void setGroupOfDoc(String groupOfDoc) {
        this.groupOfDoc = groupOfDoc;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
