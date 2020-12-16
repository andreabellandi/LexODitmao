/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.manager.LexiconManager;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author andrea
 */
@ViewScoped
@Named
public class LexiconCreationControllerToolbar extends BaseController implements Serializable {

    @Inject
    private LexiconCreationControllerFormDetail lexiconCreationControllerFormDetail;
    @Inject
    private LexiconCreationControllerSenseDetail lexiconCreationControllerSenseDetail;
    @Inject
    private LexiconCreationControllerRelationDetail lexiconCreationControllerRelationDetail;
    @Inject
    private LexiconCreationControllerTabViewList lexiconCreationControllerTabViewList;
    @Inject
    private LexiconExplorationControllerDictionary lexiconExplorationControllerDictionary;
    @Inject
    private LexiconCreationControllerDocumentationDetail lexiconCreationControllerDocumentationDetail;
    @Inject
    private LexiconManager lexiconManager;
    @Inject
    private LoginController loginController;

    public void newLemma(String lemmaType) {
        lexiconManager.unlock();
        lexiconCreationControllerFormDetail.setLocked(false);
        log(Level.INFO, loginController.getAccount(), "NEW Lemma " + lemmaType);
        // check if the lexical entry has to be saved
//        lexiconCreationControllerTabViewList.savePendingChange();
        resetLexicalEntry();
        if (lemmaType.equals("Collocation")) {
            lexiconCreationControllerFormDetail.getLemma().setType("Collocation");
        } else {
            lexiconCreationControllerFormDetail.getLemma().setType("Word");
        }
        lexiconCreationControllerFormDetail.setNewAction(true);
        lexiconCreationControllerFormDetail.setPanelHeader("Lemma and Variants");

    }

    public void newDocument(String docType) {
        log(Level.INFO, loginController.getAccount(), "NEW " + docType + " document");
        resetForNewDocument();
        lexiconCreationControllerDocumentationDetail.cleanForm();
        if (docType.equals("Internal")) {
            lexiconCreationControllerDocumentationDetail.getDoc().setSourceType("Internal");
        } else {
            if (docType.equals("Manuscript")) {
                lexiconCreationControllerDocumentationDetail.getDoc().setType("Manuscript");
                lexiconCreationControllerDocumentationDetail.getDoc().setSourceType("Internal");
            } else {
                lexiconCreationControllerDocumentationDetail.getDoc().setSourceType("External");
            }
        }
        lexiconCreationControllerDocumentationDetail.setNewAction(true);
        lexiconCreationControllerDocumentationDetail.setDocRendered(true);
        lexiconCreationControllerDocumentationDetail.setDocAlreadyExists(false);

    }

    public void resetLexicalEntry() {
        lexiconCreationControllerRelationDetail.resetRelationDetails();
        lexiconCreationControllerSenseDetail.resetSenseDetails();
        lexiconCreationControllerSenseDetail.setAddSenseButtonDisabled(false);
        lexiconCreationControllerSenseDetail.setSenseToolbarRendered(false);
        lexiconCreationControllerSenseDetail.addSense();
        lexiconCreationControllerFormDetail.resetFormDetails();
        lexiconCreationControllerFormDetail.setFormAlreadyExists(false);
        lexiconCreationControllerFormDetail.setLemmAlreadyExists(false);
        lexiconCreationControllerFormDetail.setIsAdmissibleLemma(true);
        lexiconCreationControllerFormDetail.setLemmaRendered(true);
        lexiconCreationControllerFormDetail.setAddFormButtonDisabled(true);
        lexiconCreationControllerFormDetail.getLemma().clear();
        lexiconCreationControllerFormDetail.getLemmaCopy().clear();
        lexiconCreationControllerFormDetail.getLemma().setIndividual("");
        lexiconCreationControllerFormDetail.getLemmaCopy().setIndividual("");
        lexiconExplorationControllerDictionary.clearDictionary();
    }

    public void resetForNewDocument() {
        lexiconCreationControllerRelationDetail.resetRelationDetails();
        lexiconCreationControllerSenseDetail.resetSenseDetails();
        lexiconCreationControllerSenseDetail.setAddSenseButtonDisabled(false);
        lexiconCreationControllerSenseDetail.setSenseToolbarRendered(false);
        lexiconCreationControllerFormDetail.resetFormDetails();
        lexiconCreationControllerFormDetail.setFormAlreadyExists(false);
        lexiconCreationControllerFormDetail.setLemmAlreadyExists(false);
        lexiconCreationControllerFormDetail.setIsAdmissibleLemma(true);
        lexiconCreationControllerFormDetail.setLemmaRendered(false);
        lexiconCreationControllerFormDetail.setAddFormButtonDisabled(true);
        lexiconCreationControllerFormDetail.getLemma().clear();
        lexiconCreationControllerFormDetail.getLemmaCopy().clear();
        lexiconCreationControllerFormDetail.getLemma().setIndividual("");
        lexiconCreationControllerFormDetail.getLemmaCopy().setIndividual("");
        lexiconCreationControllerDocumentationDetail.setDocRendered(true);
        lexiconCreationControllerDocumentationDetail.setAddDocButtonDisabled(true);
    }

    public void newLexicon() {
        lexiconManager.initLexicon();
    }

    public void openLexicon(FileUploadEvent event) {
        FacesMessage message = new FacesMessage("Successful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
        lexiconManager.loadLexicon(event);
        lexiconCreationControllerTabViewList.initLexicaMenu();
        lexiconCreationControllerTabViewList.setLexiconLanguage("All languages");
        lexiconCreationControllerTabViewList.initLemmaTabView("All languages");
        lexiconCreationControllerTabViewList.initFormTabView("All languages");
        lexiconCreationControllerTabViewList.setEnabledFilter(true);
    }

    public StreamedContent exportLexicon() {
        log(Level.INFO, loginController.getAccount(), "EXPORT lexicon only");
        return lexiconManager.exportOWLLexicon();
    }

    public StreamedContent exportLexiconWithAttestations() {
        log(Level.INFO, loginController.getAccount(), "EXPORT lexicon with attestations");
        return lexiconManager.exportOWLLexiconWithAttestations();
    }

}
