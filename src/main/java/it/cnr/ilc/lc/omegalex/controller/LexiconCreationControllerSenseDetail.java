/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.manager.LemmaData;
import it.cnr.ilc.lc.omegalex.manager.LexiconManager;
import it.cnr.ilc.lc.omegalex.manager.LexiconQuery;
import it.cnr.ilc.lc.omegalex.manager.OntologyManager;
import it.cnr.ilc.lc.omegalex.manager.PropertyValue;
import it.cnr.ilc.lc.omegalex.manager.PropertyValue.Ontology;
import it.cnr.ilc.lc.omegalex.manager.SenseData;
import it.cnr.ilc.lc.omegalex.manager.SenseData.Openable;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
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
public class LexiconCreationControllerSenseDetail extends BaseController implements Serializable {

    @Inject
    private LexiconCreationControllerFormDetail lexiconCreationControllerFormDetail;
    @Inject
    private LexiconCreationControllerTabViewList lexiconCreationControllerTabViewList;
    @Inject
    private LexiconCreationControllerRelationDetail lexiconCreationControllerRelationDetail;
    @Inject
    private LexiconExplorationControllerDictionary lexiconExplorationControllerDictionary;
    @Inject
    private OntologyManager ontologyManager;
    @Inject
    private PropertyValue propertyValue;
    @Inject
    private LexiconManager lexiconManager;
    @Inject
    private LoginController loginController;

    private final List<SenseData> senses = new ArrayList<>();
    private final List<SenseData> sensesCopy = new ArrayList<>();
    private String senseOpenedInRelation;
    private boolean addSenseButtonDisabled = true;
    private boolean senseToolbarRendered = false;
    private boolean undeletableSense = true;

    private boolean verified = false;

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
    
    private boolean senseRendered = false;
    private boolean locked = false;

    public boolean isSenseRendered() {
        return senseRendered;
    }

    public void setSenseRendered(boolean senseRendered) {
        this.senseRendered = senseRendered;
    }

    public List<SenseData> getSenses() {
        return senses;
    }

    public boolean isAddSenseButtonDisabled() {
        return addSenseButtonDisabled;
    }

    public void setAddSenseButtonDisabled(boolean addSenseButtonDisabled) {
        this.addSenseButtonDisabled = addSenseButtonDisabled;
    }

    public boolean isSenseToolbarRendered() {
        return senseToolbarRendered;
    }

    public void setSenseToolbarRendered(boolean senseToolbarRendered) {
        this.senseToolbarRendered = senseToolbarRendered;
    }

