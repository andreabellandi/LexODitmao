/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.manager.DocumentationManager;
import it.cnr.ilc.lc.omegalex.manager.ExternalAttestationData;
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
public class ExternalAttestationCreationDialogController extends BaseController implements Serializable {

    @Inject
    private LexiconCreationControllerTabViewList lexiconCreationControllerTabViewList;
    @Inject
    private LexiconCreationControllerFormDetail lexiconCreationControllerFormDetail;
    @Inject
    private DocumentationManager documentationManager;
    @Inject
    private LoginController loginController;

    private ExternalAttestationData externalAttestationData;

    public ExternalAttestationData getExternalAttestationData() {
        return externalAttestationData;
    }

    public void setExternalAttestationData(ExternalAttestationData externalAttestationData) {
        this.externalAttestationData = externalAttestationData;
    }

    public ArrayList<String> getDocList() {
        return (ArrayList<String>) documentationManager.getAbbreviationDocuments("External");
    }

    public String getDialogHeader() {
        return "External attestation of " + lexiconCreationControllerFormDetail.getLemma().getFormWrittenRepr() + "@" + lexiconCreationControllerFormDetail.getLemma().getLanguage();
    }

    public void addExternalAttestation(String attestationUriLemma) {
        externalAttestationData = new ExternalAttestationData();
        externalAttestationData.setAttestationUriLemma(attestationUriLemma);
        externalAttestationData.setAttestationUri("http://ditmao.ilc.cnr.it/ditmao#" + lexiconCreationControllerFormDetail.getLemma().getIndividual());
    }

    public void setExternalAttestation(ExternalAttestationData ead) {
        // means that an user modifies an attestation (not new!)
        ead.setDeleteButtonEnabled(true);
        externalAttestationData = ead;
    }

    // save new attestation
    public void save() {
        externalAttestationData.setAttestationUri("http://ditmao.ilc.cnr.it/ditmao#" + lexiconCreationControllerFormDetail.getLemma().getIndividual());
        info("template.message.saveExtAttestation.summary", "template.message.saveExtAttestation.description", null);
        if (externalAttestationData.isDeleteButtonEnabled()) {
            // modification    
            documentationManager.updateExtAttestation(externalAttestationData);
            log(Level.INFO, loginController.getAccount(), "UPDATE external attestation (id DB: " + externalAttestationData.getId() + ") of " + lexiconCreationControllerFormDetail.getLemma().getFormWrittenRepr());
            
        } else {
            // new
            Long id = documentationManager.insertExtAttestation(externalAttestationData);
            log(Level.INFO, loginController.getAccount(), "SAVE new external attestation (id DB: " + id + ") of " + lexiconCreationControllerFormDetail.getLemma().getFormWrittenRepr());
            externalAttestationData.setId(id);

            lexiconCreationControllerFormDetail.getLemma().getExternalAttestation().add(externalAttestationData);
            lexiconCreationControllerFormDetail.getLemmaCopy().getExternalAttestation().add(externalAttestationData);
        }

        String type = documentationManager.getDocumentByAbbreviation(externalAttestationData.getAbbreviationId()).getType();
        externalAttestationData.setDocType(type);

        lexiconCreationControllerFormDetail.updateAttestationBoxes();
        lexiconCreationControllerTabViewList.initAttestationTabView();
    }

}
