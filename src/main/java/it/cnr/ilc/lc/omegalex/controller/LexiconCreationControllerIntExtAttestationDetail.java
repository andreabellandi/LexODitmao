/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.domain.ExternalAttestation;
import it.cnr.ilc.lc.omegalex.domain.InternalAttestation;
import it.cnr.ilc.lc.omegalex.manager.DocumentationManager;
import it.cnr.ilc.lc.omegalex.manager.ExternalAttestationData;
import it.cnr.ilc.lc.omegalex.manager.InternalAttestationData;
import java.io.Serializable;
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
public class LexiconCreationControllerIntExtAttestationDetail extends BaseController implements Serializable {

    @Inject
    private LexiconCreationControllerTabViewList lexiconCreationControllerTabViewList;
    @Inject
    private DocumentationManager documentationManager;
    @Inject
    private LoginController loginController;

    private boolean attRendered = false;
    private String attType;

    private InternalAttestationData iad = new InternalAttestationData();
    private ExternalAttestationData ead = new ExternalAttestationData();

    public String getAttType() {
        return attType;
    }

    public void setAttType(String attType) {
        this.attType = attType;
    }

    public ExternalAttestationData getEad() {
        return ead;
    }

    public void setEad(ExternalAttestationData ead) {
        this.ead = ead;
    }

    public InternalAttestationData getIad() {
        return iad;
    }

    public void setIad(InternalAttestationData iad) {
        this.iad = iad;
    }

    public boolean isAttRendered() {
        return attRendered;
    }

    public void setAttRendered(boolean attRendered) {
        this.attRendered = attRendered;
    }

    // invoked by the controller after an user selected an attestation in the tabview
    public void addAttestation(LexiconCreationControllerTabViewList.AttTreeNode atn) {
        attType = atn.getType();
        if (attType.equals("Internal")) {
//            iad = new InternalAttestationData();
            setInternalAttestation(atn.getAttId());
        } else {
//            ead = new ExternalAttestationData();
            setExternalAttestation(atn.getAttId());
        }

    }

    private void setInternalAttestation(Long ID) {
        InternalAttestation ia = documentationManager.getInternalAttestationByID(ID);
        iad.setId(ia.getId());
        iad.setAttestationUri(ia.getAttestationUri());
        iad.setAttestationUriLemma(ia.getAttestationUriLemma());
        iad.setChapterNumber(ia.getChapterNumber());
        iad.setLineNumber(ia.getLineNumber());
//        iad.setListEntryLetter(ia.getListEntryLetter());
//        iad.setListEntryNumber(ia.getListEntryNumber());
        iad.setListEntry(ia.getListEntry());
        iad.setPageNumber(ia.getPageNumber());
        iad.setParagraphNumber(ia.getParagraphNumber());
        iad.setSectionNumber(ia.getSectionNumber());
        iad.setManSigla(ia.getManuscript().getSiglum());
        iad.setDocAbbreviation(ia.getDocument().getAbbreviation());
    }

    private void setExternalAttestation(Long ID) {
        ExternalAttestation ea = documentationManager.getExternalAttestationByID(ID);
        ead.setId(ea.getId());
        ead.setAttestationUri(ea.getAttestationUri());
        ead.setAttestationUriLemma(ea.getAttestationUriLemma());
        ead.setLineNumber(ea.getLineNumber());
        ead.setPageNumber(ea.getPageNumber());
        ead.setParagraphNumber(ea.getParagraphNumber());
        ead.setOther(ea.getOther());
        ead.setSec(ea.getSec());
        ead.setArticleNumber(ea.getArticleNumber());
        ead.setBook(ea.getBook());
        ead.setChapterNumberRoman(ea.getChapterNumberRoman());
        ead.setColumnNumber(ea.getColumnNumber());
        ead.setEntryNumber(ea.getEntryNumber());
        ead.setFascicle(ea.getFascicle());
        ead.setFolioNumber(ea.getFolioNumber());
        ead.setFootnoteNumber(ea.getFootnoteNumber());
        ead.setForm(ea.getForm());
        ead.setGlossaryNumber(ea.getGlossaryNumber());
        ead.setLineNumber(ea.getLineNumber());
        ead.setNumberOfGeographicPoint(ea.getNumberOfGeographicPoint());
        ead.setNumberOfMap(ea.getNumberOfMap());
        ead.setPart(ea.getPart());
        ead.setSubvolume(ea.getSubvolume());
        ead.setSvSublemma(ea.getSvSublemma());
        ead.setUrl(ea.getUrl());
        ead.setDocType(ea.getDocument().getType());
        ead.setVolume(ea.getVolume());
    }

    public void removeAttestation() {
        if (attType.equals("Internal")) {
            documentationManager.deleteInternalAttestationByID(iad.getId());
            log(Level.INFO, loginController.getAccount(), "DELETE Internal attestation " + iad.getDocAbbreviation() + " " + iad.getManSigla() + " " + iad.getChapterNumber() + " of " + iad.getAttestationUriLemma());
            info("template.message.deleteIntAttestation.summary", "template.message.deleteIntAttestation.description", iad.getDocAbbreviation() + " " + iad.getManSigla() + " " + iad.getChapterNumber());
            attRendered = false;
            lexiconCreationControllerTabViewList.initAttestationTabView();
        } else {
            info("template.message.deleteExtAttestation.summary", "template.message.deleteExtAttestation.description", ead.getAbbreviationId() + " of " + ead.getAttestationUriLemma());
            documentationManager.deleteExternalAttestationByID(ead.getId());
            log(Level.INFO, loginController.getAccount(), "DELETE External attestation " + ead.getAbbreviationId() + " of " + ead.getAttestationUriLemma());
            attRendered = false;
            lexiconCreationControllerTabViewList.initAttestationTabView();
        }
    }

    public void saveAttestation() {
        if (attType.equals("Internal")) {
            info("template.message.saveIntAttestation.summary", "template.message.saveIntAttestation.description", iad.getDocAbbreviation() + " " + iad.getManSigla() + " " + iad.getChapterNumber() + " of " + iad.getAttestationUriLemma());
            documentationManager.updateIntAttestation(iad);
            log(Level.INFO, loginController.getAccount(), "SAVE updated internal attestation " + iad.getDocAbbreviation() + " " + iad.getManSigla() + " " + iad.getChapterNumber());
            lexiconCreationControllerTabViewList.initAttestationTabView();
        } else {
            info("template.message.saveExtAttestation.summary", "template.message.saveExtAttestation.description", ead.getAbbreviationId() + " of " + ead.getAttestationUriLemma());
            documentationManager.updateExtAttestation(ead);
            log(Level.INFO, loginController.getAccount(), "SAVE updated external attestation " + ead.getAbbreviationId() + " of " + ead.getAttestationUriLemma());
            lexiconCreationControllerTabViewList.initAttestationTabView();
        }

    }

    public String getFormTypeLabel() {
        if (attType.equals("Internal")) {
            return iad.getAttestationUri().contains("_form") ? "Variant:" : "Lemma:";
        } else {
            return "Lemma:";
        }
    }
}