    public boolean isUndeletableSense() {
        return undeletableSense;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isNewAction() {
        return lexiconCreationControllerFormDetail.isNewAction();
    }

    public String emptyMessage(String text, String emptyMessage) {
        return emptyMessage(text, text, emptyMessage);
    }

    public String emptyMessage(String test, String text, String emptyMessage) {
        return test == null || test.equals("") ? emptyMessage : text;
    }

    public void openNote(String senseName) {
        log(Level.INFO, loginController.getAccount(), "OPEN note of Sense " + senseName);
    }

    public void saveNote(SenseData sd) throws IOException, OWLOntologyStorageException {
        log(Level.INFO, loginController.getAccount(), "EDIT note of Sense " + sd.getName() + " in " + sd.getNote());
        int order = senses.indexOf(sd);
        lexiconManager.saveSenseNote(sd, sensesCopy.get(order).getNote());
        sensesCopy.get(order).setNote(sd.getNote());
        info("template.message.saveSenseNote.summary", "template.message.saveSenseNote.description");
    }

    public void closeNote(String senseName) {
        log(Level.INFO, loginController.getAccount(), "CLOSE note of Sense " + senseName);
    }

    public String getCommentIcon(SenseData sd) {
        if (sd.getNote().length() > 0) {
            return "fa fa-comment";
        } else {
            return "fa fa-comment-o";
        }
    }

    public List<SenseData> getSensesCopy() {
        return sensesCopy;
    }

    private void addSenseCopy(SenseData sd) {
        sensesCopy.add(copySenseData(sd));
    }

    private void updateSenseCopy(SenseData sd, int order) {
        SenseData updatedSense = copySenseData(sd);
        sensesCopy.remove(order);
        sensesCopy.add(order, updatedSense);
//        SenseData _sd = sensesCopy.get(order);
//        _sd = copySenseData(sd);
    }

    private void addSenseCopy(ArrayList<SenseData> sdList) {
        sensesCopy.clear();
        for (SenseData sd : sdList) {
            sensesCopy.add(copySenseData(sd));
        }
    }

    private SenseData copySenseData(SenseData sd) {
        SenseData _sd = new SenseData();
        ArrayList<Openable> syns = new ArrayList();
        ArrayList<Openable> translations = new ArrayList();
        ArrayList<Openable> englishTranslations = new ArrayList();
        ArrayList<Openable> translationsOf = new ArrayList();
        ArrayList<Openable> englishTranslationsOf = new ArrayList();
        ArrayList<Openable> correspondences = new ArrayList();
        ArrayList<Openable> sNames = new ArrayList();
        Openable _OWLClass = new Openable();
        _OWLClass.setDeleteButtonDisabled(sd.getOWLClass().isDeleteButtonDisabled());
        _OWLClass.setViewButtonDisabled(sd.getOWLClass().isViewButtonDisabled());
        _OWLClass.setName(sd.getOWLClass().getName());
        _sd.setOWLClass(_OWLClass);
        _sd.setName(sd.getName());
        _sd.setNote(sd.getNote());
        for (Openable sn : sd.getScientificName()) {
            Openable _sns = new Openable();
            _sns.setName(sn.getName());
            sNames.add(_sns);
        }
        for (Openable syn : sd.getSynonym()) {
            Openable _syn = new Openable();
            _syn.setName(syn.getName());
            syns.add(_syn);
        }
        for (Openable trans : sd.getTranslation()) {
            Openable _trans = new Openable();
            _trans.setName(trans.getName());
            translations.add(_trans);
        }
        for (Openable trans : sd.getEnglishTranslation()) {
            Openable _trans = new Openable();
            _trans.setName(trans.getName());
            englishTranslations.add(_trans);
        }

        for (Openable trans : sd.getTranslationOf()) {
            Openable _trans = new Openable();
            _trans.setName(trans.getName());
            translationsOf.add(_trans);
        }
        for (Openable trans : sd.getEnglishTranslationOf()) {
            Openable _trans = new Openable();
            _trans.setName(trans.getName());
            englishTranslationsOf.add(_trans);
        }

        for (Openable corr : sd.getCorrespondence()) {
            Openable _corr = new Openable();
            _corr.setName(corr.getName());
            correspondences.add(_corr);
        }
        _sd.setScientificName(sNames);
        _sd.setSynonym(syns);
        _sd.setTranslation(translations);
        _sd.setEnglishTranslation(englishTranslations);

        _sd.setTranslationOf(translationsOf);
        _sd.setEnglishTranslationOf(englishTranslationsOf);

        _sd.setCorrespondence(correspondences);
        return _sd;
    }

    public void senseScientificNameKeyupEvent(AjaxBehaviorEvent e) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        SenseData sd = (SenseData) component.getAttributes().get("sense");
        String sensePart = ((String) e.getComponent().getAttributes().get("value"));
        log(Level.INFO, loginController.getAccount(), "UPDATE Sense scientific name of sense " + sd.getName() + " in " + sensePart);
        sd.setSaveButtonDisabled(false);
    }

    public void addSense() {
        SenseData sd = new SenseData();
        sd.setName("automatically set");
        sd.setSaveButtonDisabled(false);
        senses.add(sd);
    }

    public void addSenseRelation(SenseData sd, String relType) {
        log(Level.INFO, loginController.getAccount(), "ADD empty " + relType + " relation to " + sd.getName());
        SenseData.Openable sdo = new SenseData.Openable();
        sdo.setViewButtonDisabled(true);
        switch (relType) {
            case "synonym":
                sd.getSynonym().add(sdo);
                break;
            case "translation":
                sd.getTranslation().add(sdo);
                break;
            case "englishTranslation":
                sd.getEnglishTranslation().add(sdo);
                break;
//            case "translationOf":
//                sd.getTranslationOf().add(sdo);
//                break;
//            case "englishTranslationOf":
//                sd.getEnglishTranslationOf().add(sdo);
//                break;
            case "correspondence":
                sd.getCorrespondence().add(sdo);
                break;
            default:
        }
    }

