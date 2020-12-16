/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author andrea
 */
@ViewScoped
@Named
public class LexiconCreationAttestationFilterController extends BaseController implements Serializable {

    @Inject
    private LexiconCreationControllerTabViewList lexiconCreationControllerTabViewList;

    private boolean startWith = true;
    private boolean contains = false;

    private boolean internal = false;
    private boolean external = false;
    private boolean all = true;

    private String docType = "All";

    public boolean isStartWith() {
        return startWith;
    }

    public void setStartWith(boolean startWith) {
        this.startWith = startWith;
    }

    public boolean isContains() {
        return contains;
    }

    public void setContains(boolean contains) {
        this.contains = contains;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public void startsWithChanged() {
        contains = !startWith;
        if (lexiconCreationControllerTabViewList.getAttField() != null) {
            if (lexiconCreationControllerTabViewList.getAttField().length() > 0) {
                lexiconCreationControllerTabViewList.attKeyupFilterEvent(lexiconCreationControllerTabViewList.getAttField());
            }
        }
    }

    public void containsChanged() {
        startWith = !contains;
        if (lexiconCreationControllerTabViewList.getAttField() != null) {
            if (lexiconCreationControllerTabViewList.getAttField().length() > 0) {
                lexiconCreationControllerTabViewList.attKeyupFilterEvent(lexiconCreationControllerTabViewList.getAttField());
            }
        }
    }

    public void docTypeChanged() {
        if (docType.equals("Manuscript")) {
            if (external || all) {
                external = false;
                all = false;
                internal = true;
            }
        }
        lexiconCreationControllerTabViewList.attKeyupFilterEvent(lexiconCreationControllerTabViewList.getAttField());
    }

    public void internalChanged() {
        if (internal) {
            all = false;
            external = false;
        } else {
            if (internal == false && external == false && all == false) {
                internal = true;
            }
        }
        lexiconCreationControllerTabViewList.attKeyupFilterEvent(lexiconCreationControllerTabViewList.getAttField());
    }

    public void externalChanged() {
        if (external) {
            internal = false;
            all = false;
        } else {
            if (internal == false && external == false && all == false) {
                external = true;
            }
        }
        if (docType.equals("Manuscript")) {
            external = false;
            all = false;
            internal = true;
        }
        lexiconCreationControllerTabViewList.attKeyupFilterEvent(lexiconCreationControllerTabViewList.getAttField());
    }

    public void allChanged() {
        if (all) {
            internal = false;
            external = false;
        } else {
            if (internal == false && external == false && all == false) {
                all = true;
            }
        }
        if (docType.equals("Manuscript")) {
            external = false;
            all = false;
            internal = true;
        }
        lexiconCreationControllerTabViewList.attKeyupFilterEvent(lexiconCreationControllerTabViewList.getAttField());
    }

}
