/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.domain.Manuscript;
import it.cnr.ilc.lc.omegalex.manager.DocumentationManager;
import it.cnr.ilc.lc.omegalex.manager.FormData;
import it.cnr.ilc.lc.omegalex.manager.InternalAttestationData;
import it.cnr.ilc.lc.omegalex.manager.ManuscriptData;
import java.io.Serializable;
import java.util.ArrayList;
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
public class InternalAttestationCreationDialogController extends BaseController implements Serializable {

    @Inject
    private LexiconCreationControllerFormDetail lexiconCreationControllerFormDetail;
    @Inject
    private LexiconCreationControllerTabViewList lexiconCreationControllerTabViewList;
    @Inject
    private DocumentationManager documentationManager;
    @Inject
    private LoginController loginController;

    private InternalAttestationData internalAttestationData;
    private int formPosition;
    private String dialogHeader;

    public String getDialogHeader() {
        return dialogHeader;
    }

    public void setDialogHeader(String dialogHeader) {
        this.dialogHeader = dialogHeader;
    }

    public int getFormPosition() {
        return formPosition;
    }

    public void setFormPosition(int formPosition) {
        this.formPosition = formPosition;
    }

    public InternalAttestationData getInternalAttestationData() {
        return internalAttestationData;
    }

    public void setInternalAttestationData(InternalAttestationData internalAttestationData) {
        this.internalAttestationData = internalAttestationData;
    }

    public ArrayList<String> getDocList() {
        return (ArrayList<String>) documentationManager.getAbbreviationDocuments("Internal");
    }

    public ArrayList<String> getManSiglaList() {
        ArrayList<String> al = new ArrayList();
        for (Manuscript m : documentationManager.getManuscripts()) {
            al.add(m.getSiglum());
        }
        return al;
    }

    public void addInternalAttestation(String attestationUriLemma) {
        internalAttestationData = new InternalAttestationData();
        internalAttestationData.setAttestationUriLemma(attestationUriLemma);
        internalAttestationData.setAttestationUri("http://ditmao.ilc.cnr.it/ditmao#" + lexiconCreationControllerFormDetail.getLemma().getIndividual());
    }

    public void addInternalAttestation(FormData fd) {
        internalAttestationData = new InternalAttestationData();
        internalAttestationData.setAttestationUriLemma(fd.getFormWrittenRepr());
        internalAttestationData.setAttestationUri("http://ditmao.ilc.cnr.it/ditmao#" + fd.getIndividual());
        
    }

    public void setInternalAttestation(InternalAttestationData iad) {
        // means that an user modifies an attestation (not new!)
        iad.setDeleteButtonEnabled(true);
        internalAttestationData = iad;
    }

    // save/update internal attestation of both lemma and form
    public void save() {
        if (internalAttestationData.getAttestationUri().contains("_form")) {
            // form attestation
            saveIntAttOfForm();
        } else {
            // lemma attestation
            saveIntAttOfLemma();
        }
        lexiconCreationControllerTabViewList.initAttestationTabView();
    }

    private void saveIntAttOfLemma() {
        internalAttestationData.setAttestationUri("http://ditmao.ilc.cnr.it/ditmao#" + lexiconCreationControllerFormDetail.getLemma().getIndividual());
        info("template.message.saveIntAttestation.summary", "template.message.saveIntAttestation.description", null);
        if (internalAttestationData.isDeleteButtonEnabled()) {
            // modification    
            documentationManager.updateIntAttestation(internalAttestationData);
            log(Level.INFO, loginController.getAccount(), "UPDATE internal attestation (id DB: " + internalAttestationData.getId() + ") of " + lexiconCreationControllerFormDetail.getLemma().getFormWrittenRepr());
        } else {
            // new
            Long id = documentationManager.insertIntAttestation(internalAttestationData);
            log(Level.INFO, loginController.getAccount(), "SAVE new internal attestation (id DB: " + id + ") of " + lexiconCreationControllerFormDetail.getLemma().getFormWrittenRepr());
            internalAttestationData.setId(id);
            lexiconCreationControllerFormDetail.getLemma().getInternalAttestation().add(internalAttestationData);
            lexiconCreationControllerFormDetail.getLemmaCopy().getInternalAttestation().add(internalAttestationData);
            lexiconCreationControllerFormDetail.updateAttestationBoxes();
        }
    }

    private void saveIntAttOfForm() {
        info("template.message.saveIntAttestation.summary", "template.message.saveIntAttestation.description", null);
        if (internalAttestationData.isDeleteButtonEnabled()) {
            // modification    
            documentationManager.updateIntAttestation(internalAttestationData);
            log(Level.INFO, loginController.getAccount(), "UPDATE internal attestation (id DB: " + internalAttestationData.getId() + ") of " + internalAttestationData.getAttestationUriLemma());
        } else {
            // new
            Long id = documentationManager.insertIntAttestation(internalAttestationData);
            log(Level.INFO, loginController.getAccount(), "SAVE new internal attestation (id DB: " + id + ") of " + internalAttestationData.getAttestationUriLemma());
            internalAttestationData.setId(id);
            lexiconCreationControllerFormDetail.getForms().get(formPosition).getInternalAttestation().add(internalAttestationData);
            lexiconCreationControllerFormDetail.getFormsCopy().get(formPosition).getInternalAttestation().add(internalAttestationData);
            int size = lexiconCreationControllerFormDetail.getForms().get(formPosition).getInternalAttestation().size();
            lexiconCreationControllerFormDetail.updateAttestationFormBoxes(size);
        }
    }

}