    public void addScientificNames(SenseData sd) {
        log(Level.INFO, loginController.getAccount(), "ADD empty scientific name to " + sd.getName());
        SenseData.Openable sdo = new SenseData.Openable();
        sd.getScientificName().add(sdo);
    }

    public void ontologySearch(SenseData sd) {
        log(Level.INFO, loginController.getAccount(), "SEARCH ontology class for " + sd.getScientificName());
        lexiconManager.ontologySearch(sd);
    }

//    private void updateRelationtoDelete(SenseData sd, Openable sdo, String relType) {
//        ArrayList<Triple> _toDelete = new ArrayList();
//        _toDelete = sd.getInvalidRelations();
//        if ((_toDelete != null) && (!_toDelete.isEmpty())) {
//            for (Triple t : _toDelete) {
//                if ((t.getSubject().equals(sd.getName()))
//                        && (t.getProperty().equals(relType))
//                        && (t.getObject().equals(sdo.getName()))) {
//                    sd.getInvalidRelations().remove(t);
//                }
//            }
//        }
//    }
    public void addSense(String entry, String entryType) {
        senses.clear();
        sensesCopy.clear();
        ArrayList<SenseData> al = null;
        switch (entryType) {
            case "Lemma":
                al = lexiconManager.getSensesOfLemma(entry);
                break;
            case "Form":
                al = lexiconManager.getSensesOfForm(entry);
                break;
            case "Sense":
                al = lexiconManager.getSenses(entry);
                break;
        }
        senses.addAll(al);
        addSenseCopy(al);
    }

    public void removeSense(SenseData sd) throws IOException, OWLOntologyStorageException {
        if ((senses.indexOf(sd) == 0) && (senses.indexOf(sd) != -1)) {
            // default sense can't be deleted !
            log(Level.INFO, loginController.getAccount(), "Negated Deletion of Default Sense " + sd.getName());
            warn("template.message.deleteSenseDenied.summary", "template.message.deleteSenseDenied.description", sd.getName());
        } else {
            if ((senses.indexOf(sd) > 0) && (senses.indexOf(sd) != -1)) {
                // it sets all sense relations as invalid (to be deleted)
                log(Level.INFO, loginController.getAccount(), "DELETE Sense " + sd.getName());
                removeAllSenseRelations(sd);
                // remove them from the lexicon by invoking saveSense and remove the sense individual from the lexicon
                lexiconManager.deleteSense(sd);
                info("template.message.deleteSense.summary", "template.message.deleteSense.description", sd.getName());
                String currentLanguage = lexiconCreationControllerTabViewList.getLexiconLanguage();
                if (senseOpenedInRelation != null) {
                    if (sd.getName().equals(senseOpenedInRelation)) {
                        lexiconCreationControllerRelationDetail.resetRelationDetails();
                        lexiconCreationControllerRelationDetail.setRelationLemmaRendered(false);
                    }
                }
                // remove the sense box
                senses.remove(sd);
            }
        }
    }

    private void removeAllSenseRelations(SenseData sd) {
        ArrayList<Openable> al = new ArrayList();
        al.addAll(sd.getSynonym());
        for (Openable op : al) {
            removeSenseRelation(sd, op, "synonym");
        }
        al.addAll(sd.getTranslation());
        for (Openable op : al) {
            removeSenseRelation(sd, op, "translation");
        }
        al.addAll(sd.getCorrespondence());
        for (Openable op : al) {
            removeSenseRelation(sd, op, "correspondence");
        }
    }

