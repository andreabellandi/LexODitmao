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
public class LexiconCreationFormFilterController extends BaseController implements Serializable {

    @Inject
    private LexiconCreationControllerTabViewList lexiconCreationControllerTabViewList;

    private boolean startWith = true;
    private boolean contains = false;

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

    public void startsWithChanged() {
        contains = !startWith;
        if (lexiconCreationControllerTabViewList.getFormField() != null) {
            if (lexiconCreationControllerTabViewList.getFormField().length() > 0) {
                lexiconCreationControllerTabViewList.formKeyupFilterEvent(lexiconCreationControllerTabViewList.getFormField());
            }
        }
    }

    public void containsChanged() {
        startWith = !contains;
        if (lexiconCreationControllerTabViewList.getFormField() != null) {
            if (lexiconCreationControllerTabViewList.getFormField().length() > 0) {
                lexiconCreationControllerTabViewList.formKeyupFilterEvent(lexiconCreationControllerTabViewList.getFormField());
            }
        }
    }

}
