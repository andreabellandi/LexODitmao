/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.domain.InternalAttestation;
import it.cnr.ilc.lc.omegalex.manager.AccountManager;
import it.cnr.ilc.lc.omegalex.manager.DocumentationManager;
import it.cnr.ilc.lc.omegalex.manager.ExternalAttestationData;
import it.cnr.ilc.lc.omegalex.manager.FormData;
import it.cnr.ilc.lc.omegalex.manager.InternalAttestationData;
import it.cnr.ilc.lc.omegalex.manager.LemmaData;
import it.cnr.ilc.lc.omegalex.manager.LemmaData.Word;
import it.cnr.ilc.lc.omegalex.manager.LexiconManager;
import it.cnr.ilc.lc.omegalex.manager.LexiconQuery;
import it.cnr.ilc.lc.omegalex.manager.PropertyValue;
import it.cnr.ilc.lc.omegalex.manager.ReferenceData;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.log4j.Level;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 *
 * @author andrea
 */
@ViewScoped
@Named
public class LexiconCreationControllerFormDetail extends BaseController implements Serializable {

    @Inject
    private PropertyValue propertyValue;
    @Inject
    private LexiconCreationControllerTabViewList lexiconCreationControllerTabViewList;
    @Inject
    private LexiconCreationControllerSenseDetail lexiconCreationViewSenseDetail;
    @Inject
    private LexiconCreationControllerRelationDetail lexiconCreationControllerRelationDetail;
    @Inject
    private LexiconExplorationControllerDictionary lexiconExplorationControllerDictionary;
    @Inject
    private ExternalAttestationCreationDialogController externalAttestationCreationDialogController;
    @Inject
    private InternalAttestationCreationDialogController internalAttestationCreationDialogController;
    @Inject
    private DocumentationManager documentationManager;
    @Inject
    private LexiconManager lexiconManager;
    @Inject
    private LoginController loginController;

    private LemmaData lemma = new LemmaData();
    private LemmaData lemmaCopy = new LemmaData();

    private final ArrayList<FormData> forms = new ArrayList();
    private final ArrayList<FormData> formsCopy = new ArrayList();

    private final String LemmaOfMultiwordNotFound = "";

    private String externalAttestaionTabHeader = "External Documentation (0)";
    private String internalAttestaionTabHeader = "Internal Documentation (0)";

    private String internalAttestaionFormTabHeader = "Internal Documentation (0)";

    private String panelHeader = "Lexical entry";

    private boolean lemmaRendered = false;
    private boolean newAction = false;
    private boolean lemmAlreadyExists = false;
    private boolean isAdmissibleLemma = true;
    private boolean formAlreadyExists = false;
    private boolean addFormButtonDisabled = true;

    private boolean verified = false;

    private boolean locked = false;
    private String locker = "";

    private final String MULTIWORD_COMPONENT_REGEXP = "([0-9]*[#|\\*]*)*";
//    private final String ADMISSIBLE_WORD_REGEXP = "^[aA-zZ]+[0-9]*"; 
    //    private final String ADMISSIBLE_MULTIWORD_REGEXP = "^[aA-zZ]+";
    //private final String ADMISSIBLE_WORD_REGEXP = "^[^\\*\\.\\#\\(\\)\\[\\]\\{\\};:,\\/=\\+\\-_\\\\\\?\\!\"%&0-9\\s]+[0-9]*$";
    private final String ADMISSIBLE_WORD_REGEXP = "^[^\\^\\°\\|\\*\\.\\#\\(\\)\\[\\]\\{\\};:,\\/=\\+\\-_\\\\\\?\\!\"%&\\d\\s]+\\d*$";
    private final String ADMISSIBLE_MULTIWORD_REGEXP = "^[^\\^\\°\\|\\*\\.\\#\\(\\)\\[\\]\\{\\};:,\\/=\\+\\-_\\\\\\?\\!\"%&]+$";

    public boolean isUserEnable() {
        return loginController.getAccount().getType().getName().equals(AccountManager.ADMINISTRATOR);
    }

    public String getInternalAttestaionFormTabHeader() {
        return internalAttestaionFormTabHeader;
    }

    public void setInternalAttestaionFormTabHeader(String internalAttestaionFormTabHeader) {
        this.internalAttestaionFormTabHeader = internalAttestaionFormTabHeader;
    }

    public String getExternalAttestaionTabHeader() {
        return externalAttestaionTabHeader;
    }

    public void setExternalAttestaionTabHeader(String externalAttestaionTabHeader) {
        this.externalAttestaionTabHeader = externalAttestaionTabHeader;
    }

    public String getInternalAttestaionTabHeader() {
        return internalAttestaionTabHeader;
    }

    public void setInternalAttestaionTabHeader(String internalAttestaionTabHeader) {
        this.internalAttestaionTabHeader = internalAttestaionTabHeader;
    }

    public String getPanelHeader() {
        return panelHeader;
    }