    public List<String> completeText(String sense) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        String relType = (String) component.getAttributes().get("Relation");
        String currentSense = (String) component.getAttributes().get("currentSense");
        List<Map<String, String>> al = null;
        switch (relType) {
            case "synonym":
                al = lexiconManager.sensesList("All languages");
                break;
            case "translation":
                al = lexiconManager.sensesList("fr");
                break;
            case "correspondence":
                al = lexiconManager.sensesList("All languages");
                break;
            case "englishTranslation":
                al = lexiconManager.sensesList("en");
                break;
            case "ontoRef":
                return getFilteredClasses(ontologyManager.classesList(), sense);
//                break;
            default:
        }
        return getFilteredList(al, sense, currentSense, relType);
    }

    public void onRelationTargetSelect(SenseData sd, SenseData.Openable sdo) {
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        String relType = (String) component.getAttributes().get("Relation");
        log(Level.INFO, loginController.getAccount(), "ADD Sense " + sdo.getName() + " as " + relType + " of the sense " + sd.getName());
        sd.setSaveButtonDisabled(false);
        sdo.setDeleteButtonDisabled(false);
        sdo.setViewButtonDisabled(false);
        //AGGIUNTO//
//        if (relType.equals("synonym")) {
//            sd.getSynonym().add(sdo);
//        }
//        if (relType.equals("translation")) {
//            sd.getTranslation().add(sdo);
//        }
    }

    private ArrayList<String> vecchia_getFilteredList(ArrayList<String> list, String keyFilter, String currentSense, String relType) {
        ArrayList<String> filteredList = new ArrayList();
        Collections.sort(list);
        for (String l : list) {
            if ((l.startsWith(keyFilter)) && (!l.isEmpty())) {
                if (relType.equals("translation")) {
                    filteredList.add(l + "@fr");
                } else {
                    filteredList.add(l);
                }
            }
        }
        filteredList.remove(currentSense);
        return filteredList;
    }

    private List<String> getFilteredList(List<Map<String, String>> list, String keyFilter, String currentSense, String relType) {
        List<String> filteredList = new ArrayList();
        Collections.sort(list, new LexiconComparator("writtenRep"));
        for (Map<String, String> m : list) {
            String wr = m.get("writtenRep");
            if (!relType.equals("ontoRef")) {
                if ((wr.startsWith(keyFilter)) && (!wr.isEmpty())) {
                    if (relType.equals("translation")) {
                        if (!wr.equals(currentSense)) {
                            filteredList.add(wr + "@fr");
                        }
                    } else {
                        if (relType.equals("englishTranslation")) {
                            if (!wr.equals(currentSense)) {
                                filteredList.add(wr + "@en");
                            }
                        } else {
                            if (!wr.equals(currentSense)) {
                                filteredList.add(wr + "@" + m.get("lang"));
                            }
                        }
                    }
                }
            } else {
                if ((wr.startsWith(keyFilter.toUpperCase())) && (!wr.isEmpty())) {
                    filteredList.add(wr);
                }
            }
        }
        return filteredList;
    }

    private List<String> getFilteredClasses(List<String> list, String keyFilter) {
        List<String> filteredList = new ArrayList();
        Collections.sort(list);
        for (String clazz : list) {
            if (clazz.startsWith(keyFilter)) {
                filteredList.add(clazz);
            }
        }
        return filteredList;
    }

    public void removeSenseRelation(SenseData sd, SenseData.Openable sdo, String relType) {
        switch (relType) {
            case "synonym":
                log(Level.INFO, loginController.getAccount(), "REMOVE " + (sdo.getName().isEmpty() ? " empty synonym" : sdo.getName()) + " (synonym of " + sd.getName() + ")");
                sd.getSynonym().remove(sdo);
                break;
            case "translation":
                log(Level.INFO, loginController.getAccount(), "REMOVE " + (sdo.getName().isEmpty() ? " empty translation" : sdo.getName()) + " (translation of " + sd.getName() + ")");
                sd.getTranslation().remove(sdo);
                break;
            case "englishTranslation":
                log(Level.INFO, loginController.getAccount(), "REMOVE " + (sdo.getName().isEmpty() ? " empty translation" : sdo.getName()) + " (translation of " + sd.getName() + ")");
                sd.getEnglishTranslation().remove(sdo);
                break;
            case "translationOf":
                log(Level.INFO, loginController.getAccount(), "REMOVE " + sdo.getName() + " (translationOf of " + sd.getName() + ")");
                sd.getTranslation().remove(sdo);
                break;
            case "englishTranslationOf":
                log(Level.INFO, loginController.getAccount(), "REMOVE " + sdo.getName() + " (translationOf of " + sd.getName() + ")");
                sd.getEnglishTranslation().remove(sdo);
                break;
            case "correspondence":
                log(Level.INFO, loginController.getAccount(), "REMOVE " + (sdo.getName().isEmpty() ? " empty correspondence" : sdo.getName()) + " (correspondence of " + sd.getName() + ")");
                sd.getCorrespondence().remove(sdo);
                break;
            case "reference":
                log(Level.INFO, loginController.getAccount(), "REMOVE " + (sdo.getName().isEmpty() ? " empty reference" : sdo.getName()) + " (reference of " + sd.getName() + ")");
                sd.getOWLClass().setName("");
                sd.getOWLClass().setViewButtonDisabled(false);
                break;
            default:
        }
        if (!sdo.getName().isEmpty() || (relType.equals("reference"))) {
            sd.setSaveButtonDisabled(false);
        }
        relationPanelCheck(sdo.getName());
    }

    public void onOntologyReferenceSelect(SenseData sd) {
        log(Level.INFO, loginController.getAccount(), "ADD ontological reference " + sd.getOWLClass().getName() + " to the sense " + sd.getName());
        sd.setSaveButtonDisabled(false);
    }

    private void relationPanelCheck(String OWLName) {
        if (lexiconCreationControllerRelationDetail.getCurrentLexicalEntry().equals(OWLName)) {
            lexiconCreationControllerRelationDetail.resetRelationDetails();
            lexiconCreationControllerRelationDetail.setCurrentLexicalEntry("");
        }
    }

    public void removeScientificName(SenseData sd, SenseData.Openable sdo) {
        log(Level.INFO, loginController.getAccount(), "REMOVE " + sdo.getName() + " (scientific name of " + sd.getName() + ")");
        sd.getScientificName().remove(sdo);
        if (!sdo.getName().isEmpty()) {
            sd.setSaveButtonDisabled(false);
        }
    }

