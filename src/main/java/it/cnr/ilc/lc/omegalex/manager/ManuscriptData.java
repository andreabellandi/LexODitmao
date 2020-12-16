/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.manager;

import it.cnr.ilc.lc.omegalex.domain.Manuscript;
import java.io.Serializable;

/**
 *
 * @author andrea
 */
public class ManuscriptData implements Serializable {

    private boolean saveButtonDisabled;
    private boolean deleteButtonDisabled;

    private Long docId;
    private String siglum;
    private String title;

    public ManuscriptData(Manuscript m) {
        this.saveButtonDisabled = true;
        this.deleteButtonDisabled = false;
        this.docId = m.getId();
        this.siglum = m.getSiglum();
        this.title = m.getTitle();
    }

    public ManuscriptData() {
    }

    public String getSiglum() {
        return siglum;
    }

    public void setSiglum(String siglum) {
        this.siglum = siglum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public void clear() {
        this.saveButtonDisabled = true;
        this.deleteButtonDisabled = false;
    }

}
