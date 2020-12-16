/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.domain.Document;
import it.cnr.ilc.lc.omegalex.domain.Manuscript;
import it.cnr.ilc.lc.omegalex.manager.DocumentData;
import it.cnr.ilc.lc.omegalex.manager.DocumentationManager;
import it.cnr.ilc.lc.omegalex.manager.ManuscriptData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.log4j.Level;

/**
 *
 * @author andrea
 */
@ViewScoped
@Named
public class LexiconCreationControllerDocumentationDetail extends BaseController implements Serializable {

    @Inject
    private LexiconCreationControllerTabViewList lexiconCreationControllerTabViewList;
    @Inject
    private DocumentationManager documentationManager;
    @Inject
    private LoginController loginController;

    private boolean docRendered = false;
    private boolean newAction = false;
    private boolean docAlreadyExists = false;
    private boolean addDocButtonDisabled = true;

    private DocumentData doc = new DocumentData();
    private ManuscriptData man = new ManuscriptData();

    public boolean isDocRendered() {
        return docRendered;
    }

    public void setDocRendered(boolean docRendered) {
        this.docRendered = docRendered;
    }

    public boolean isNewAction() {
        return newAction;
    }

    public void setNewAction(boolean newAction) {
        this.newAction = newAction;
    }

    public boolean isDocAlreadyExists() {
        return docAlreadyExists;
    }

    public void setDocAlreadyExists(boolean docAlreadyExists) {
        this.docAlreadyExists = docAlreadyExists;
    }

    public boolean isAddDocButtonDisabled() {
        return addDocButtonDisabled;
    }

    public void setAddDocButtonDisabled(boolean addDocButtonDisabled) {
        this.addDocButtonDisabled = addDocButtonDisabled;
    }

    public DocumentData getDoc() {
        return doc;
    }

    public void cleanForm() {
        doc.setDocId(Long.valueOf(-1));
        doc.setAbbreviation("");
        doc.setSourceType("");
        doc.setTitle("");
        doc.setType("");
    }

    public void docTypeChanged(AjaxBehaviorEvent e) {
        String type = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Document type " + doc.getAbbreviation() + " to " + type);
        doc.setType(type);
        doc.setSaveButtonDisabled(!isSavableDocument());
    }

    public void docAbbChanged(AjaxBehaviorEvent e) {
        String abb = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Document abbreviation of " + doc.getDocId() + " to " + doc.getAbbreviation());
        doc.setAbbreviation(abb);
        if (abbreviationAlreadyExists(abb)) {
            docAlreadyExists = true;
            doc.setSaveButtonDisabled(true);
        } else {
            docAlreadyExists = false;
            doc.setSaveButtonDisabled(!isSavableDocument());
        }
    }

    private boolean abbreviationAlreadyExists(String abb) {
        if (documentationManager.abbreviationDocumentAlreadyExists(abb) || documentationManager.abbreviationManuscriptAlreadyExists(abb)) {
            return true;
        } else {
            return false;
        }
    }

    public void docTitleChanged(AjaxBehaviorEvent e) {
        String tit = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Document title of " + doc.getAbbreviation() + " to " + doc.getTitle());
        doc.setTitle(tit);
        doc.setSaveButtonDisabled(!isSavableDocument());
    }

    // invoked by the controller after an user selected a document in the tabview
    public void addDocument(LexiconCreationControllerTabViewList.DocTreeNode dtn) {
        doc.clear();
        if (dtn.getType().equals("Manuscript")) {
            setManuscript(dtn.getDocId());
        } else {
            setDocument(dtn.getDocId());
        }
    }

    private void setDocument(Long ID) {
        Document d = documentationManager.getDocumentByID(ID);
        doc.setDocId(d.getId());
        doc.setAbbreviation(d.getAbbreviation());
        doc.setSourceType(d.getSourceType());
        doc.setTitle(d.getTitle());
        doc.setType(d.getType());
    }

    private void setManuscript(Long ID) {
        Manuscript m = documentationManager.getManuscriptByID(ID);
        doc.setDocId(m.getId());
        doc.setAbbreviation(m.getSiglum());
        doc.setTitle(m.getTitle());
        doc.setType("Manuscript");
        doc.setSourceType("Internal");
    }

    private List<ManuscriptData> copyManuscriptData(List<Manuscript> mdl) {
        List<ManuscriptData> _mdl = new ArrayList();
        for (Manuscript md : mdl) {
            ManuscriptData _md = new ManuscriptData();
            _md.setSiglum(md.getSiglum());
            _md.setDocId(md.getId());
            _md.setTitle(md.getTitle());
            _mdl.add(_md);
        }
        return _mdl;
    }

    public void removeDocument() {
        if (!Objects.equals(doc.getDocId(), Long.valueOf(-1))) {
            if (removable()) {
                documentationManager.deleteDocumentByID(doc.getDocId(), doc.getType());
                log(Level.INFO, loginController.getAccount(), "DELETE Document " + doc.getAbbreviation());
                info("template.message.deleteDocument.summary", "template.message.deleteDocument.description", doc.getAbbreviation());
                doc.clear();
                docRendered = false;
                addDocButtonDisabled = true;
                lexiconCreationControllerTabViewList.initDocumentTabView();
            } else {
                log(Level.INFO, loginController.getAccount(), "TRY TO DELETE Manuscript " + doc.getAbbreviation() + " that has some association");
                warn("template.message.deleteManuscriptDenied.summary", "template.message.deleteManuscriptDenied.description", doc.getAbbreviation());
            }
        } else {
            docRendered = false;
        }
    }

    public void saveDocument() {
        doc.setSaveButtonDisabled(true);
        doc.setDeleteButtonDisabled(false);
        addDocButtonDisabled = false;
        info("template.message.saveDocument.summary", "template.message.saveDocument.description", doc.getAbbreviation());
        if (newAction) {
            Long id = documentationManager.insertDocument(doc);
            if (doc.getType().equals("Manuscript")) {
                setManuscript(id);
                log(Level.INFO, loginController.getAccount(), "SAVE new manuscript " + doc.getAbbreviation());
            } else {
                setDocument(id);
                log(Level.INFO, loginController.getAccount(), "SAVE new document " + doc.getAbbreviation());
            }
        } else {
            documentationManager.updateDocument(doc);
            if (doc.getType().equals("Manuscript")) {
                setManuscript(doc.getDocId());
                log(Level.INFO, loginController.getAccount(), "SAVE updated manuscript " + doc.getAbbreviation());
            } else {
                setDocument(doc.getDocId());
                log(Level.INFO, loginController.getAccount(), "SAVE updated document " + doc.getAbbreviation());
            }
        }
        newAction = false;
        lexiconCreationControllerTabViewList.initDocumentTabView();
    }

    private boolean removable() {
        if (doc.getType().equals("Manuscript")) {
            return !documentationManager.isEditedManuscript(doc.getAbbreviation());
        } else {
            return true;
        }
    }

    private boolean isSavableDocument() {
        return (!doc.getAbbreviation().isEmpty() && (!docAlreadyExists)
                && (doc.getType().equals("Article") || doc.getType().equals("Book") || doc.getType().equals("Map") || doc.getType().equals("Manuscript") || doc.getType().equals("Dictionary")
                || doc.getType().equals("Text")));
    }

}