//    public void vecchia_removeSenseRelation(SenseData sd, SenseData.Openable sdo, String relType) {
//        ArrayList<SenseData.Openable> _sdo = new ArrayList();
//        ArrayList<Triple> relationToDelete = new ArrayList();
//        switch (relType) {
//            case "synonym":
//                _sdo = sd.getSynonym();
//                sd.setSynonym(new ArrayList<>());
//                for (SenseData.Openable r : _sdo) {
//                    if (!r.getName().equals(sdo.getName())) {
//                        sd.getSynonym().add(r);
//                    } else {
//                        if (hasSynonym(sd.getName())) {
//                            setRelationToDelete(relationToDelete, sd.getName(), "equivalent", r.getName());
//                            sd.setSaveButtonDisabled(false);
//                        } else {
//                            sd.setSaveButtonDisabled(true);
//                        }
//                    }
//                }
//                break;
//            case "translation":
//                _sdo = sd.getTranslation();
//                sd.setTranslation(new ArrayList<>());
//                for (SenseData.Openable r : _sdo) {
//                    if (!r.getName().equals(sdo.getName())) {
//                        sd.getTranslation().add(r);
//                    } else {
//                        if (hasTranslation(sd.getName())) {
//                            setRelationToDelete(relationToDelete, sd.getName(), "translation", r.getName());
//                            sd.setSaveButtonDisabled(false);
//                        } else {
//                            sd.setSaveButtonDisabled(true);
//                        }
//                    }
//                }
//                break;
//            default:
//        }
//        sd.setInvalidRelations(relationToDelete);
//    }
    private boolean hasSynonym(String sense) {
        return !lexiconManager.getSynonym(sense).isEmpty();
    }

    private boolean hasTranslation(String sense) {
        return !lexiconManager.getSynonym(sense).isEmpty();
    }