    public void setPanelHeader(String panelHeader) {
        this.panelHeader = panelHeader;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getLocker() {
        return locker;
    }

    public void setLocker(String locker) {
        this.locker = locker;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public List<FormData> getForms() {
        return forms;
    }

    public ArrayList<FormData> getFormsCopy() {
        return formsCopy;
    }

    public LemmaData getLemma() {
        return lemma;
    }

    public LemmaData getLemmaCopy() {
        return lemmaCopy;
    }

    public boolean isNewAction() {
        return newAction;
    }

    public void setNewAction(boolean newAction) {
        this.newAction = newAction;
    }

    public boolean isLemmAlreadyExists() {
        return lemmAlreadyExists;
    }

    public void setLemmAlreadyExists(boolean lemmAlreadyExists) {
        this.lemmAlreadyExists = lemmAlreadyExists;
    }

    public boolean isIsAdmissibleLemma() {
        return isAdmissibleLemma;
    }

    public void setIsAdmissibleLemma(boolean isAdmissibleLemma) {
        this.isAdmissibleLemma = isAdmissibleLemma;
    }

    public boolean isFormAlreadyExists() {
        return formAlreadyExists;
    }

    public void setFormAlreadyExists(boolean formAlreadyExists) {
        this.formAlreadyExists = formAlreadyExists;
    }

    public boolean isAddFormButtonDisabled() {
        return addFormButtonDisabled;
    }

    public void setAddFormButtonDisabled(boolean addFormButtonDisabled) {
        this.addFormButtonDisabled = addFormButtonDisabled;
    }

    public boolean isLemmaRendered() {
        return lemmaRendered;
    }

    public void setLemmaRendered(boolean lemmaRendered) {
        this.lemmaRendered = lemmaRendered;
    }

    public String getLemmaOfMultiwordNotFound() {
        return LemmaOfMultiwordNotFound;
    }

    public String getEntryErrorLabel() {
        if (lemma.getType().equals("Word") && (lemma.getFormWrittenRepr().contains(" "))) {
            return "Word can not contain spaces";
        } else {
            if (!isAdmissibleLemma) {
                return "Only letters are admissible. <br/> For homonyms you can concatenate a number <br/> to the written representation";
            } else {
                return "Lemma already exists";
            }
        }
    }

    public String getCommentIcon() {
        if (lemma.getNote().length() > 0) {
            return "fa fa-comment";
        } else {
            return "fa fa-comment-o";
        }
    }

    public String getCommentIcon(FormData fd) {
        if (fd.getNote().length() > 0) {
            return "fa fa-comment";
        } else {
            return "fa fa-comment-o";
        }
    }

    public boolean isRenderedEntryErrorLabel() {
        if ((lemmAlreadyExists)
                || (lemma.getType().equals("Word"))
                && (lemma.getFormWrittenRepr().contains(" "))
                || (!isAdmissibleLemma)) {
            return true;
        }
        return false;
    }

    public String emptyMessage(String text, String emptyMessage) {
        return emptyMessage(text, text, emptyMessage);
    }

    public String emptyMessage(String test, String text, String emptyMessage) {
        return test == null || test.equals("") ? emptyMessage : text;
    }

    public void openNote() {
        log(Level.INFO, loginController.getAccount(), "OPEN note of Lemma " + lemma.getFormWrittenRepr());
    }

    public void openNote(String form) {
        log(Level.INFO, loginController.getAccount(), "OPEN note of Form " + form);
    }

    public void saveNote() throws IOException, OWLOntologyStorageException {
        log(Level.INFO, loginController.getAccount(), "EDIT note of Lemma " + lemma.getFormWrittenRepr() + " in " + lemma.getNote());
        lexiconManager.saveLemmaNote(lemma, lemmaCopy.getNote());
        lemmaCopy.setNote(lemma.getNote());
        info("template.message.saveLemmaNote.summary", "template.message.saveLemmaNote.description");
    }

    public void saveNote(FormData fd) throws IOException, OWLOntologyStorageException {
        log(Level.INFO, loginController.getAccount(), "EDIT note of Form " + fd.getFormWrittenRepr() + " in " + fd.getNote());
        int order = forms.indexOf(fd);
        lexiconManager.saveFormNote(fd, formsCopy.get(order).getNote());
        formsCopy.get(order).setNote(fd.getNote());
        info("template.message.saveFormNote.summary", "template.message.saveformNote.description");
    }

    public void closeNote() {
        log(Level.INFO, loginController.getAccount(), "CLOSE note of Lemma " + lemma.getFormWrittenRepr());
    }

    public void closeNote(String form) {
        log(Level.INFO, loginController.getAccount(), "CLOSE note of Form " + form);
    }

    // invoked by the controller after an user selected a lemma in the tabview
    public void addLemma(String lemma) {
        this.lemma.clear();
        this.lemma = lexiconManager.getLemmaAttributes(lemma);
        ArrayList<FormData> al = lexiconManager.getFormsOfLemma(lemma, this.lemma.getLanguage(), documentationManager);
        forms.clear();
        forms.addAll(al);
        createLemmaCopy();
        addFormCopy(al);
    }

    // for keeping track of modifies
    private void createLemmaCopy() {
        this.lemmaCopy.setFormWrittenRepr(lemma.getFormWrittenRepr());
        this.lemmaCopy.setGender(lemma.getGender());

        this.lemmaCopy.setPerson(lemma.getPerson());
        this.lemmaCopy.setMood(lemma.getMood());
        this.lemmaCopy.setVoice(lemma.getVoice());

        this.lemmaCopy.setInfo(lemma.getInfo());
        this.lemmaCopy.setLanguage(lemma.getLanguage());
        this.lemmaCopy.setNumber(lemma.getNumber());
        this.lemmaCopy.setPoS(lemma.getPoS());
        this.lemmaCopy.setIndividual(lemma.getIndividual());
        this.lemmaCopy.setType(lemma.getType());
        this.lemmaCopy.setNote(lemma.getNote());
        this.lemmaCopy.setUsedIn(lemma.getUsedIn());
        this.lemmaCopy.setBilingualVariant(lemma.isBilingualVariant());

        this.lemmaCopy.setTransilterationType(lemma.getTransilterationType());
        this.lemmaCopy.setTransliteration(lemma.getTransliteration());
        this.lemmaCopy.setAlphabet(lemma.getAlphabet());
        this.lemmaCopy.setDocumentedIn(copyWordData(lemma.getDocumentedIn()));
        this.lemmaCopy.setVerified(lemma.isVerified());
        this.lemmaCopy.setLinguisticType(lemma.isLinguisticType());

        this.lemmaCopy.setSeeAlso(copyWordData(lemma.getSeeAlso()));
        this.lemmaCopy.setBilingual(copyWordData(lemma.getBilingual()));
        this.lemmaCopy.setMultiword(copyWordData(lemma.getMultiword()));
        this.lemmaCopy.setCollocation(copyWordData(lemma.getCollocation()));
        this.lemmaCopy.setSublemma(copyWordData(lemma.getSublemma()));
        this.lemmaCopy.setReference(copyReferenceData(lemma.getReference()));
        this.lemmaCopy.setOtherLanguage(copyWordData(lemma.getOtherLanguage()));
        this.lemmaCopy.setLinguisticTypeEntry(copyWordData(lemma.getLinguisticTypeEntry()));

        this.lemmaCopy.setExternalAttestation(copyExternalAttestationData(lemma.getExternalAttestation()));
        this.lemmaCopy.setInternalAttestation(copyInternalAttestationData(lemma.getInternalAttestation()));

        this.lemmaCopy.setCorrespondence(lemma.getCorrespondence());
    }

    private ArrayList<Word> copyWordData(ArrayList<Word> alw) {
        ArrayList<Word> _alw = new ArrayList();
        for (Word w : alw) {
            Word _w = new Word();
            _w.setOWLName(w.getOWLName());
            _w.setLanguage(w.getLanguage());
            _w.setWrittenRep(w.getWrittenRep());
            _w.setOWLComp(w.getOWLComp());
            _w.setLabel(w.getLabel());
            _alw.add(_w);
        }
        return _alw;
    }

    private Word copyWordData(Word w) {
        Word _w = new Word();
        _w.setOWLName(w.getOWLName());
        _w.setLanguage(w.getLanguage());
        _w.setWrittenRep(w.getWrittenRep());
        _w.setOWLComp(w.getOWLComp());
        _w.setLabel(w.getLabel());
        return _w;
    }

    public void checkForDocInModification(LemmaData ld) {
        log(Level.INFO, loginController.getAccount(), "EDIT DocumentedIn of " + ld.getFormWrittenRepr());
        ld.setSaveButtonDisabled(false);
    }

    public void checkForDocInModification(FormData fd) {
        log(Level.INFO, loginController.getAccount(), "EDIT DocumentedIn of " + fd.getFormWrittenRepr());
        fd.setSaveButtonDisabled(false);
    }

    private ArrayList<ReferenceData> copyReferenceData(ArrayList<ReferenceData> alr) {
        ArrayList<ReferenceData> _alr = new ArrayList();
        for (ReferenceData rd : alr) {
            ReferenceData _rd = new ReferenceData();
            _rd.setAbbrevTitle(rd.getAbbrevTitle());
            _rd.setAuthor(rd.getAuthor());
            _rd.setCity(rd.getCity());
            _rd.setEditedIn(rd.getEditedIn());
            _rd.setGroupOfDoc(rd.getGroupOfDoc());
            _rd.setPublisher(rd.getPublisher());
            _rd.setTitle(rd.getTitle());
            _rd.setType(rd.getType());
            _rd.setYear(rd.getYear());
            _rd.setLocation(rd.getLocation());
            _alr.add(_rd);
        }
        return _alr;
    }

    private ArrayList<ExternalAttestationData> copyExternalAttestationData(ArrayList<ExternalAttestationData> eadl) {
        ArrayList<ExternalAttestationData> _eadl = new ArrayList();
        for (ExternalAttestationData ead : eadl) {
            ExternalAttestationData _ead = new ExternalAttestationData();
            _ead.setId(ead.getId());
            _ead.setAttestationUriLemma(ead.getAttestationUriLemma());
            _ead.setArticleNumber(ead.getArticleNumber());
            _ead.setAttestationUri(ead.getAttestationUri());
            _ead.setBook(ead.getBook());
            _ead.setChapterNumberRoman(ead.getChapterNumberRoman());
            _ead.setColumnNumber(ead.getColumnNumber());
            _ead.setEntryNumber(ead.getEntryNumber());
            _ead.setFascicle(ead.getFascicle());
            _ead.setFolioNumber(ead.getFolioNumber());
            _ead.setFootnoteNumber(ead.getFootnoteNumber());
            _ead.setGlossaryNumber(ead.getFootnoteNumber());
            _ead.setLineNumber(ead.getLineNumber());
            _ead.setNumberOfGeographicPoint(ead.getNumberOfGeographicPoint());
            _ead.setNumberOfMap(ead.getNumberOfMap());
            _ead.setPageNumber(ead.getPageNumber());
            _ead.setParagraphNumber(ead.getParagraphNumber());
            _ead.setPart(ead.getPart());
            _ead.setSubvolume(ead.getSubvolume());
            _ead.setSvSublemma(ead.getSvSublemma());
            _ead.setVolume(ead.getVolume());
            _ead.setAbbreviationId(ead.getAbbreviationId());
            _ead.setForm(ead.getForm());
            _ead.setSec(ead.getSec());
            _ead.setOther(ead.getOther());
            _eadl.add(_ead);
        }
        return _eadl;
    }

    private ArrayList<InternalAttestationData> copyInternalAttestationData(ArrayList<InternalAttestationData> iadl) {
        ArrayList<InternalAttestationData> _iadl = new ArrayList();
        for (InternalAttestationData iad : iadl) {
            InternalAttestationData _iad = new InternalAttestationData();
            _iad.setId(iad.getId());
            _iad.setAttestationUriLemma(iad.getAttestationUriLemma());
            _iad.setDocAbbreviation(iad.getDocAbbreviation());
            _iad.setManSigla(iad.getManSigla());
            _iad.setAbbreviationId(iad.getAbbreviationId());
            _iad.setAttestationUri(iad.getAttestationUri());
            _iad.setChapterNumber(iad.getChapterNumber());
            _iad.setLineNumber(iad.getLineNumber());
//            _iad.setListEntryLetter(iad.getListEntryLetter());
//            _iad.setListEntryNumber(iad.getListEntryNumber());
            _iad.setListEntry(iad.getListEntry());
            _iad.setPageNumber(iad.getPageNumber());
            _iad.setParagraphNumber(iad.getParagraphNumber());
            _iad.setSectionNumber(iad.getSectionNumber());
            _iad.setSiglum(iad.getSiglum());
            _iadl.add(_iad);
        }
        return _iadl;
    }

    private String[] getSiglum(String sig) {
        String[] _sig = null;
        if (sig != null) {
            for (int i = 0; i < sig.length(); i++) {
                _sig[i] = Character.toString(sig.charAt(i));
            }
        }
        return _sig;
    }

    private ReferenceData copyReferenceData(ReferenceData rd) {
        ReferenceData _rd = new ReferenceData();
        _rd.setAbbrevTitle(rd.getAbbrevTitle());
        _rd.setAuthor(rd.getAuthor());
        _rd.setCity(rd.getCity());
        _rd.setEditedIn(rd.getEditedIn());
        _rd.setGroupOfDoc(rd.getGroupOfDoc());
        _rd.setPublisher(rd.getPublisher());
        _rd.setTitle(rd.getTitle());
        _rd.setType(rd.getType());
        _rd.setYear(rd.getYear());
        _rd.setLocation(rd.getLocation());
        return _rd;
    }

    private void addFormCopy(FormData fd) {
        formsCopy.add(0, copyFormData(fd));
    }

    private void updateFormCopy(FormData fd, int order) {
        formsCopy.remove(order);
        formsCopy.add(order, copyFormData(fd));
    }

    private void addFormCopy(ArrayList<FormData> fdList) {
        formsCopy.clear();
        for (FormData fd : fdList) {
            formsCopy.add(copyFormData(fd));
        }
    }

    private void removeFormCopy(int order) {
        formsCopy.remove(order);
    }

    private FormData copyFormData(FormData fd) {
        FormData _fd = new FormData();
        _fd.setAlphabet(fd.getAlphabet());
        _fd.setAlphabeticalType(fd.isAlphabeticalType());
        _fd.setFormWrittenRepr(fd.getFormWrittenRepr());
        _fd.setGender(fd.getGender());

        _fd.setPerson(fd.getPerson());
        _fd.setMood(fd.getMood());
        _fd.setVoice(fd.getVoice());

        _fd.setGraphicType(fd.isGraphicType());
        _fd.setGraphophoneticType(fd.isGraphophoneticType());
        _fd.setNote(fd.getNote());
        _fd.setDocumentedIn(copyWordData(fd.getDocumentedIn()));
        _fd.setInternalAttestation(copyInternalAttestationData(fd.getInternalAttestation()));
        _fd.setUnspecifiedType(fd.isUnspecifiedType());
        _fd.setMorphosyntacticType(fd.isMorphosyntacticType());
        _fd.setLanguage(fd.getLanguage());
        _fd.setMorphologicalType(fd.isMorphologicalType());
        _fd.setLinguisticType(fd.isLinguisticType());
        _fd.setNumber(fd.getNumber());
        _fd.setPoS(fd.getPoS());
        _fd.setTransliteration(fd.getTransliteration());
        _fd.setTransilterationType(fd.getTransilterationType());
        _fd.setIndividual(fd.getIndividual());
        _fd.setUsedIn(fd.getUsedIn());
        _fd.setLinguisticTypeEntry(copyWordData(fd.getLinguisticTypeEntry()));
        return _fd;
    }

    // invoked by an user that adds a new form of a lemma
    public void addForm() {
        log(Level.INFO, loginController.getAccount(), "ADD empty Form box");
        FormData fd = new FormData();
        setFormDefaultValues(fd);
        forms.add(0, fd);
        addFormCopy(fd);
    }

    private void setFormDefaultValues(FormData fd) {
        fd.setLanguage(lemma.getLanguage());
        fd.setAlphabet("Latn");
        fd.setPoS(lemma.getPoS());
    }

    // invoked by the controller after an user selected a form in the tabview
    public void addForm(String form) {
        this.lemma.clear();
        this.lemma = lexiconManager.getLemmaEntry(form, documentationManager);
        ArrayList<FormData> al = lexiconManager.getFormsOfLemma(getLemma().getIndividual(), this.lemma.getLanguage(), documentationManager);
        forms.clear();
        forms.addAll(al);
        createLemmaCopy();
        addFormCopy(al);
    }

    // invoked by the controller after an user selected a sense in the tabview
    public void addForms(String sense) {
        this.lemma.clear();
        this.lemma = lexiconManager.getLemmaOfSense(sense);
        ArrayList<FormData> al = lexiconManager.getFormsOfLemma(getLemma().getIndividual(), this.lemma.getLanguage(), documentationManager);
        forms.clear();
        forms.addAll(al);
        createLemmaCopy();
        addFormCopy(al);
    }

    // invoked by the controller after an user selected add sense to lemma
    public void addSense() throws IOException, OWLOntologyStorageException {
        log(Level.INFO, loginController.getAccount(), "ADD empty Sense box");
        lexiconCreationViewSenseDetail.saveSense(lemma);
        lexiconCreationViewSenseDetail.setSenseToolbarRendered(true);
    }

    public void removeForm(FormData fd) throws IOException, OWLOntologyStorageException {
        log(Level.INFO, loginController.getAccount(), "DELETE Form " + fd.getFormWrittenRepr());
        lexiconManager.deleteForm(fd);
        info("template.message.deleteForm.summary", "template.message.deleteForm.description", fd.getFormWrittenRepr());
        // remove the form from forms and copyForms
        int order = forms.indexOf(fd);
        forms.remove(fd);
        removeFormCopy(order);
        String currentLanguage = lexiconCreationControllerTabViewList.getLexiconLanguage();
        lexiconCreationControllerTabViewList.initFormTabView(currentLanguage);
        lexiconCreationControllerTabViewList.initAttestationTabView();
    }

    public void saveForm(FormData fd) throws IOException, OWLOntologyStorageException {
        fd.setSaveButtonDisabled(true);
        fd.setDeleteButtonDisabled(false);
        int order = forms.indexOf(fd);
        if (formsCopy.get(order).getFormWrittenRepr() == null) {
            // saving due to new form action
            log(Level.INFO, loginController.getAccount(), "SAVE new Form " + fd.getFormWrittenRepr());
            lexiconManager.saveForm(fd, lemma);
        } else {
            // saving due to a form modification action
            if (isSameWrittenRep(fd.getFormWrittenRepr(), order)) {
                // modification does not concern form written representation
                log(Level.INFO, loginController.getAccount(), "SAVE updated Form " + fd.getFormWrittenRepr());
                lexiconManager.updateForm(formsCopy.get(order), fd, lemma);
            } else {
                // modification concerns also form written representation
                log(Level.INFO, loginController.getAccount(), "SAVE updated Form with renaming from " + formsCopy.get(order).getFormWrittenRepr() + " to " + fd.getFormWrittenRepr());
                lexiconManager.saveFormWithIRIRenaming(formsCopy.get(order), fd, lemma);
            }
        }
        info("template.message.saveForm.summary", "template.message.saveForm.description", fd.getFormWrittenRepr());
        String currentLanguage = lexiconCreationControllerTabViewList.getLexiconLanguage();
        lexiconCreationControllerTabViewList.initFormTabView(currentLanguage);
        lexiconCreationControllerTabViewList.initAttestationTabView();
        updateFormCopy(fd, order);
    }

    private boolean isSameWrittenRep(String wr, int order) {
        if (formsCopy.get(order).getFormWrittenRepr().equals(wr)) {
            return true;
        } else {
            return false;
        }
    }

    public void formNameKeyupEvent(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        String formPart = ((String) e.getComponent().getAttributes().get("value"));
        String currentLanguage = lexiconCreationControllerTabViewList.getLexiconLanguage();
        List<Map<String, String>> formList = lexiconManager.formsList(currentLanguage);
        if (contains(formList, formPart, fd.getLanguage())
                && (isSameLemma(formList, formPart))
                && (!isSameForm(fd, formPart))) {
            formAlreadyExists = true;
            fd.setSaveButtonDisabled(true);
        } else {
            formAlreadyExists = false;
            fd.setSaveButtonDisabled(false);
        }
        log(Level.INFO, loginController.getAccount(), "UPDATE Form name from " + fd.getFormWrittenRepr() + " to " + formPart);
        fd.setFormWrittenRepr(formPart);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    private boolean isSameForm(FormData fd, String part) {
        int order = forms.indexOf(fd);
        if (formsCopy.get(order).getFormWrittenRepr() == null) {
            return false;
        } else {
            if (!formsCopy.get(order).getFormWrittenRepr().equals(part)) {
                return false;
            } else {
                return true;
            }
        }
    }

    public void lemmaNameKeyupEvent(String lemmaPart, List<Map<String, String>> lemmaList) {
        if (contains(lemmaList, lemmaPart, lemma.getLanguage()) && (!lemmaPart.equals(lemmaCopy.getFormWrittenRepr()))) {
            lemmAlreadyExists = true;
            /**/ isAdmissibleLemma = true;
            lemma.setSaveButtonDisabled(true);
        } else {
            /**/ if (isAdmissibleLemma(lemmaPart, lemma.getType())) {
                lemmAlreadyExists = false;
                /**/ isAdmissibleLemma = true;
                lemma.setSaveButtonDisabled(false);
                /**/            } else {
                /**/ isAdmissibleLemma = false;
                /**/ lemma.setSaveButtonDisabled(true);
                /**/            }
        }
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma name from " + lemma.getFormWrittenRepr() + " to " + lemmaPart);
        lemma.setSaveButtonDisabled(isSavableLemma() || (lemma.getFormWrittenRepr().contains(" ")));
        lemma.setFormWrittenRepr(lemmaPart);
    }

    private boolean isAdmissibleLemma(String w, String type) {
        if (type.equals("Word")) {
            return isAdmissibleWord(w);
        } else {
            return isAdmissibleMultiwordWord(w);
        }
    }

    private boolean isAdmissibleWord(String w) {
        if (w.matches(ADMISSIBLE_WORD_REGEXP) || w.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isAdmissibleMultiwordWord(String mw) {
        for (String comp : mw.split(" ")) {
            if (!(comp.matches(ADMISSIBLE_MULTIWORD_REGEXP) || mw.isEmpty())) {
                return false;
            }
        }
        return true;
    }

    public void lemmaMultiwordKeyupEvent(String lemmaPart, List<Map<String, String>> lemmaList) {
        if (!lemmaPart.isEmpty()) {
            int spacesNumber = getSpaceCount(lemmaPart, " ");
            lemma.getMultiword().clear();
            String[] splitted = lemmaPart.split(" ");
            for (int i = 0; i < spacesNumber; i++) {
                setMultiword(splitted, i, lemmaList);
            }
            if (spacesNumber > 0) {
                if ((splitted.length == spacesNumber + 1) && (!splitted[spacesNumber].isEmpty())) {
                    setMultiword(splitted, spacesNumber, lemmaList);
                }
            }
            lemmaNameKeyupEvent(lemmaPart, lemmaList);
        } else {
            lemma.getMultiword().clear();
        }
        lemma.setSaveButtonDisabled(isSavableLemmaMultiword());
    }

    private boolean isSavableLemmaMultiword() {
        if (isSavableLemma() || lemma.getFormWrittenRepr().split(" ").length <= 1) {
            return true;
        } else {
            return false;
        }
    }

    private void setMultiword(String[] s, int i, List<Map<String, String>> lemmaList) {
//        Word w = getWordsCandidates(s[i], lemmaList);
        Word w = getComponent(s, i, lemmaList);
        lemma.getMultiword().add(w);
    }

//    private Word getWordsCandidates(String wr, List<Map<String, String>> lemmaList) {
//        Word w = new Word();
//        boolean firstWord = true;
//        Collections.sort(lemmaList, new LexiconComparator("writtenRep"));
//        for (Map<String, String> m : lemmaList) {
//            if ((wr.equals(m.get("writtenRep"))) && (!m.get("writtenRep").isEmpty())) {
//                if (firstWord) {
//                    setWord(m, w);
//                    firstWord = false;
//                }
//                CandidateWord cw = new CandidateWord();
//                setCandidateWord(m, cw);
//                w.getCandidates().add(cw);
//            }
//        }
//        if (firstWord) {
//            w.setWrittenRep(wr + " not found");
//            w.setViewButtonDisabled(true);
//        }
//        return w;
//    }
//    private void setWord(Map<String, String> m, Word w) {
    //        w.setWrittenRep(m.get("writtenRep"));
    //        w.setOWLName(m.get("individual"));
    //        w.setLanguage(m.get("lang"));
    //        w.setLabel(w.getWrittenRep() + "@" + w.getLanguage());
    //    }
    //
    //    private void setCandidateWord(Map<String, String> m, CandidateWord cw) {
    //        cw.setWrittenRep(m.get("writtenRep"));
    //        cw.setOWLName(m.get("individual"));
    //        cw.setLanguage(m.get("lang"));
    //    }
    private Word getComponent(String[] s, int i, List<Map<String, String>> lemmaList) {
        Word w = new Word();
        w.setWrittenRep(s[i] + " not found");
        w.setViewButtonDisabled(true);
        Collections.sort(lemmaList, new LexiconComparator("writtenRep"));
        for (Map<String, String> m : lemmaList) {
//            || (writtenRep.matches(wr + "([0-9]*[#|\\*]*)*")
            if ((s[i].equals(m.get("writtenRep")) || (m.get("writtenRep").matches(Pattern.quote(s[i] + MULTIWORD_COMPONENT_REGEXP)))) && (!m.get("writtenRep").isEmpty())) {
//            if ((m.get("writtenRep").matches(s[i] + MULTIWORD_COMPONENT_REGEXP)) && (!m.get("writtenRep").isEmpty())) {
                setComponent(m, w, i);
                break;
            }
        }
        return w;
    }

    private void setComponent(Map<String, String> m, Word w, int position) {
        w.setWrittenRep(m.get("writtenRep"));
        w.setOWLName(m.get("individual"));
        w.setLanguage(m.get("lang"));
        w.setLabel(w.getWrittenRep() + "@" + w.getLanguage());
        w.setViewButtonDisabled(false);
        if (!isNewAction()) {
            String OWLComp = lexiconManager.getComponentAtPosition(lemma.getIndividual(), Integer.toString(position));
            w.setOWLComp(OWLComp);
        }
    }

    public void lemmaNameKeyupEvent(AjaxBehaviorEvent e) {
        String lemmaPart = (String) e.getComponent().getAttributes().get("value");
        String currentLanguage = lexiconCreationControllerTabViewList.getLexiconLanguage();
        List<Map<String, String>> lemmaList = lexiconManager.lemmasList(currentLanguage);
        if (lemma.getType().equals("Word")) {
            lemmaNameKeyupEvent(lemmaPart, lemmaList);
        } else {
            lemmaMultiwordKeyupEvent(lemmaPart, lemmaList);
        }
    }

    private int getSpaceCount(String str, String toFind) {
        String s = str.replaceAll("\\s+", " ");
        int lastIndex = 0;
        int count = 0;
        while (lastIndex != -1) {
            lastIndex = s.indexOf(toFind, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += toFind.length();
            }
        }
        return count;
    }

    // returns true if the lemma (or form) already exists in a specific lexicon
    private boolean contains(List<Map<String, String>> l, String form, String lang) {
        boolean contains = false;
        for (Map<String, String> m : l) {
            if (!lang.isEmpty()) {
                if (m.get("writtenRep").equals(form.trim()) && (m.get("lang").equals(lang))) {
                    contains = true;
                    break;
                }
            } else {
                if (m.get("writtenRep").equals(form.trim())) {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    private boolean isSameLemma(List<Map<String, String>> l, String formPart) {
        boolean same = false;
        for (Map<String, String> m : l) {
            if (m.get("writtenRep").equals(formPart) && (m.get("lemma").equals(lemma.getIndividual()))) {
                same = true;
                break;
            }
        }
        return same;
    }

    private void lemmaNameKeyupEvent() {
        String lemmaPart = lemma.getFormWrittenRepr();
        String currentLanguage = lexiconCreationControllerTabViewList.getLexiconLanguage();
        List<Map<String, String>> lemmaList = lexiconManager.lemmasList(currentLanguage);
        if (contains(lemmaList, lemmaPart, lemma.getLanguage()) && (!lemmaPart.equals(lemmaCopy.getFormWrittenRepr()))) {
            lemmAlreadyExists = true;
            lemma.setSaveButtonDisabled(true);
        } else {
            /**/ if (isAdmissibleLemma(lemmaPart, lemma.getType())) {
                lemmAlreadyExists = false;
                /**/ isAdmissibleLemma = true;
                lemma.setSaveButtonDisabled(false);
                /**/            } else {
                /**/ isAdmissibleLemma = false;
                /**/ lemma.setSaveButtonDisabled(true);
                /**/            }
        }
        lemma.setSaveButtonDisabled(isSavableLemma() || (lemma.getFormWrittenRepr().contains(" ")));
        lemma.setFormWrittenRepr(lemmaPart);
    }

    // invoked when a word of a multiword is chosen from the menu
//    public void wordChanged(AjaxBehaviorEvent e) {
//        String v = (String) e.getComponent().getAttributes().get("value");
//        String[] params = v.split("@");
//        String wr = params[0];
//        String lang = params[1];
//        String owlName = params[2];
//        Word w = (Word) e.getComponent().getAttributes().get("newWord");
//        int idx = lemma.getMultiword().indexOf(w);
//        lemma.getMultiword().get(idx).setWrittenRep(wr);
//        lemma.getMultiword().get(idx).setLanguage(lang);
//        lemma.getMultiword().get(idx).setOWLName(owlName);
//
//    }
    private void ckeckLemmaSavability() {
        if (!lemma.getType().equals("Word")) {
            lemma.setSaveButtonDisabled(isSavableLemma() || (!lemma.getFormWrittenRepr().contains(" ")));
        } else {
            if (isSavableLemma()) {
                lemma.setSaveButtonDisabled(true);
            } else {
                if (lemma.getFormWrittenRepr().contains(" ")) {
                    lemma.setSaveButtonDisabled(true);
                } else {
                    lemma.setSaveButtonDisabled(false);
                }
            }
        }
    }

    public void languageChanged(AjaxBehaviorEvent e) {
        String lang = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma language of " + lemma.getFormWrittenRepr() + " to " + lang);
        lemma.setLanguage(lang);
        // check again if the lemma already exists in the new language
        lemmaNameKeyupEvent();
        ckeckLemmaSavability();
    }

    public void lemmaInfoChanged(AjaxBehaviorEvent e) {
        String info = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma info of " + lemma.getFormWrittenRepr() + " to " + info);
        lemma.setInfo(info);
        addFormButtonDisabled = true;
        ckeckLemmaSavability();
    }

    public void verifiedChanged() {
        log(Level.INFO, loginController.getAccount(), "UPDATE Verified field of " + lemma.getFormWrittenRepr() + " to " + lemma.isVerified());
        addFormButtonDisabled = true;
        ckeckLemmaSavability();
    }

    public void lemmaGenderChanged(AjaxBehaviorEvent e) {
        String gender = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma gender of " + lemma.getFormWrittenRepr() + " to " + gender);
        lemma.setGender(gender);
        addFormButtonDisabled = true;
        ckeckLemmaSavability();
    }

    public void lemmaPersonChanged(AjaxBehaviorEvent e) {
        String person = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma person of " + lemma.getFormWrittenRepr() + " to " + person);
        lemma.setPerson(person);
        addFormButtonDisabled = true;
        lemma.setSaveButtonDisabled(false);
    }

    public void lemmaMoodChanged(AjaxBehaviorEvent e) {
        String mood = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma mood of " + lemma.getFormWrittenRepr() + " to " + mood);
        lemma.setMood(mood);
        addFormButtonDisabled = true;
        lemma.setSaveButtonDisabled(false);
    }

    public void lemmaVoiceChanged(AjaxBehaviorEvent e) {
        String voice = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma voice of " + lemma.getFormWrittenRepr() + " to " + voice);
        lemma.setVoice(voice);
        addFormButtonDisabled = true;
        lemma.setSaveButtonDisabled(false);
    }

    public void lemmaNumberChanged(AjaxBehaviorEvent e) {
        String number = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma number of " + lemma.getFormWrittenRepr() + " to " + number);
        lemma.setNumber(number);
        addFormButtonDisabled = true;
        ckeckLemmaSavability();
    }

    public void lemmaPoSChanged(AjaxBehaviorEvent e) {
        String pos = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma Part-of-Speech of " + lemma.getFormWrittenRepr() + " to " + pos);
        lemma.setPoS(pos);
        addFormButtonDisabled = true;
        ckeckLemmaSavability();
    }

    public void lemmaUsedInChanged(AjaxBehaviorEvent e) {
        String usedIn = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma used in languages of " + lemma.getFormWrittenRepr() + " to " + usedIn);
        lemma.setUsedIn(usedIn);
        addFormButtonDisabled = true;
        ckeckLemmaSavability();
    }

    public void lemmaAlphabet(AjaxBehaviorEvent e) {
        String alph = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma alphabet of " + lemma.getFormWrittenRepr() + " to " + alph);
        lemma.setAlphabet(alph);
        addFormButtonDisabled = true;
        ckeckLemmaSavability();
    }

    public void lemmaTransliteration(AjaxBehaviorEvent e) {
        String tr = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma transliteration " + lemma.getFormWrittenRepr() + " to " + tr);
        lemma.setTransliteration(tr);
        addFormButtonDisabled = true;
        ckeckLemmaSavability();
    }

    public void changeLemmaTransliterationType(AjaxBehaviorEvent e) {
        String trType = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma transliteration type " + lemma.getFormWrittenRepr() + " to " + trType);
        lemma.setTransilterationType(trType);
        addFormButtonDisabled = true;
        ckeckLemmaSavability();
    }

    public void lemmaBiblioTypeChanged(ReferenceData rd) {
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma biblio type of " + lemma.getFormWrittenRepr() + " to " + rd.getType());
//        addFormButtonDisabled = true;
//        ckeckLemmaSavability();
    }

    public void lemmaBiblioGroupChanged(ReferenceData rd) {
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma biblio group of " + lemma.getFormWrittenRepr() + " to " + rd.getGroupOfDoc());
//        addFormButtonDisabled = true;
//        ckeckLemmaSavability();
    }

    public void lemmaBiblioAbbrTitleChanged(ReferenceData rd) {
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma biblio abbreviation title of " + lemma.getFormWrittenRepr() + " to " + rd.getAbbrevTitle());
//        addFormButtonDisabled = true;
//        ckeckLemmaSavability();
    }

    public void lemmaBiblioLocationChanged(ReferenceData rd) {
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma biblio location of " + lemma.getFormWrittenRepr() + " to " + rd.getLocation());
//        addFormButtonDisabled = true;
//        ckeckLemmaSavability();
        if (rd.getLocation().isEmpty()) {
            rd.setSaveButtonDisabled(true);
        } else {
            rd.setSaveButtonDisabled(false);
        }
    }

    public void changeMultiwordType(AjaxBehaviorEvent e) {
        String type = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Multiword Lemma type of " + lemma.getFormWrittenRepr() + " to " + type);
        lemma.setType(type);
        lemma.setSaveButtonDisabled(isSavableLemmaMultiword());
        // TODO: filter by language !!!
//        getLexicaLanguages();
    }

    public void bilingualVariantType(AjaxBehaviorEvent e) {
        boolean type = (boolean) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma bilingual type of " + lemma.getFormWrittenRepr() + " to " + type);
        lemma.setBilingualVariant(type);
        addFormButtonDisabled = true;
        ckeckLemmaSavability();
    }

    public void correspondenceCheck(AjaxBehaviorEvent e) {
        boolean type = (boolean) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE correspondence check box of " + lemma.getFormWrittenRepr() + " to " + type);
        lemma.setCorrespondence(String.valueOf(type));
        addFormButtonDisabled = true;
        ckeckLemmaSavability();
    }

    public void formAlphabet(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        String alphabet = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form alphabet of " + fd.getFormWrittenRepr() + " to " + alphabet);
        fd.setAlphabet(alphabet);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formTransliteration(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        String trans = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form transliteration of " + fd.getFormWrittenRepr() + " to " + trans);
        fd.setTransliteration(trans);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formPoSChanged(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        String pos = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form Part-of-Speech of " + fd.getFormWrittenRepr() + " to " + pos);
        fd.setPoS(pos);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formNumberChanged(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        String number = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form number of " + fd.getFormWrittenRepr() + " to " + number);
        fd.setNumber(number);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formUsedInChanged(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        String usedIn = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form used in language of " + fd.getFormWrittenRepr() + " to " + usedIn);
        fd.setUsedIn(usedIn);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formGenderChanged(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        String gender = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form gender of " + fd.getFormWrittenRepr() + " to " + gender);
        fd.setGender(gender);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formPersonChanged(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        String person = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form person of " + fd.getFormWrittenRepr() + " to " + person);
        fd.setPerson(person);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formMoodChanged(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        String mood = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form mood of " + fd.getFormWrittenRepr() + " to " + mood);
        fd.setMood(mood);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formVoiceChanged(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        String voice = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form voice of " + fd.getFormWrittenRepr() + " to " + voice);
        fd.setVoice(voice);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formAlphabeticalType(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        Boolean type = (Boolean) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form alphabetical type of " + fd.getFormWrittenRepr() + " to " + type.toString());
        fd.setAlphabeticalType(type);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formGraphicType(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        Boolean type = (Boolean) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form graphic type of " + fd.getFormWrittenRepr() + " to " + type.toString());
        fd.setGraphicType(type);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formMorphologicalType(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        Boolean type = (Boolean) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form morphological type of " + fd.getFormWrittenRepr() + " to " + type.toString());
        fd.setMorphologicalType(type);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formGraphophoneticType(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        Boolean type = (Boolean) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form graphophonetic type of " + fd.getFormWrittenRepr() + " to " + type.toString());
        fd.setGraphophoneticType(type);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formUnspecifiedType(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        Boolean type = (Boolean) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form unspecified type of " + fd.getFormWrittenRepr() + " to " + type.toString());
        fd.setUnspecifiedType(type);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void formLinguisticType(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        Boolean type = (Boolean) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form linguistic type of " + fd.getFormWrittenRepr() + " to " + type.toString());
        if (!type) {
            resetLiguisticVariant(fd.getLinguisticTypeEntry());
        }
        fd.setLinguisticType(type);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void lemmaLinguisticType(AjaxBehaviorEvent e) {
        Boolean type = (Boolean) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Lemma linguistic type of " + lemma.getFormWrittenRepr() + " to " + type.toString());
        if (!type) {
            resetLiguisticVariant(lemma.getLinguisticTypeEntry());
        }
        lemma.setLinguisticType(type);
        lemma.setSaveButtonDisabled(isSavableLemma());
    }

    private void resetLiguisticVariant(Word w) {
        w.setLabel("");
        w.setOWLComp("");
        w.setOWLName("");
        w.setLanguage("");
        w.setWrittenRep("");
        w.setDeleteButtonDisabled(false);
        w.setViewButtonDisabled(false);
    }

//    public void formBilingualType(AjaxBehaviorEvent e) {
//        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
//        FormData fd = (FormData) component.getAttributes().get("form");
//        Boolean type = (Boolean) e.getComponent().getAttributes().get("value");
//        fd.setBilingualType(type);
//        fd.setSaveButtonDisabled(isSavableForm(fd));
//    }
    public void formMorphosyntacticType(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        Boolean type = (Boolean) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form morphosyntactic type of " + fd.getFormWrittenRepr() + " to " + type.toString());
        fd.setMorphosyntacticType(type);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    public void changeTransliterationType(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        FormData fd = (FormData) component.getAttributes().get("form");
        String type = (String) e.getComponent().getAttributes().get("value");
        log(Level.INFO, loginController.getAccount(), "UPDATE Form transliteration type of " + fd.getFormWrittenRepr() + " to " + type.toString());
        fd.setTransilterationType(type);
        fd.setSaveButtonDisabled(isSavableForm(fd));
    }

    private boolean isSavableLemma() {
        if ((lemma.getFormWrittenRepr() == null) || (lemma.getLanguage() == null)) {
            return true;
        } else {
            if ((lemma.getFormWrittenRepr().replaceAll("\\s", "").length() > 0) && (!lemmAlreadyExists)
                    /**/ && (isAdmissibleLemma)
                    && (lemma.getLanguage().length() > 0)) {
                return false;
            } else {
                return true;
            }
        }
    }

    private boolean isSavableForm(FormData fd) {
        if ((fd.getFormWrittenRepr() == null)) {
            return true;
        } else {
            if ((fd.getFormWrittenRepr().replaceAll("\\s", "").length() > 0)
                    && (!formAlreadyExists)) {
                // written rep correctly inserted
                if (fd.getTransilterationType().equals(LexiconQuery.NO_TRANSLITERATION_FOUND)) {
                    // no transliteration type selected
                    return false;
                } else {
                    // transliteration type selected
                    if (fd.getTransliteration() == null) {
                        // transliteration field is null
                        return true;
                    } else {
                        // transliteration field is not null
                        if (!fd.getTransliteration().isEmpty()) {
                            // transliteration field is not null and it is not empty
                            return false;
                        } else {
                            // transliteration field is not null but it is empty
                            return true;
                        }
                    }
                }
            } else {
                return true;
            }
        }
    }

    public void removeLemma() throws IOException, OWLOntologyStorageException {
//        removeFormsAndSenses();
        log(Level.INFO, loginController.getAccount(), "DELETE Lemma " + lemma.getFormWrittenRepr());
        lexiconManager.deleteLemma(lemmaCopy, forms, lexiconCreationViewSenseDetail.getSenses());
        info("template.message.deleteLemma.summary", "template.message.deleteLemma.description", lemma.getFormWrittenRepr());
        forms.clear();
        formsCopy.clear();
        lemma.clear();
        lemmaCopy.clear();
        lemmaRendered = false;
        addFormButtonDisabled = true;
        lexiconCreationViewSenseDetail.getSenses().clear();
        lexiconCreationViewSenseDetail.setAddSenseButtonDisabled(true);
        lexiconCreationControllerRelationDetail.resetRelationDetails();
        String currentLanguage = lexiconCreationControllerTabViewList.getLexiconLanguage();
        lexiconCreationControllerTabViewList.initLemmaTabView(currentLanguage);
        lexiconCreationControllerTabViewList.initFormTabView(currentLanguage);
        lexiconCreationControllerTabViewList.initAttestationTabView();
    }

//    private void removeFormsAndSenses() throws IOException, OWLOntologyStorageException {
//        for (FormData fd : forms) {
//            Logger.getLogger("omegalex").info("Action Delete Form " + fd.getFormWrittenRepr() + " of lemma " + lemma.getFormWrittenRepr());
//            lexiconManager.deleteForm(fd);
//        }
//        for (SenseData sd : lexiconCreationViewSenseDetail.getSenses()) {
//            Logger.getLogger("omegalex").info("Action Delete Sense " + sd.getName() + " of lemma " + lemma.getFormWrittenRepr());
//            lexiconManager.deleteSense(sd);
//        }
//    }
    public void resetFormDetails() {
        forms.clear();
        formsCopy.clear();
        lemma.clear();
        lemmaCopy.clear();
    }

    // constructor of uri individuals
    private String getIRI(String... params) {
        StringBuilder iri = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            iri.append(sanitize(params[i]));
            if (i < (params.length - 1)) {
                iri.append("_");
            }
        }
        return iri.toString();
    }

    public void saveLemma() throws IOException, OWLOntologyStorageException {
        lemma.setSaveButtonDisabled(true);
        lemma.setDeleteButtonDisabled(false);
        addFormButtonDisabled = false;
        if (lemmaCopy.getFormWrittenRepr().isEmpty()) {
            // saving due to a new lemma action
            saveLemma(false, true);
            String entry = getIRI(lemma.getFormWrittenRepr(), lemma.getLanguage(), "lemma");
            // check if the lexical entry is available and lock it
            lexiconManager.lock(entry);
            setLocked(false);
            setPanelHeader("Lexical entry");
            lexiconCreationViewSenseDetail.setLocked(false);
            log(Level.INFO, loginController.getAccount(), "LOCK the lexical entry related to " + entry);
        } else {
            // saving due to a lemma modification action
            if (lemma.getFormWrittenRepr().equals(lemmaCopy.getFormWrittenRepr())) {
                // modification does not concern lemma written representation
                saveLemma(false, false);
            } else {
                // modification concerns also lemma written representation
                saveLemma(true, false);
            }
        }
        createLemmaCopy();
        newAction = false;
//        lexiconCreationControllerTabViewList.initAttestationTabView();
//        String currentLanguage = lexiconCreationControllerTabViewList.getLexiconLanguage();
//        lexiconCreationControllerTabViewList.initLemmaTabView(currentLanguage);
        lexiconCreationViewSenseDetail.setSenseRendered(true);
    }

    private void saveLemma(boolean renaming, boolean newAction) throws IOException, OWLOntologyStorageException {
        info("template.message.saveLemma.summary", "template.message.saveLemma.description", lemma.getFormWrittenRepr());
        if (renaming) {
            saveForUpdateWithRenameAction();
            lexiconCreationControllerTabViewList.initAttestationTabView();
            updateTabViewLists();
        } else {
            if (newAction) {
                saveForNewAction();
                updateTabViewLists();
            } else {
                saveForUpdateAction();
                lexiconCreationControllerTabViewList.initAttestationTabView();
            }
        }
    }

    private void saveForNewAction() throws IOException, OWLOntologyStorageException {
        if (lemma.getType().equals("Word")) {
            // word case
            log(Level.INFO, loginController.getAccount(), "SAVE new Lemma and its default sense " + lemma.getFormWrittenRepr());
//            lexiconManager.saveLemma(lemma);
            lexiconCreationViewSenseDetail.saveDefaultSense(lemma, "Word");
        } else {
            // multiword case
            log(Level.INFO, loginController.getAccount(), "SAVE new Multiword and its default sense " + lemma.getFormWrittenRepr());
//            lexiconManager.saveLemmaForNewAction(lemma);
            lexiconCreationViewSenseDetail.saveDefaultSense(lemma, "Multiword");
        }
//        lexiconCreationViewSenseDetail.saveDefaultSense(lemma);
        lexiconCreationViewSenseDetail.setSenseToolbarRendered(true);
        setNewAction(false);
    }

    private void saveForUpdateAction() throws IOException, OWLOntologyStorageException {
        if (lemma.getType().equals("Word")) {
            // word case
            log(Level.INFO, loginController.getAccount(), "UPDATE Lemma " + lemma.getFormWrittenRepr());
            lexiconManager.updateLemma(lemmaCopy, lemma);
        } else {
            // multiword case
            log(Level.INFO, loginController.getAccount(), "UPDATE Multiword Lemma " + lemma.getFormWrittenRepr());
            // it is possible that some word has an empty writtenRep, 
            // so the deletion of the related fields (OWLName and lang) it is recommended
            lexiconManager.updateLemmaForMultiword(lemmaCopy, lemma);
        }
    }

    private void saveForUpdateWithRenameAction() throws IOException, OWLOntologyStorageException {
        if (lemma.getType().equals("Word")) {
            // word case
            log(Level.INFO, loginController.getAccount(), "SAVE updated Lemma with renaming from " + lemmaCopy.getFormWrittenRepr() + " to " + lemma.getFormWrittenRepr());
            lexiconManager.saveLemmaWithIRIRenaming(lemmaCopy, lemma, formsCopy);
            lexiconCreationViewSenseDetail.addSense(lemma.getIndividual(), "Lemma");
        } else {
            // multiword case
            log(Level.INFO, loginController.getAccount(), "SAVE updated Multiword Lemma with renaming from " + lemmaCopy.getFormWrittenRepr() + " to " + lemma.getFormWrittenRepr());
            lexiconManager.saveMultiwordLemmaWithIRIRenaming(lemmaCopy, lemma, formsCopy);
            lexiconCreationViewSenseDetail.addSense(lemma.getIndividual(), "Lemma");
        }

    }

    private void updateTabViewLists() {
        String currentLanguage = lexiconCreationControllerTabViewList.getLexiconLanguage();
        lexiconCreationControllerTabViewList.initLemmaTabView(currentLanguage);
        lexiconCreationControllerTabViewList.initFormTabView(currentLanguage);
    }

    public ArrayList<String> getLexicaLanguages() {
        ArrayList<String> al = new ArrayList();
//        if (lemma.getType().equals("Word")) {
        for (String lang : lexiconManager.lexicaLanguagesList()) {
            al.add(lang);
        }
        Collections.sort(al);
        return al;
//        } else {
//            return getLexicaLanguages(lemma.getType());
//        }
    }

    // invoked when lemma is a multiword in order to get the languages according to the multiword type
    public ArrayList<String> getLexicaLanguages(String mwType) {
        ArrayList<String> al = new ArrayList();
        for (String lang : lexiconManager.lexicaLanguagesList()) {
            if (mwType.equals("bilingual")) {
                if (lang.length() > 4) {
                    al.add(lang);
                }
            } else {
                if (lang.length() <= 4) {
                    al.add(lang);
                }
            }
        }
        return al;
    }
//------------------------------------------------------------------------------

    public List<String> completeTextSeeAOc(String _lemma) {
        List<Map<String, String>> al = lexiconManager.lemmasList("All languages");
        return getFilteredList(al, _lemma, lemma.getFormWrittenRepr(), false, true);
    }

    public List<String> completeTextOnlyForMW(String _lemma) {
        List<Map<String, String>> al = lexiconManager.lemmasList("All languages");
        return getFilteredList(al, _lemma, lemma.getFormWrittenRepr(), true, false);
    }

    public List<String> completeText(String _lemma) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        boolean b = (boolean) component.getAttributes().get("corr");
        List<Map<String, String>> al = lexiconManager.corrsList("All languages");
        return b ? getFilteredListForCorr(al, _lemma, lemma.getFormWrittenRepr()) : getFilteredList(al, _lemma, lemma.getFormWrittenRepr(), false, false);
    }

    public List<String> completeTextForBilingual(String _lemma) {
        List<Map<String, String>> al = lexiconManager.bilingualLemmasList("All languages");
        return getFilteredList(al, _lemma, lemma.getFormWrittenRepr(), false, false);
    }

    private List<String> getFilteredList(List<Map<String, String>> list, String keyFilter, String currentLemma, boolean onlyMW, boolean seeAOc) {
        List<String> filteredList = new ArrayList();
        Collections.sort(list, new LexiconComparator("writtenRep"));
        for (Map<String, String> m : list) {
            String wr = m.get("writtenRep");
            if ((wr.startsWith(keyFilter)) && (!wr.isEmpty())) {
                if (!wr.equals(currentLemma)) {
                    if (onlyMW) {
                        if (wr.contains(" ")) {
                            filteredList.add(wr + "@" + m.get("lang"));
                        }
                    } else {
                        if (seeAOc) {
                            if (m.get("lang").equals("aoc")) {
                                filteredList.add(wr + "@aoc");
                            }
                        } else {
                            filteredList.add(wr + "@" + m.get("lang"));
                        }
                    }
                }
            }
        }
        return filteredList;
    }

    private List<String> getFilteredListForCorr(List<Map<String, String>> list, String keyFilter, String currentLemma) {
        List<String> filteredList = new ArrayList();
        Collections.sort(list, new LexiconComparator("writtenRep"));
        for (Map<String, String> m : list) {
            String wr = m.get("writtenRep");
            if (m.get("corr").equals("true")) {
                if ((wr.startsWith(keyFilter)) && (!wr.isEmpty())) {
                    if (!wr.equals(currentLemma)) {
                        filteredList.add(wr + "@" + m.get("lang"));
                    }
                }
            }
        }
        return filteredList;
    }

    public List<String> completeComponents(String currentComponent) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        Word w = (Word) component.getAttributes().get("currentComponent");
        List<Map<String, String>> al = lexiconManager.lemmasList("All languages");
        return getFilteredComponentList(al, w.getWrittenRep());

    }
// 

    private List<String> getFilteredComponentList(List<Map<String, String>> list, String writtenRep) {
        List<String> filteredList = new ArrayList();
        String[] splitted = getNormalizedHomonym(writtenRep);
//        Collections.sort(list, new LexiconComparator("writtenRep"));
        for (Map<String, String> m : list) {
            String wr = m.get("writtenRep");
            if ((wr.matches(splitted[0] + MULTIWORD_COMPONENT_REGEXP)) && (!wr.isEmpty())) {
                filteredList.add(wr + "@" + m.get("lang"));
            }
        }
        return filteredList;
    }

    private String[] getNormalizedHomonym(String wr) {
        String[] splitted = new String[10];
        if (wr.matches(".*\\d.*")) {
            // contains a number
            splitted = wr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        } else {
            // does not contain a number
            splitted[0] = wr;
        }
        return splitted;
    }

    public void onSublemmaSelect(Word sublemma) {
        log(Level.INFO, loginController.getAccount(), "ADD Sublemma " + sublemma.getWrittenRep() + " to the Lemma " + lemma.getFormWrittenRepr());
        lemma.setSaveButtonDisabled(false);
        setLexicalRelationEntry(sublemma);
        sublemma.setDeleteButtonDisabled(false);
        sublemma.setViewButtonDisabled(false);
    }

    public void onCollocationSelect(Word collocation) {
        log(Level.INFO, loginController.getAccount(), "ADD Collocation " + collocation.getWrittenRep() + " to the Lemma " + lemma.getFormWrittenRepr());
        lemma.setSaveButtonDisabled(false);
        setLexicalRelationEntry(collocation);
        collocation.setDeleteButtonDisabled(false);
        collocation.setViewButtonDisabled(false);
    }

    public void onReferenceSelect(Word reference) {
        log(Level.INFO, loginController.getAccount(), "ADD Reference (seeAlso) " + reference.getWrittenRep() + " to the Lemma " + lemma.getFormWrittenRepr());
        lemma.setSaveButtonDisabled(false);
        setLexicalRelationEntry(reference);
        reference.setDeleteButtonDisabled(false);
        reference.setViewButtonDisabled(false);
    }

    public void onBilingualSelect(Word bilingual) {
        log(Level.INFO, loginController.getAccount(), "ADD Bilingual variant " + bilingual.getWrittenRep() + " to the Lemma " + lemma.getFormWrittenRepr());
        lemma.setSaveButtonDisabled(false);
        setLexicalRelationEntry(bilingual);
        bilingual.setDeleteButtonDisabled(false);
        bilingual.setViewButtonDisabled(false);
    }

    public void onLinguisticVariantSelect(Word lv) {
        log(Level.INFO, loginController.getAccount(), "ADD linguistic variant " + lv.getWrittenRep() + " to the Lemma " + lemma.getFormWrittenRepr());
        lemma.setSaveButtonDisabled(false);
        setLexicalRelationEntry(lv);
        lv.setDeleteButtonDisabled(false);
        lv.setViewButtonDisabled(false);
    }

    // it queries the lexicon in order to get the name of the individual
    private void setLexicalRelationEntry(Word w) {
        String splitted[] = w.getWrittenRep().split("@");
        String lemma = splitted[0];
        String lang = splitted[1];
        Word wd = lexiconManager.getLemma(lemma, lang);
        w.setWrittenRep(wd.getWrittenRep());
        w.setOWLName(wd.getOWLName());
        w.setLanguage(wd.getLanguage());
    }

    public void onWordSelect(Word word) {
        log(Level.INFO, loginController.getAccount(), "UPDATE the word " + word.getWrittenRep() + " of the Multiword " + lemma.getFormWrittenRepr());
        setMultiwordComponentButtons(word);
        lemma.setSaveButtonDisabled(isSavableLemmaMultiword());
        setWordOfMultiword(word);
        word.setDeleteButtonDisabled(false);
        word.setViewButtonDisabled(false);
    }

    // it queries the lexicon in order to get the name of the individual
    private void setWordOfMultiword(Word w) {
        String splitted[] = w.getLabel().split("@");
        String lemma = splitted[0];
        String lang = splitted[1];
        Word wd = lexiconManager.getLemma(lemma, lang);
        w.setWrittenRep(wd.getWrittenRep());
        w.setOWLName(wd.getOWLName().replace("_lemma", "_entry"));
        w.setLanguage(wd.getLanguage());
        w.setOWLComp(wd.getOWLComp());
        w.setLabel(wd.getWrittenRep() + "@" + wd.getLanguage());
    }

    // invoked by the lemma box in order to get the details of a component of a multiword
    public void addEntryOfMultiwordComponent(Word lemma, String relType) {
        log(Level.INFO, loginController.getAccount(), "VIEW Deatils of multiword component " + lemma.getOWLName() + " of " + lemma.getWrittenRep());
        setMultiwordComponentButtons(lemma);
//        String relationValue = sdo.getName();
//        senseOpenedInRelation = sd.getName();
        lemma.setViewButtonDisabled(true);
        lexiconCreationControllerRelationDetail.resetRelationDetails();
        lexiconCreationControllerRelationDetail.setAddButtonsDisabled(false);
        lexiconCreationControllerRelationDetail.setEntryOfLexicalRelation(lemma.getOWLName().replace("_entry", "_lemma"));
        lexiconCreationControllerRelationDetail.setColumnHeader(": " + relType);
        checkForLock(lemma.getOWLName());
        lexiconManager.getLexiconLocker().print();
        lexiconCreationControllerRelationDetail.setRelationLemmaRendered(true);
    }

    private void setMultiwordComponentButtons(Word comp) {
        for (Word w : lemma.getMultiword()) {
            if (comp.getWrittenRep().equals(w.getWrittenRep()) && comp.getLanguage().equals(w.getLanguage()) || w.getWrittenRep().isEmpty()) {
                w.setViewButtonDisabled(true);
            } else {
                w.setViewButtonDisabled(false);
            }
        }
    }

    // invoked by the lemma box in order to get the details of the lexical relation
    public void addEntryOfLexicalRelation(Word w, String relType) {
        log(Level.INFO, loginController.getAccount(), "VIEW Deatils of " + w.getWrittenRep() + " by " + relType + " relation of Lemma " + lemma.getFormWrittenRepr());
//        setLexicalRelationButtons(false);
//        w.setDeleteButtonDisabled(false);
//        w.setViewButtonDisabled(true);
        lexiconCreationControllerRelationDetail.resetRelationDetails();
        lexiconCreationControllerRelationDetail.setAddButtonsDisabled(false);
        lexiconCreationControllerRelationDetail.setEntryOfLexicalRelation(w.getOWLName());
        lexiconCreationControllerRelationDetail.setColumnHeader(": " + relType);
        checkForLock(w.getOWLName());
        lexiconManager.getLexiconLocker().print();
        lexiconCreationControllerRelationDetail.setRelationLemmaRendered(true);
        lexiconCreationControllerRelationDetail.setCurrentLexicalEntry(w.getOWLName());
        lexiconCreationViewSenseDetail.setSenseRelationButtons(false);
        lexiconCreationControllerRelationDetail.setActiveTab(2);
    }

    public void setLexicalRelationButtons(boolean b) {
        for (Word w : lemma.getSublemma()) {
            w.setViewButtonDisabled(b);
            w.setDeleteButtonDisabled(b);
        }
        for (Word w : lemma.getCollocation()) {
            w.setViewButtonDisabled(b);
            w.setDeleteButtonDisabled(b);
        }
        for (Word w : lemma.getSeeAlso()) {
            w.setViewButtonDisabled(b);
            w.setDeleteButtonDisabled(b);
        }
    }

    // invoked by controller after an user selected add a reference (seeAlso) to lemma
    public void addReference() {
        log(Level.INFO, loginController.getAccount(), "ADD empty reference (seeAlso) to lemma " + lemma.getFormWrittenRepr());
        Word reference = new Word();
        reference.setViewButtonDisabled(true);
        lemma.getSeeAlso().add(reference);
    }

    // invoked by controller after an user selected add a Internal Documentation to lemma
    public void addDocIn() {
        log(Level.INFO, loginController.getAccount(), "ADD empty Internal Documentation to lemma " + lemma.getFormWrittenRepr());
        internalAttestationCreationDialogController.addInternalAttestation(lemma.getFormWrittenRepr());
    }

    // invoked by controller after an user selected add a Internal Documentation to form
    public void addFormDocIn(FormData fd) {
        log(Level.INFO, loginController.getAccount(), "ADD empty Internal Documentation to form " + fd.getFormWrittenRepr());
        internalAttestationCreationDialogController.addInternalAttestation(fd);
        internalAttestationCreationDialogController.setFormPosition(forms.indexOf(fd));
    }

    // invoked by controller after an user selected add a collocation to lemma
    public void addCollocation() {
        log(Level.INFO, loginController.getAccount(), "ADD empty Collocation to lemma " + lemma.getFormWrittenRepr());
        Word collocation = new Word();
        collocation.setViewButtonDisabled(true);
        lemma.getCollocation().add(collocation);
    }

    // invoked by controller after an user selected add a sublemma to lemma
    public void addSublemma() {
        log(Level.INFO, loginController.getAccount(), "ADD empty Sublemma to lemma " + lemma.getFormWrittenRepr());
        Word sublemma = new Word();
        sublemma.setViewButtonDisabled(true);
        lemma.getSublemma().add(sublemma);
    }

    // invoked by controller after an user selected add a bilingual variant to lemma
    public void addBilingual() {
        log(Level.INFO, loginController.getAccount(), "ADD empty bilingual variant to lemma " + lemma.getFormWrittenRepr());
        Word bilingual = new Word();
        bilingual.setViewButtonDisabled(true);
        lemma.getBilingual().add(bilingual);
    }

    // invoked by controller after an user selected add a External Documentation to lemma
    public void addBibliography() {
        log(Level.INFO, loginController.getAccount(), "ADD empty External Documentation to lemma " + lemma.getFormWrittenRepr());
        externalAttestationCreationDialogController.addExternalAttestation(lemma.getFormWrittenRepr());
    }

    public void removeSublemma(Word sublemma) {
        log(Level.INFO, loginController.getAccount(), "REMOVE sublemma " + (sublemma.getWrittenRep().isEmpty() ? " empty sublemma" : sublemma.getWrittenRep()) + " from " + lemma.getFormWrittenRepr());
        lemma.getSublemma().remove(sublemma);
        lemma.setSaveButtonDisabled(false);
        relationPanelCheck(sublemma.getOWLName());
    }

    public void removeCollocation(Word collocation) {
        log(Level.INFO, loginController.getAccount(), "REMOVE collocation " + (collocation.getWrittenRep().isEmpty() ? " empty collocation" : collocation.getWrittenRep()) + " from " + lemma.getFormWrittenRepr());
        lemma.getCollocation().remove(collocation);
        lemma.setSaveButtonDisabled(false);
        relationPanelCheck(collocation.getOWLName());
    }

    public void removeReference(Word reference) {
        log(Level.INFO, loginController.getAccount(), "REMOVE reference (seeAlso) " + (reference.getWrittenRep().isEmpty() ? " empty see also" : reference.getWrittenRep()) + " from " + lemma.getFormWrittenRepr());
        lemma.getSeeAlso().remove(reference);
        lemma.setSaveButtonDisabled(false);
        relationPanelCheck(reference.getOWLName());
    }

    public void removeBilingual(Word bilingual) {
        log(Level.INFO, loginController.getAccount(), "REMOVE bilingual " + (bilingual.getWrittenRep().isEmpty() ? " empty bilingual" : bilingual.getWrittenRep()) + " from " + lemma.getFormWrittenRepr());
        lemma.getBilingual().remove(bilingual);
        lemma.setSaveButtonDisabled(false);
        relationPanelCheck(bilingual.getOWLName());
    }

    // TO FINISH !!!
    public void deleteBiblio(ReferenceData biblio) throws IOException, OWLOntologyStorageException {
        log(Level.INFO, loginController.getAccount(), "DELETE biblio " + (biblio.getLocation().isEmpty() ? " empty biblio" : biblio.getLocation()) + " from " + lemma.getFormWrittenRepr());
        lemma.getReference().remove(biblio);
        lemma.setSaveButtonDisabled(false);
        updateAttestationBoxes();
        info("template.message.deleteLemmaBiblio.summary", "template.message.deleteLemmaBiblio.description");
//        relationPanelCheck(bilingual.getOWLName());
    }

    // TO FINISH !!!
    public void saveBiblio(ReferenceData biblio) throws IOException, OWLOntologyStorageException {
        int order = lemma.getReference().indexOf(biblio);
        if (lemmaCopy.getReference().size() > order && lemmaCopy.getReference().size() > 0) {
            // the bibliographical entry needs to be updated
            lexiconManager.saveLemmaBiblio(lemma, lemmaCopy.getReference().get(order), biblio);
            lemmaCopy.getReference().remove(order);
        } else {
            // it is a new action
            lexiconManager.saveLemmaBiblio(lemma, null, biblio);
        }
        lemmaCopy.getReference().add(order, copyReferenceData(lemma.getReference().get(order)));
        biblio.setSaveButtonDisabled(true);
//        log(Level.INFO, loginController.getAccount(), "SAVE biblio " + biblio.getType() + "" + biblio.getGroupOfDoc() + " " + biblio.getAbbrevTitle() + " " + biblio.getLocation() + " of " + lemma.getFormWrittenRepr());
        log(Level.INFO, loginController.getAccount(), "SAVE biblio " + biblio.getType() + " " + biblio.getLocation() + " of " + lemma.getFormWrittenRepr());
        info("template.message.saveLemmaBiblio.summary", "template.message.saveLemmaBiblio.description");
    }

    // invoked by controller after an user selected update External attestation to lemma
    public void updateExternalAttestation(ExternalAttestationData ead) {
        log(Level.INFO, loginController.getAccount(), "UPDATE External attestation of " + lemma.getFormWrittenRepr());
        externalAttestationCreationDialogController.setExternalAttestation(ead);
    }

    // invoked by controller after an user selected delete External attestation to lemma
    public void deleteExternalAttestation(ExternalAttestationData ead) {
        log(Level.INFO, loginController.getAccount(), "DELETE External attestation of " + lemma.getFormWrittenRepr());
        documentationManager.deleteExternalAttestationByID(ead.getId());
        lemma.getExternalAttestation().remove(ead);
        lemmaCopy.getExternalAttestation().remove(ead);
        updateAttestationBoxes();
    }

    // invoked by controller after an user selected update Internal attestation to lemma
    public void updateInternalAttestation(InternalAttestationData iad) {
        log(Level.INFO, loginController.getAccount(), "UPDATE Internal attestation of " + lemma.getFormWrittenRepr());
        internalAttestationCreationDialogController.setInternalAttestation(iad);
        internalAttestationCreationDialogController.setDialogHeader("Internal attestation of " + lemma.getFormWrittenRepr() + "@" + lemma.getLanguage());
    }

    // invoked by controller after an user selected delete Internal attestation to lemma
    public void deleteInternalAttestation(InternalAttestationData iad) {
        log(Level.INFO, loginController.getAccount(), "DELETE Internal attestation of " + lemma.getFormWrittenRepr());
        documentationManager.deleteInternalAttestationByID(iad.getId());
        lemma.getInternalAttestation().remove(iad);
        lemmaCopy.getInternalAttestation().remove(iad);
        updateAttestationBoxes();
        lexiconCreationControllerTabViewList.initAttestationTabView();
    }

    public void updateAttestationBoxes() {
        int numberOfExtAtt = lemma.getExternalAttestation().size();
        int numberOfIntAtt = lemma.getInternalAttestation().size();
        externalAttestaionTabHeader = "External Documentation (" + Integer.toString(numberOfExtAtt) + ")";
        internalAttestaionTabHeader = "Internal Documentation (" + Integer.toString(numberOfIntAtt) + ")";
    }

    // invoked by controller after an user selected delete Internal attestation to form
    public void deleteInternalAttestationForm(InternalAttestationData iad, FormData fd) {
        log(Level.INFO, loginController.getAccount(), "DELETE Internal attestation of form " + fd.getFormWrittenRepr());
        documentationManager.deleteInternalAttestationByID(iad.getId());
        int order = forms.indexOf(fd);
        forms.get(order).getInternalAttestation().remove(iad);
        formsCopy.get(order).getInternalAttestation().remove(iad);
        updateAttestationFormBoxes(fd);
        lexiconCreationControllerTabViewList.initAttestationTabView();
    }

    // invoked by controller after an user selected update Internal attestation to form
    public void updateInternalAttestationForm(InternalAttestationData iad, FormData fd) {
        log(Level.INFO, loginController.getAccount(), "UPDATE Internal attestation of " + fd.getFormWrittenRepr());
        internalAttestationCreationDialogController.setFormPosition(forms.indexOf(fd));
        internalAttestationCreationDialogController.setInternalAttestation(iad);
        internalAttestationCreationDialogController.setDialogHeader("Internal attestation of " + fd.getFormWrittenRepr() + "@" + fd.getLanguage());
    }

    public String updateAttestationFormBoxes(FormData fd) {
        int numberOfIntAtt = fd.getInternalAttestation().size();
        return "Internal Documentation (" + Integer.toString(numberOfIntAtt) + ")";
    }

    public String updateAttestationFormBoxes(int size) {
        return "Internal Documentation (" + size + ")";
    }

    private void relationPanelCheck(String OWLName) {
        if (lexiconCreationControllerRelationDetail.getCurrentLexicalEntry().equals(OWLName)) {
            lexiconCreationControllerRelationDetail.resetRelationDetails();
            lexiconCreationControllerRelationDetail.setCurrentLexicalEntry("");
        }
    }

    private String sanitize(String wr) {
        String instance = wr.trim();
//        instance = instance.replaceAll("#", "_SHARP_");
//        instance = instance.replaceAll("\\*", "_STAR_");
        instance = instance.replaceAll("\\(", "OB_");
        instance = instance.replaceAll("\\)", "_CB");
        instance = instance.replaceAll("\\[", "OSB_");
        instance = instance.replaceAll("\\]", "_CSB");
//        instance = instance.replaceAll("\\{", "OSB_");
//        instance = instance.replaceAll("\\}", "_CSB");
//        instance = instance.replaceAll("\\?", "_QUEST_");
        instance = instance.replaceAll("#", "");
        instance = instance.replaceAll("\\*", "");
//        instance = instance.replaceAll("\\(", "");
//        instance = instance.replaceAll("\\)", "");
//        instance = instance.replaceAll("\\[", "");
//        instance = instance.replaceAll("\\]", "");
        instance = instance.replaceAll("\\{", "");
        instance = instance.replaceAll("\\}", "");
        instance = instance.replaceAll("\\?", "");
        instance = instance.replaceAll("\\.", "");
        instance = instance.replaceAll("\\,", "");
        instance = instance.replaceAll("\\:", "");
        instance = instance.replaceAll("\\;", "");
        instance = instance.replaceAll("\\!", "");
        instance = instance.replaceAll("\\'", "_APOS_");
        instance = instance.replaceAll("\\’", "_APOS_");
//        instance = instance.replaceAll("\\'", "_");
        instance = instance.replaceAll("\\’", "_APOS_");
        instance = instance.replaceAll("\\‘", "_APOS_");
        instance = instance.replaceAll("\\s+", "_");
        instance = instance.replaceAll("/", "_");
        instance = instance.replaceAll(" ", " ");
        instance = instance.replaceAll(" +(\\d)", "$1");
        return instance;
    }

    //------------------------------------------------------------------------------
    public ArrayList<String> getLemmaInfo() {
        return propertyValue.getLemmaInfo();
    }

    public ArrayList<String> getPoS() {
        return propertyValue.getPoS();
    }

    public ArrayList<String> getNumber() {
        return propertyValue.getNumber();
    }

    public ArrayList<String> getGender() {
        return propertyValue.getGender();
    }

    public ArrayList<String> getPerson() {
        return propertyValue.getPerson();
    }

    public ArrayList<String> getMood() {
        return propertyValue.getMood();
    }

    public ArrayList<String> getVoice() {
        return propertyValue.getVoice();
    }

    public ArrayList<String> getAlphabet() {
        return propertyValue.getAlphabet();
    }

    public ArrayList<String> getTransliterationType() {
        return propertyValue.getTransliterationType();
    }

    public ArrayList<String> getMultiwordType() {
        return propertyValue.getMultiwordType();
    }

    public ArrayList<String> getUsedIn() {
        ArrayList<String> al = new ArrayList();
        al.add("aoc");
        al.add("lat");
        al.remove(lemma.getLanguage());
        return al;
    }

    public ArrayList<String> getReferenceType() {
        return propertyValue.getReferenceType();
    }

    public ArrayList<String> getReferenceAbbrTitle(String type) {
        return propertyValue.getReferenceAbbrTitle(type);
    }

    public ArrayList<String> getReferenceDocGroup(String type) {
        return propertyValue.getReferenceDocGroup(type);
    }

    private void checkForLock(String entry) {
        // check if the lexical entry is available and lock it
        boolean locked = lexiconManager.checkForLock(entry);
        if (locked) {
            lexiconCreationControllerRelationDetail.setLocked(true);
            lexiconCreationControllerRelationDetail.setLocker(lexiconManager.getLockingUser(entry) + " is working ... ");
            log(Level.INFO, loginController.getAccount(), "ACCESS TO THE LOCKED lexical entry related to " + entry);
        } else {
            lexiconCreationControllerRelationDetail.setLocked(false);
            lexiconCreationControllerRelationDetail.setLocker("");
            log(Level.INFO, loginController.getAccount(), "LOCK the lexical entry related to " + entry);
        }
    }

    public int internalAttestationSize() {
        int size = 0, s = 0;
        if (lemma.getInternalAttestation() != null) {
            for (InternalAttestationData iad : lemma.getInternalAttestation()) {
                if ((s = getInternalAttestationLabel(iad).length()) > size) {
                    size = s;
                }
            }
        }
        return size;
    }

    public int externalAttestationSize() {
        int size = 0, s = 0;
        if (lemma.getExternalAttestation() != null) {
            for (ExternalAttestationData ead : lemma.getExternalAttestation()) {
                if ((s = getExternalAttestationLabelByType(ead).length()) > size) {
                    size = s;
                }
            }
        }
        return size;
    }

    public String getInternalAttestationLabel(InternalAttestationData iad) {
        return iad.getDocAbbreviation()
                + " " + (iad.getManSigla() != null ? iad.getManSigla() : "")
                + " " + (iad.getChapterNumber() != null ? iad.getChapterNumber() : "")
                + " " + (iad.getParagraphNumber() != null ? iad.getParagraphNumber() : "")
                + " " + (iad.getListEntry() != null ? iad.getListEntry() : "");
    }

    public String getExternalAttestationLabelByType(ExternalAttestationData ead) {
        String ret = "";
        // if the attestation is external, manSigla contains the type of the document it attested in
        switch (ead.getDocType()) {
            case "Dictionary": {
                ret = ead.getAbbreviationId() + " "
                        + ead.getFascicle() + " "
                        + ead.getVolume() + " "
                        + ead.getSubvolume() + " "
                        + ead.getPageNumber() + " "
                        + ead.getEntryNumber() + " "
                        + ead.getColumnNumber() + " "
                        + ead.getForm() + " "
                        + ead.getSvSublemma();
                break;
            }
            case "Map": {
                ret = ead.getAbbreviationId() + " " + ead.getNumberOfMap() + " " + ead.getNumberOfGeographicPoint() + " " + ead.getForm() + " " + ead.getSvSublemma();
                break;
            }
            case "Book": {
                ret = ead.getAbbreviationId() + " " + ead.getVolume() + " " + ead.getBook() + " " + ead.getChapterNumberRoman()
                        + " " + ead.getPart() + " " + ead.getSec() + " " + ead.getEntryNumber() + " " + ead.getFolioNumber()
                        + " " + ead.getPageNumber() + " " + ead.getForm() + " " + ead.getSvSublemma();
                break;
            }
            default: {
                ret = ead.getAbbreviationId();
                break;
            }
        }
        return ret;
    }

}