//    private void setRelationToDelete(ArrayList<Triple> l, String s, String p, String o) {
//        Triple t = new Triple();
//        t.setSubject(s);
//        t.setProperty(p);
//        t.setObject(o);
//        l.add(t);
//    }
    // invoked by new sense button of lemma box
    public void saveSense(LemmaData ld) throws IOException, OWLOntologyStorageException {
        SenseData sd = new SenseData();
        sd.setSaveButtonDisabled(true);
//        sd.setInvalidRelations(new ArrayList());
        lexiconManager.saveSense(sd, ld);
        senses.add(sd);
        addSenseCopy(sd);
        info("template.message.saveSense.summary", "template.message.saveSense.description", sd.getName());
        String currentLanguage = lexiconCreationControllerTabViewList.getLexiconLanguage();
//        setRelationsButtons(sd);
    }

    // invoked by sense box
    public void saveSenseRelation(SenseData sd) throws IOException, OWLOntologyStorageException {
        log(Level.INFO, loginController.getAccount(), "SAVE Sense " + sd.getName());
        removeEmptyRelations(sd);
        int order = senses.indexOf(sd);
        lexiconManager.saveSenseRelation(sensesCopy.get(order), sd);
        updateSenseCopy(sd, order);
        sd.setSaveButtonDisabled(true);
        setSenseToolbarRendered(true);
        info("template.message.saveSenseRelation.summary", "template.message.saveSenseRelation.description", sd.getName());
    }

    private void removeEmptyRelations(SenseData sd) {
        ArrayList<Openable> syns = new ArrayList();
        ArrayList<Openable> trans = new ArrayList();
        ArrayList<Openable> corrs = new ArrayList();
        ArrayList<Openable> sns = new ArrayList();
        for (int i = 0; i < sd.getSynonym().size(); i++) {
            if (!sd.getSynonym().get(i).getName().isEmpty()) {
                Openable syn = new Openable();
                syn.setName(sd.getSynonym().get(i).getName());
                syns.add(syn);
            }
        }
        for (int i = 0; i < sd.getTranslation().size(); i++) {
            if (!sd.getTranslation().get(i).getName().isEmpty()) {
                Openable t = new Openable();
                t.setName(sd.getTranslation().get(i).getName());
                trans.add(t);
            }
        }
        for (int i = 0; i < sd.getCorrespondence().size(); i++) {
            if (!sd.getCorrespondence().get(i).getName().isEmpty()) {
                Openable c = new Openable();
                c.setName(sd.getCorrespondence().get(i).getName());
                corrs.add(c);
            }
        }
        for (int i = 0; i < sd.getScientificName().size(); i++) {
            if (!sd.getScientificName().get(i).getName().isEmpty()) {
                Openable sn = new Openable();
                sn.setName(sd.getScientificName().get(i).getName());
                sns.add(sn);
            }
        }
        sd.setScientificName(sns);
        sd.setSynonym(syns);
        sd.setTranslation(trans);
        sd.setCorrespondence(corrs);
    }

    // invoked by new lemma
    public void saveDefaultSense(LemmaData ld, String wordType) throws IOException, OWLOntologyStorageException {
        setDefaultSense();
        lexiconManager.saveLemmaAndDefaultSense(senses.get(0), ld, wordType);
        String currentLanguage = lexiconCreationControllerTabViewList.getLexiconLanguage();
        addSenseCopy(senses.get(0));
    }

    private void setDefaultSense() {
        senses.clear();
        sensesCopy.clear();
        SenseData defaultSense = new SenseData();
        defaultSense.setSaveButtonDisabled(true);
        defaultSense.setDeleteButtonDisabled(false);
        senses.add(defaultSense);
    }

    private void setRelationsButtons(SenseData sd) {
        if (sd.getSynonym() != null) {
            for (SenseData.Openable syn : sd.getSynonym()) {
                syn.setDeleteButtonDisabled(false);
                syn.setViewButtonDisabled(false);
            }
        }
        if (sd.getTranslation() != null) {
            for (SenseData.Openable tr : sd.getTranslation()) {
                tr.setDeleteButtonDisabled(false);
                tr.setViewButtonDisabled(false);
            }
        }
    }

    public void ontoClassChanged(SenseData sense) {
        log(Level.INFO, loginController.getAccount(), "UPDATE OntoClass to " + sense.getName() + " of " + sense.getName());
        sense.setSaveButtonDisabled(false);
    }

    public void addEntryOfSenseRelation(SenseData.Openable sdo, String relType, SenseData sd) {
        log(Level.INFO, loginController.getAccount(), "VIEW Deatils of the " + sdo.getName() + " " + relType + " of " + sd.getName());
//        setSenseRelationButtons(false);
        String relationValue = getInstance(sdo.getName());
        relationValue = relationValue.replaceAll("\\’", "_APOS_").replaceAll("\\'", "_APOS_");
        senseOpenedInRelation = relationValue;
//        sdo.setDeleteButtonDisabled(false);
//        sdo.setViewButtonDisabled(true);
        lexiconCreationControllerRelationDetail.resetRelationDetails();
        lexiconCreationControllerRelationDetail.setAddButtonsDisabled(false);
        lexiconCreationControllerRelationDetail.setEntryOfSenseRelation(relationValue);
        lexiconCreationControllerRelationDetail.setColumnHeader(": " + relType);
        checkForLock(getIndividual(sdo.getName()));
        lexiconManager.getLexiconLocker().print();
        lexiconCreationControllerRelationDetail.setRelationLemmaRendered(true);
        lexiconCreationControllerRelationDetail.setCurrentLexicalEntry(sdo.getName());
        lexiconCreationControllerFormDetail.setLexicalRelationButtons(false);
        lexiconCreationControllerRelationDetail.setActiveTab(2);
    }

    private String getIndividual(String sense) {
        String s = sense.split("_sense")[0];
        return s + "_lemma";
    }

    private String getInstance(String s) {
        return s.split("@")[0];
    }

    public void resetSenseDetails() {
        senses.clear();
        senseRendered = false;
    }

    public void addOntoReference(SenseData sd) {
        log(Level.INFO, loginController.getAccount(), "ADD empty ontological reference to " + sd.getName());
        sd.getOWLClass().setViewButtonDisabled(true);
    }

    public void setSenseRelationButtons(boolean b) {
        for (SenseData sd : senses) {
            for (SenseData.Openable sdo : sd.getSynonym()) {
                if (sdo.getName().equals(LexiconQuery.NO_ENTRY_FOUND)) {
                    sdo.setDeleteButtonDisabled(!b);
                    sdo.setViewButtonDisabled(!b);
                } else {
                    sdo.setDeleteButtonDisabled(b);
                    sdo.setViewButtonDisabled(b);
                }
            }
            for (SenseData.Openable sdo : sd.getTranslation()) {
                if (sdo.getName().equals(LexiconQuery.NO_ENTRY_FOUND)) {
                    sdo.setDeleteButtonDisabled(!b);
                    sdo.setViewButtonDisabled(!b);
                } else {
                    sdo.setDeleteButtonDisabled(b);
                    sdo.setViewButtonDisabled(b);
                }
            }
            for (SenseData.Openable sdo : sd.getEnglishTranslation()) {
                if (sdo.getName().equals(LexiconQuery.NO_ENTRY_FOUND)) {
                    sdo.setDeleteButtonDisabled(!b);
                    sdo.setViewButtonDisabled(!b);
                } else {
                    sdo.setDeleteButtonDisabled(b);
                    sdo.setViewButtonDisabled(b);
                }
            }
            for (SenseData.Openable sdo : sd.getTranslationOf()) {
                if (sdo.getName().equals(LexiconQuery.NO_ENTRY_FOUND)) {
                    sdo.setDeleteButtonDisabled(!b);
                    sdo.setViewButtonDisabled(!b);
                } else {
                    sdo.setDeleteButtonDisabled(b);
                    sdo.setViewButtonDisabled(b);
                }
            }
            for (SenseData.Openable sdo : sd.getEnglishTranslationOf()) {
                if (sdo.getName().equals(LexiconQuery.NO_ENTRY_FOUND)) {
                    sdo.setDeleteButtonDisabled(!b);
                    sdo.setViewButtonDisabled(!b);
                } else {
                    sdo.setDeleteButtonDisabled(b);
                    sdo.setViewButtonDisabled(b);
                }
            }
            for (SenseData.Openable sdo : sd.getCorrespondence()) {
                if (sdo.getName().equals(LexiconQuery.NO_ENTRY_FOUND)) {
                    sdo.setDeleteButtonDisabled(!b);
                    sdo.setViewButtonDisabled(!b);
                } else {
                    sdo.setDeleteButtonDisabled(b);
                    sdo.setViewButtonDisabled(b);
                }
            }
        }
    }

    public List<SelectItem> getSensesByLanguage(String sense, boolean filterByLanguage) {
        String filter = "";
//        if (filterByLanguage) filter = lexiconCreationControllerFormDetail.getLemma().getLanguage(); //SIM
        if (!filter.isEmpty()) {
            return lexiconManager.getSensesByLanguage(sense, filter);
        } else {
            return lexiconManager.getSensesByLanguage(sense, null);
        }
    }

    public ArrayList<Ontology> getTaxonomy() {
        return propertyValue.getTaxonomy();
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

    public ArrayList<String> getScientificNames() {
        ArrayList<String> al = new ArrayList();
        for (String sn : lexiconManager.getScientificNames()) {
            if (!al.contains(sn)) {
                al.add(sn);
            }
        }
        Collections.sort(al);
        return al;
    }

}
