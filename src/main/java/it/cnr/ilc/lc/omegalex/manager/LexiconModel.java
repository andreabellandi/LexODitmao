/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.manager;

import it.cnr.ilc.lc.omegalex.controller.BaseController;
import it.cnr.ilc.lc.omegalex.controller.LoginController;
import it.cnr.ilc.lc.omegalex.domain.InternalAttestation;
import it.cnr.ilc.lc.omegalex.manager.LemmaData.Word;
import it.cnr.ilc.lc.omegalex.manager.SenseData.Openable;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.singleton;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

/**
 *
 * @author andreabellandi
 */
public class LexiconModel extends BaseController {

    @Inject
    private OntologyManager ontologyManager;
    @Inject
    private LoginController loginController;

    private final String LEMON_NS = "http://ditmao.ilc.cnr.it:8082/DitmaoOntologies/lemon.rdf";
    private final String LEXINFO_NS = "http://ditmao.ilc.cnr.it:8082/DitmaoOntologies/lexinfo.owl";
    private final String LEXICON_NS = "http://ditmao.ilc.cnr.it/ditmao";
    private final String DITMAO_LEMON_NS = "http://ditmao.ilc.cnr.it/ditmaoLemon.owl";

    private OWLOntologyManager manager;
    private OWLOntology ontology;
    private OWLDataFactory factory;

    private PrefixManager pm;
    private OWLNamedIndividual lexiconEntry;

    private static final String ONTOLOGY_FOLDER = System.getProperty("user.home") + "/.LexO/";
    private static final String DEFAULT_ONTOLOGY = ONTOLOGY_FOLDER + "ditmao.owl";

    public LexiconModel(FileUploadEvent f) {
        manager = OWLManager.createOWLOntologyManager();
        try (InputStream inputStream = f.getFile().getInputstream()) {
            ontology = manager.loadOntologyFromOntologyDocument(inputStream);
            factory = manager.getOWLDataFactory();
            setPrefixes();
        } catch (OWLOntologyCreationException | IOException ex) {
            log(org.apache.log4j.Level.ERROR, loginController.getAccount(), "LOADING lexicon ", ex);
        }
    }

    public LexiconModel() {
        manager = OWLManager.createOWLOntologyManager();
        try (InputStream inputStream = new FileInputStream(DEFAULT_ONTOLOGY)) {
            ontology = manager.loadOntologyFromOntologyDocument(inputStream);
            factory = manager.getOWLDataFactory();
            setPrefixes();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LexiconModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | OWLOntologyCreationException ex) {
            Logger.getLogger(LexiconModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setPrefixes() {
        pm = new DefaultPrefixManager();
        pm.setPrefix("lexicon", LEXICON_NS);
        pm.setPrefix("lemon", LEMON_NS);
        pm.setPrefix("lexinfo", LEXINFO_NS);
        pm.setPrefix("ditmaolemon", DITMAO_LEMON_NS);
    }

    public void addNewLexicon() {
        OWLClass lexiconClass = factory.getOWLClass(pm.getPrefixName2PrefixMap().get("lemon:"), "#Lexicon");
        lexiconEntry = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#myLexicon");
        addIndividualAxiom(lexiconClass, lexiconEntry);
        addLanguageToLexicon(lexiconEntry);
    }

    private void addLanguageToLexicon(OWLNamedIndividual ni) {
        addDataPropertyAxiom("language", ni, "eng", pm.getPrefixName2PrefixMap().get("lemon:"));
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

    // Save a new lemma note
    public void saveLemmaNote(LemmaData ld, String oldNote) {
        OWLNamedIndividual lemma = getIndividual(ld.getIndividual());
        if (oldNote.isEmpty()) {
            // it needs to create the instance of the property note
            addDataPropertyAxiom("note", lemma, ld.getNote(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        } else {
            // an instance of the property already exists and we have to modify its value
            updateDataPropertyAxiom(lemma, "note", oldNote, ld.getNote(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
    }

    // Save a new lemma bibliographical entry (oldBiblio is null when save action is a new one)
    // ASSUMPTION: newBiblio can not be empty
    public void ORIGINAL_saveLemmaBiblio(LemmaData ld, ReferenceData oldBiblio, ReferenceData newBiblio) {
        OWLNamedIndividual lemma = getIndividual(ld.getIndividual());
        String newbiblioRef = (newBiblio.getType() + " " + newBiblio.getGroupOfDoc() + " " + newBiblio.getAbbrevTitle() + " " + newBiblio.getLocation()).replaceAll("\\s+", " ");
        if (oldBiblio == null) {
            // new action
            addDataPropertyAxiom("isDocumentedIn", lemma, newbiblioRef, pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        } else {
            // update action
            String oldbiblioRef = (oldBiblio.getType() + " " + oldBiblio.getGroupOfDoc() + " " + oldBiblio.getAbbrevTitle() + " " + oldBiblio.getLocation()).replaceAll("\\s+", " ");
            if (!oldbiblioRef.equals(newbiblioRef)) {
                updateDataPropertyAxiom(lemma, "note", oldbiblioRef, newbiblioRef, pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
            }
        }
    }

    public void saveLemmaBiblio(LemmaData ld, ReferenceData oldBiblio, ReferenceData newBiblio) {
        OWLNamedIndividual lemma = getIndividual(ld.getIndividual());
        String newbiblioRef = (newBiblio.getType() + "@" + newBiblio.getLocation()).replaceAll("\\s+", " ");
        if (oldBiblio == null) {
            // new action
            addDataPropertyAxiom("alsoDocumentedIn", lemma, newbiblioRef, pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        } else {
            // update action
            String oldbiblioRef = (oldBiblio.getType() + "@" + oldBiblio.getLocation()).replaceAll("\\s+", " ");
            if (!oldbiblioRef.equals(newbiblioRef)) {
                updateDataPropertyAxiom(lemma, "alsoDocumentedIn", oldbiblioRef, newbiblioRef, pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
            }
        }
    }

    // NEW LEMMA ACTION: write all the triples about the new lemma entry
    public void addLemma(LemmaData ld, String lex) {
        String lemmaInstance = getIRI(ld.getFormWrittenRepr(), ld.getLanguage(), "lemma");
        String entryInstance = getIRI(ld.getFormWrittenRepr(), ld.getLanguage(), "entry");
        ld.setIndividual(lemmaInstance);
        OWLNamedIndividual lexicon = getIndividual(lex);
        OWLNamedIndividual le = getEntry(entryInstance, ld.getType());
        OWLNamedIndividual cf = getForm(lemmaInstance);
        addObjectPropertyAxiom("entry", lexicon, le, pm.getPrefixName2PrefixMap().get("lemon:"));
        addObjectPropertyAxiom("canonicalForm", le, cf, pm.getPrefixName2PrefixMap().get("lemon:"));
        addDataPropertyAxiom("writtenRep", cf, ld.getFormWrittenRepr(), pm.getPrefixName2PrefixMap().get("lemon:"));
        if (ld.isBilingualVariant() && (!ld.getType().equals("Word"))) {
            addDataPropertyAxiom("bilingualVariant", cf, "true", pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
        addProperty("ditmaolemon", cf, "lemmaInfo", ld.getInfo());
        addProperty("lexinfo", cf, "partOfSpeech", ld.getPoS());
        addProperty("lexinfo", cf, "gender", ld.getGender());
        addProperty("lexinfo", cf, "mood", ld.getMood());
        addProperty("lexinfo", cf, "person", ld.getPerson());
        addProperty("lexinfo", cf, "voice", ld.getVoice());
        addProperty("lexinfo", cf, "number", ld.getNumber());
        addProperty("ditmaolemon", cf, "hasAlphabet", ld.getAlphabet());
        if (!ld.getUsedIn().isEmpty()) {
            addProperty("ditmaolemon", "lexicon", cf, "usedIn", "ditmao_" + ld.getUsedIn());
        }
        String type = ld.getTransilterationType();
        if (!type.equals(LexiconQuery.NO_TRANSLITERATION_FOUND)) {
            addDataPropertyAxiom(type + "Transliteration", cf, ld.getTransliteration(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
        addCollocationRelation(le, ld.getCollocation());
        addSublemmaRelation(le, ld.getSublemma());
        if (ld.isLinguisticType()) {
            setLinguisticVariantProperty(ld, cf);
        }
        addDataPropertyAxiom("isCorrespondence", cf, ld.getCorrespondence(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        addDataPropertyAxiom("verified", le, "false", pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
    }

    private void addSublemmaRelation(OWLNamedIndividual le, ArrayList<Word> alw) {
        for (Word w : alw) {
            // TO FINISH
            addObjectPropertyAxiom("hasSublemma", le, getIndividual(w.getOWLName()), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
    }

    private void addCollocationRelation(OWLNamedIndividual le, ArrayList<Word> alw) {
        for (Word w : alw) {
            // TO FINISH
            addObjectPropertyAxiom("hasCollocation", le, getIndividual(w.getOWLName()), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
    }

    // write the entry as individual of the related class and returns it 
    private OWLNamedIndividual getEntry(String uri, String clazz) {
        String ns = null;
        if (clazz.contains("Word")) {
            ns = pm.getPrefixName2PrefixMap().get("lemon:");
        } else {
            ns = pm.getPrefixName2PrefixMap().get("ditmaolemon:");
        }
        OWLClass lexicalEntryClass = factory.getOWLClass(ns, "#" + clazz);
        OWLNamedIndividual lexicalEntry = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#" + uri);
        addIndividualAxiom(lexicalEntryClass, lexicalEntry);
        return lexicalEntry;
    }

    // returns the ontological individual of a given uri string
    private OWLNamedIndividual getIndividual(String uri) {
        return factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#" + uri);
    }

    // write all triples about lemma entry with RENAMING
    public void updateLemmaWithRenaming(LemmaData oldLemma, LemmaData newLemma) {
        String oldLemmaInstance = oldLemma.getIndividual();
        String newLemmaInstance = getIRI(newLemma.getFormWrittenRepr(), newLemma.getLanguage(), "lemma");
//        String oldEntryInstance = getIRI(oldLemma.getFormWrittenRepr(), oldLemma.getLanguage(), "entry");
        String oldEntryInstance = oldLemmaInstance.replace("_lemma", "_entry");
        String newEntryInstance = getIRI(newLemma.getFormWrittenRepr(), newLemma.getLanguage(), "entry");
        newLemma.setIndividual(newLemmaInstance);
        updateLemma(oldLemma, newLemma);
        IRIrenaming(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + oldLemmaInstance), IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + newLemmaInstance));
        OWLNamedIndividual le = getIndividual(oldEntryInstance);
        // form individuals renaming
        formRenaming(oldLemma, newLemma, le);
        // sense individuals renaming
        senseRenaming(oldLemma, newLemma, le);
        IRIrenaming(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + oldEntryInstance), IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + newEntryInstance));
    }

    private void formRenaming(LemmaData oldLemma, LemmaData newLemma, OWLNamedIndividual le) {
        OWLObjectProperty otherForm = factory.getOWLObjectProperty(pm.getPrefixName2PrefixMap().get("lemon:"), "#otherForm");
        for (OWLIndividual i : EntitySearcher.getObjectPropertyValues(le, otherForm, ontology).collect(Collectors.toList())) {
            String formInstance = i.toStringID().replace(pm.getPrefixName2PrefixMap().get("lexicon:") + "#", "");
            formInstance = formInstance.replace(sanitize(oldLemma.getFormWrittenRepr()), sanitize(newLemma.getFormWrittenRepr()));
            IRIrenaming(IRI.create(i.toStringID()), IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + formInstance));
        }
    }

    private void senseRenaming(LemmaData oldLemma, LemmaData newLemma, OWLNamedIndividual le) {
        OWLObjectProperty sense = factory.getOWLObjectProperty(pm.getPrefixName2PrefixMap().get("lemon:"), "#sense");
        int senseNumb = 1;
        for (OWLIndividual i : EntitySearcher.getObjectPropertyValues(le, sense, ontology).collect(Collectors.toList())) {
            String senseInstance = getIRI(newLemma.getFormWrittenRepr(), oldLemma.getLanguage(), "sense");
            IRIrenaming(IRI.create(i.toStringID()), IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + senseInstance + senseNumb));
            senseNumb++;
        }
    }

    private void IRIrenaming(IRI oldIRI, IRI newIRI) {
        OWLEntityRenamer ren = new OWLEntityRenamer(manager, singleton(ontology));
        List<OWLOntologyChange> changes = ren.changeIRI(oldIRI, newIRI);
        ontology.getOWLOntologyManager().applyChanges(changes);
    }

//    public void updateLemma(LemmaData oldLemma, LemmaData newLemma) {
//        String _subject = oldLemma.getIndividual();
//        OWLNamedIndividual subject = factory.getOWLNamedIndividual(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + _subject));
//        updateDataPropertyAxiom(subject, "writtenRep", oldLemma.getFormWrittenRepr(), newLemma.getFormWrittenRepr(), pm.getPrefixName2PrefixMap().get("lemon:"));
//        if (!newLemma.getInfo().isEmpty() && !newLemma.getInfo().equals(LexiconQuery.NO_ENTRY_FOUND)) {
//            updateObjectPropertyAxiom(subject, "lemmaInfo", oldLemma.getInfo(), newLemma.getInfo(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
//        }
//        if (!newLemma.getPoS().isEmpty() && !newLemma.getPoS().equals(LexiconQuery.NO_ENTRY_FOUND)) {
//            updateObjectPropertyAxiom(subject, "partOfSpeech", oldLemma.getPoS(), newLemma.getPoS(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
//        }
//        if (!newLemma.getGender().isEmpty() && !newLemma.getGender().equals(LexiconQuery.NO_ENTRY_FOUND)) {
//            updateObjectPropertyAxiom(subject, "gender", oldLemma.getGender(), newLemma.getGender(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
//        }
//        if (!newLemma.getNumber().isEmpty() && !newLemma.getNumber().equals(LexiconQuery.NO_ENTRY_FOUND)) {
//            updateObjectPropertyAxiom(subject, "number", oldLemma.getNumber(), newLemma.getNumber(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
//        }
//        if (!newLemma.getUsedIn().isEmpty() && !newLemma.getUsedIn().equals(LexiconQuery.NO_ENTRY_FOUND)) {
//            updateObjectPropertyAxiom(subject, "usedIn", oldLemma.getUsedIn(), newLemma.getUsedIn(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"), pm.getPrefixName2PrefixMap().get("lexicon:"));
//        }
//        updateLexicalRelation(oldLemma.getIndividual(), oldLemma.getSeeAlso(), newLemma.getSeeAlso(), "seeAlso");
//        if (newLemma.getType().equals("Word")) {
//            updateLexicalRelation(oldLemma.getIndividual(), oldLemma.getSublemma(), newLemma.getSublemma(), "hasSublemma");
//            updateLexicalRelation(oldLemma.getIndividual(), oldLemma.getCollocation(), newLemma.getCollocation(), "hasCollocation");
//        } else {
//            updateBilingualVariant(oldLemma, newLemma);
//        }
//    }
    public void updateLemma(LemmaData oldLemma, LemmaData newLemma) {
        String _subject = oldLemma.getIndividual();
        OWLNamedIndividual subject = factory.getOWLNamedIndividual(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + _subject));
        OWLNamedIndividual entrySubject = factory.getOWLNamedIndividual(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + _subject.replace("_lemma", "_entry")));
        updateDataPropertyAxiom(subject, "writtenRep", oldLemma.getFormWrittenRepr(), newLemma.getFormWrittenRepr(), pm.getPrefixName2PrefixMap().get("lemon:"));
        updateDocumentedIn(subject, oldLemma.getDocumentedIn(), newLemma.getDocumentedIn());
        updateDataPropertyAxiom(entrySubject, "verified", oldLemma.isVerified(), newLemma.isVerified(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        if (newLemma.getInfo().isEmpty()) {
            updateObjectPropertyAxiom(subject, "lemmaInfo", oldLemma.getInfo(), "attestedLemma", pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        } else {
            updateObjectPropertyAxiom(subject, "lemmaInfo", oldLemma.getInfo(), newLemma.getInfo(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
        
        updateObjectPropertyAxiom(subject, "partOfSpeech", oldLemma.getPoS(), newLemma.getPoS(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
        updateObjectPropertyAxiom(subject, "gender", oldLemma.getGender(), newLemma.getGender(), pm.getPrefixName2PrefixMap().get("lexinfo:"));

        updateObjectPropertyAxiom(subject, "person", oldLemma.getPerson(), newLemma.getPerson(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
        updateObjectPropertyAxiom(subject, "mood", oldLemma.getMood(), newLemma.getMood(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
        updateObjectPropertyAxiom(subject, "voice", oldLemma.getVoice(), newLemma.getVoice(), pm.getPrefixName2PrefixMap().get("lexinfo:"));

        updateObjectPropertyAxiom(subject, "hasAlphabet", oldLemma.getAlphabet(), newLemma.getAlphabet(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        updateObjectPropertyAxiom(subject, "number", oldLemma.getNumber(), newLemma.getNumber(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
        updateObjectPropertyAxiom(subject, "usedIn", oldLemma.getUsedIn(), newLemma.getUsedIn(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"), pm.getPrefixName2PrefixMap().get("lexicon:"));
        updateLexicalRelation(oldLemma.getIndividual(), oldLemma.getSeeAlso(), newLemma.getSeeAlso(), "seeAlso");
        updateLemmaTransliteration(subject, oldLemma, newLemma);
        if (newLemma.getType().equals("Word")) {
            updateLexicalRelation(oldLemma.getIndividual(), oldLemma.getSublemma(), newLemma.getSublemma(), "hasSublemma");
            updateLexicalRelation(oldLemma.getIndividual(), oldLemma.getCollocation(), newLemma.getCollocation(), "hasCollocation");
        } else {
            updateBilingualVariant(oldLemma, newLemma);
        }
        updateLinguisticVariant(subject, oldLemma, newLemma);

        // check if it was a correspondence
        if (oldLemma.getCorrespondence().equals("true") && newLemma.getCorrespondence().equals("false")) {
            OWLNamedIndividual s = factory.getOWLNamedIndividual(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + oldLemma.getIndividual()));
            updateDataPropertyAxiom(s, "isCorrespondence", "true", "false", pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
    }

    private void updateDocumentedIn(OWLNamedIndividual sbj, ArrayList<Word> oldDocs, ArrayList<Word> newDocs) {
        for (Word oldDoc : oldDocs) {
            removeDataPropertyAxiom("isDocumentedIn", sbj, oldDoc.getWrittenRep(), DITMAO_LEMON_NS);
        }
        for (Word newDoc : newDocs) {
            if (!newDoc.getWrittenRep().isEmpty()) {
                addDataPropertyAxiom("isDocumentedIn", sbj, newDoc.getWrittenRep(), DITMAO_LEMON_NS);
            }
        }
    }

    private void updateLemmaTransliteration(OWLNamedIndividual sbj, LemmaData oldLemma, LemmaData newLemma) {
        if (newLemma.getTransilterationType().equals(LexiconQuery.NO_TRANSLITERATION_FOUND)) {
            if (!oldLemma.getTransilterationType().equals(LexiconQuery.NO_TRANSLITERATION_FOUND)) {
                removeDataPropertyAxiom(oldLemma.getTransilterationType() + "Transliteration", sbj, oldLemma.getTransliteration(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
            }
        } else {
            if (!oldLemma.getTransilterationType().equals(newLemma.getTransilterationType()) || !oldLemma.getTransliteration().equals(newLemma.getTransliteration())) {
                removeDataPropertyAxiom(oldLemma.getTransilterationType() + "Transliteration", sbj, oldLemma.getTransliteration(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
                addDataPropertyAxiom(newLemma.getTransilterationType() + "Transliteration", sbj, newLemma.getTransliteration(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
            }
        }
    }

    private void updateBilingualVariant(LemmaData oldLemma, LemmaData newLemma) {
        // check for bilingualVariant boolean property
        if (newLemma.isBilingualVariant()) {
            if (!oldLemma.isBilingualVariant()) {
                // write the property
                addDataPropertyAxiom("bilingualVariant", getIndividual(oldLemma.getIndividual()), "true", pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
            }
        } else {
            if (oldLemma.isBilingualVariant()) {
                // delete the property
                removeDataPropertyAxiom("bilingualVariant", getIndividual(oldLemma.getIndividual()), "true", pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
            }
        }
        // check for hasBilingualVariant property
        removeHasBilingualVariant(getIndividual(oldLemma.getIndividual()), oldLemma.getBilingual());
        addHasBilingualVariant(getIndividual(oldLemma.getIndividual()), newLemma.getBilingual());
    }

    private void removeHasBilingualVariant(OWLNamedIndividual src, ArrayList<Word> alw) {
        for (Word w : alw) {
            removeObjectPropertyAxiom("ditmaolemon", src, "hasBilingualVariant", factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#" + w.getOWLName()));
        }
    }

    private void addHasBilingualVariant(OWLNamedIndividual src, ArrayList<Word> alw) {
        for (Word w : alw) {
            addObjectPropertyAxiom("hasBilingualVariant", src, factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#" + w.getOWLName()), DITMAO_LEMON_NS);
        }
    }

    private void updateLexicalRelation(String sbj, ArrayList<Word> oldWords, ArrayList<Word> newWords, String rel) {
        // for each old lexical relation, if it is not in new lexical relation then remove old lexical relation
        for (Word w : oldWords) {
            if ((!contains(newWords, w)) && (!w.getWrittenRep().isEmpty())) {
                removeLexicalRelation("ditmaolemon", getIndividual(sbj.replace("_lemma", "_entry")), rel, w.getOWLName().replace("_lemma", "_entry"));
                if (rel.equals("seeAlso")) {
                    removeObjectPropertyAxiom("ditmaolemon", getIndividual(w.getOWLName().replace("_lemma", "_entry")), rel, getIndividual(sbj.replace("_lemma", "_entry")));
                }
            }
        }
        // for each new lexical relation, if it is not in old lexical relation then add new lexical relation
        for (Word w : newWords) {
            if ((!contains(oldWords, w)) && (!w.getWrittenRep().isEmpty())) {
                addLexicalRelation("ditmaolemon", getIndividual(sbj.replace("_lemma", "_entry")), rel, w.getOWLName().replace("_lemma", "_entry"));
                w.setDeleteButtonDisabled(false);
                w.setViewButtonDisabled(false);
                if (rel.equals("seeAlso")) {
                    addObjectPropertyAxiom(rel, getIndividual(w.getOWLName().replace("_lemma", "_entry")), getIndividual(sbj.replace("_lemma", "_entry")), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
                }
            }
        }
    }

    private boolean contains(ArrayList<Word> alw, Word w) {
        for (Word _w : alw) {
            if (w.getOWLName().equals(_w.getOWLName())) {
                return true;
            }
        }
        return false;
    }

    private void removeLexicalRelation(String ns, OWLNamedIndividual subject, String rel, String object) {
        String _obj = object.replace("_lemma", "_entry");
        OWLNamedIndividual obj = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#" + _obj);
        OWLObjectProperty prop = factory.getOWLObjectProperty(pm.getPrefixName2PrefixMap().get(ns + ":"), "#" + rel);
        OWLObjectPropertyAssertionAxiom propertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(prop, subject, obj);
        ontology.removeAxiom(propertyAssertion);
    }

    public void addLexicalRelation(String ns, OWLNamedIndividual sbj, String rel, String obj) {
        addObjectPropertyAxiom(rel, sbj, getIndividual(obj), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
    }

    // NEW LEMMA MULTIWORD ACTION: write all the triples about the new lemma entry
    public void addMultiwordLemma(LemmaData ld, String lex) {
        addLemma(ld, lex);
        createMultiwordDecomposition(ld);
    }

    private void createMultiwordDecomposition(LemmaData ld) {
        ArrayList<Word> constituentsList = getConstituents(ld.getMultiword());
        String _entry = ld.getIndividual().replace("_lemma", "_entry");
        OWLNamedIndividual entry = factory.getOWLNamedIndividual(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + _entry));
        for (int i = 0; i < constituentsList.size(); i++) {
            String position = Integer.toString(i);
            // write the component individual
            OWLNamedIndividual componentIndividual = getComponent(getIRI(ld.getIndividual().replace("_lemma", "_entry"), "comp", position));
            addObjectPropertyAxiom("constituent", entry, componentIndividual, pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
            // write its position
            addDataPropertyAxiom("hasPosition", componentIndividual, position, pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
            // write its correspondence if the word exists as lexical entry
            String lexicalEntryOfWord = constituentsList.get(i).getOWLName();
            if (!lexicalEntryOfWord.isEmpty()) {
                OWLNamedIndividual le = getIndividual(lexicalEntryOfWord.replace("_lemma", "_entry"));
                addObjectPropertyAxiom("correspondsTo", componentIndividual, le, pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
            }
        }
    }

    // write the comp of the component class and returns it
    private OWLNamedIndividual getComponent(String uri) {
        OWLClass ComponentClass = factory.getOWLClass(pm.getPrefixName2PrefixMap().get("lemon:"), "#Component");
        OWLNamedIndividual c = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#" + uri);
        addIndividualAxiom(ComponentClass, c);
        return c;
    }

    // returns all the words composing the multiword
    // TODO: it should need a tokenizer !!!!!!
    private ArrayList<Word> getConstituents(ArrayList<Word> alw) {
        ArrayList<Word> constituentsList = new ArrayList();
        for (Word w : alw) {
            constituentsList.add(w);
        }
        return constituentsList;
    }

    // UPDATE multiwordLemma with no renaming
    public void updateMultiwordLemma(LemmaData oldLemma, LemmaData newLemma) {
        // it verifies if the multiword type has been changed
        updateMultiwordLemmaClass(oldLemma, newLemma);
        // it verifies if some component has been changed
        updateComponentsOfLemma(oldLemma.getMultiword(), newLemma.getMultiword());
        // update the oppurtune lemma fields
        updateLemma(oldLemma, newLemma);
    }

    private void updateMultiwordLemmaClass(LemmaData oldLemma, LemmaData newLemma) {
        if (!oldLemma.getType().equals(newLemma.getType())) {
            // it needs to modify the type of the entry related to this lemma
            String _entry = oldLemma.getIndividual().replace("_lemma", "_entry");
            OWLNamedIndividual entry = factory.getOWLNamedIndividual(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + _entry));
            OWLClass oldClass = factory.getOWLClass(IRI.create(pm.getPrefixName2PrefixMap().get("ditmaolemon:") + "#" + oldLemma.getType()));
            OWLClass newClass = factory.getOWLClass(IRI.create(pm.getPrefixName2PrefixMap().get("ditmaolemon:") + "#" + newLemma.getType()));
            addIndividualAxiom(newClass, entry);
            removeIndividualAxiom(oldClass, entry);
        }
    }

    // A component can be changed but NOT the written representation of the multiword 
    // examples: from accus@lat to accus@aoc; from "w not found" to accus@lat;
    private void updateComponentsOfLemma(ArrayList<Word> oldComponents, ArrayList<Word> newComponents) {
        for (int i = 0; i < oldComponents.size(); i++) {
            if (!oldComponents.get(i).getLabel().equals(newComponents.get(i).getLabel())) {
                Word oldComp = oldComponents.get(i);
                Word newComp = newComponents.get(i);
                // write the new component (only the modification of the correspondsTo property is required)
                if (!oldComp.getWrittenRep().contains("not found")) {
                    // the related word is associated with a lexical entry, so deletion it is required
                    removeObjectPropertyAxiom(pm.getPrefixName2PrefixMap().get("lemon:"), getIndividual(oldComp.getOWLComp()), "correspondsTo", getIndividual(oldComp.getOWLName()));
                }
                addObjectPropertyAxiom("correspondsTo", getIndividual(oldComp.getOWLComp()), getIndividual(newComp.getOWLName().replace("_lemma", "_entry")), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
            }
        }
    }

    // UPDATE multiwordLemma with renaming
    public void updateMultiwordLemmaWithRenaming(LemmaData oldLemma, LemmaData newLemma) {
        // it verifies if the multiword type has been changed
        updateMultiwordLemmaClass(oldLemma, newLemma);
        // delete the old constituents
        deleteMultiwordDecomposition(oldLemma);
        // update the oppurtune lemma fields
        updateLemmaWithRenaming(oldLemma, newLemma);
        // create the new constituents
        createMultiwordDecomposition(newLemma);
    }

    private void deleteMultiwordDecomposition(LemmaData oldLemma) {
        OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
        for (Word comp : oldLemma.getMultiword()) {
            remover.visit(getIndividual(comp.getOWLComp()));
            manager.applyChanges(remover.getChanges());
        }
    }

    // Save a new form note
    public void saveFormNote(FormData fd, String oldNote) {
        OWLNamedIndividual form = getIndividual(fd.getIndividual());
        if (oldNote.isEmpty()) {
            // it needs to create the instance of the property note
            addDataPropertyAxiom("note", form, fd.getNote(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        } else {
            // an instance of the property already exists and we have to modify its value
            updateDataPropertyAxiom(form, "note", oldNote, fd.getNote(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
    }

    // NEW FORM ACTION: write all triples about form entry
    public void addForm(FormData fd, LemmaData ld) {
        String formInstance = getIRI(ld.getFormWrittenRepr(), ld.getLanguage(), fd.getFormWrittenRepr(), "form");
        String entryInstance = getIRI(ld.getFormWrittenRepr(), ld.getLanguage(), "entry");
        OWLNamedIndividual le = getIndividual(entryInstance);
        OWLNamedIndividual of = getForm(formInstance);
        fd.setIndividual(formInstance);
        addObjectPropertyAxiom("otherForm", le, of, pm.getPrefixName2PrefixMap().get("lemon:"));
        addDataPropertyAxiom("writtenRep", of, fd.getFormWrittenRepr(), pm.getPrefixName2PrefixMap().get("lemon:"));
//        addProperty("lexinfo", of, "partOfSpeech", fd.getPoS());
        if (fd.getGender() != null) {
            addProperty("lexinfo", of, "gender", fd.getGender());
        }
        if (fd.getNumber() != null) {
            addProperty("lexinfo", of, "number", fd.getNumber());
        }

        if (fd.getPerson() != null) {
            addProperty("lexinfo", of, "person", fd.getPerson());
        }
        if (fd.getMood() != null) {
            addProperty("lexinfo", of, "mood", fd.getMood());
        }
        if (fd.getVoice() != null) {
            addProperty("lexinfo", of, "voice", fd.getVoice());
        }

        addProperty("ditmaolemon", of, "hasAlphabet", fd.getAlphabet());
        if (!fd.getUsedIn().isEmpty()) {
            addProperty("ditmaolemon", "lexicon", of, "usedIn", "ditmao_" + fd.getUsedIn());
        }
        String type = fd.getTransilterationType();
        if (!type.equals(LexiconQuery.NO_TRANSLITERATION_FOUND)) {
            addDataPropertyAxiom(type + "Transliteration", of, fd.getTransliteration(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
        setVariantTypes(fd, of);
    }

    // set all the types of a variant
    private void setVariantTypes(FormData fd, OWLNamedIndividual of) {
        if (fd.isGraphicType()) {
            addProperty("ditmaolemon", of, "variant", "graphicVariant");
        }
        if (fd.isGraphophoneticType()) {
            addProperty("ditmaolemon", of, "variant", "graphophoneticVariant");
        }
        if (fd.isAlphabeticalType()) {
            addProperty("ditmaolemon", of, "variant", "alphabeticalVariant");
        }
        if (fd.isMorphologicalType()) {
            addProperty("ditmaolemon", of, "variant", "morphologicalVariant");
        }
        if (fd.isUnspecifiedType()) {
            addProperty("ditmaolemon", of, "variant", "unspecifiedVariant");
        }
        if (fd.isMorphosyntacticType()) {
            addProperty("ditmaolemon", of, "variant", "morphosyntacticVariant");
        }
    }

    private void setLinguisticVariantProperty(LemmaData ld, OWLNamedIndividual cf) {
        OWLNamedIndividual lemma = factory.getOWLNamedIndividual(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + ld.getLinguisticTypeEntry().getOWLName()));
        addObjectPropertyAxiom("seeAOc", cf, lemma, pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
    }

    // write all triples about form entry with RENAMING
    public void addFormWithRenaming(FormData oldForm, FormData newForm, LemmaData ld) {
//        String oldFormInstance = getIRI(ld.getFormWrittenRepr(), ld.getLanguage(), oldForm.getFormWrittenRepr(), "form");
        String oldFormInstance = oldForm.getIndividual();
        String newFormInstance = getIRI(ld.getFormWrittenRepr(), ld.getLanguage(), newForm.getFormWrittenRepr(), "form");
        updateForm(oldForm, newForm, ld);
        IRIrenaming(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + oldFormInstance), IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + newFormInstance));
    }

    public void updateForm(FormData oldForm, FormData newForm, LemmaData ld) {
//        String _subject = getIRI(ld.getFormWrittenRepr(), ld.getLanguage(), oldForm.getFormWrittenRepr(), "form");
        String _subject = oldForm.getIndividual();
        OWLNamedIndividual subject = factory.getOWLNamedIndividual(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + _subject));
        updateDataPropertyAxiom(subject, "writtenRep", oldForm.getFormWrittenRepr(), newForm.getFormWrittenRepr(), pm.getPrefixName2PrefixMap().get("lemon:"));
        updatePropertyValues(subject, oldForm, newForm);
        updateDocumentedIn(subject, oldForm.getDocumentedIn(), newForm.getDocumentedIn());
        updateFormTypes(subject, "variant", oldForm.isGraphicType(), newForm.isGraphicType(), "graphicVariant", "ditmaolemon");
        updateFormTypes(subject, "variant", oldForm.isGraphophoneticType(), newForm.isGraphophoneticType(), "graphophoneticVariant", "ditmaolemon");
        updateFormTypes(subject, "variant", oldForm.isAlphabeticalType(), newForm.isAlphabeticalType(), "alphabeticalVariant", "ditmaolemon");
        updateFormTypes(subject, "variant", oldForm.isMorphologicalType(), newForm.isMorphologicalType(), "morphologicalVariant", "ditmaolemon");
        updateFormTypes(subject, "variant", oldForm.isUnspecifiedType(), newForm.isUnspecifiedType(), "unspecifiedVariant", "ditmaolemon");
        updateFormTypes(subject, "variant", oldForm.isMorphosyntacticType(), newForm.isMorphosyntacticType(), "morphosyntacticVariant", "ditmaolemon");
        updateTransliteration(subject, oldForm, newForm);
    }

    private void updateLinguisticVariant(OWLNamedIndividual sbj, LemmaData oldLemma, LemmaData newLemma) {
        if (oldLemma.isLinguisticType() && !newLemma.isLinguisticType()) {
            updateFormTypes(sbj, "variant", true, false, "linguisticVariant", "ditmaolemon");
            // remove linguisticVariant
            removeObjectPropertyAxiom("ditmaolemon", sbj, "seeAOc", getIndividual(oldLemma.getLinguisticTypeEntry().getOWLName()));
        } else {
            if (!oldLemma.isLinguisticType() && newLemma.isLinguisticType()) {
                updateFormTypes(sbj, "variant", false, true, "linguisticVariant", "ditmaolemon");
                // add linguisticVariant
                setLinguisticVariantProperty(newLemma, sbj);
            }
        }
    }

//    private void updatePropertyValues(OWLNamedIndividual subject, FormData oldForm, FormData newForm) {
//        if (!newForm.getPoS().isEmpty()) {
//            if (!newForm.getPoS().equals(oldForm.getPoS())) {
//                updateObjectPropertyAxiom(subject, "partOfSpeech", oldForm.getPoS(), newForm.getPoS(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
//            }
//        } else {
//            if (!oldForm.getPoS().equals(LexiconQuery.NO_ENTRY_FOUND)) {
//                removeObjectPropertyAxiom("lexinfo", subject, "partOfSpeech", oldForm.getPoS());
//            }
//        }
//        if (!newForm.getGender().isEmpty()) {
//            if (!newForm.getGender().equals(oldForm.getGender())) {
//                updateObjectPropertyAxiom(subject, "gender", oldForm.getGender(), newForm.getGender(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
//            }
//        } else {
//            if (!oldForm.getGender().equals(LexiconQuery.NO_ENTRY_FOUND)) {
//                removeObjectPropertyAxiom("lexinfo", subject, "gender", oldForm.getGender());
//            }
//        }
//        if (!newForm.getNumber().isEmpty()) {
//            if (!newForm.getNumber().equals(oldForm.getNumber())) {
//                updateObjectPropertyAxiom(subject, "number", oldForm.getNumber(), newForm.getNumber(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
//            }
//        } else {
//            if (!oldForm.getNumber().equals(LexiconQuery.NO_ENTRY_FOUND)) {
//                removeObjectPropertyAxiom("lexinfo", subject, "number", oldForm.getNumber());
//            }
//        }
//        if (!newForm.getAlphabet().isEmpty()) {
//            if (!newForm.getAlphabet().equals(oldForm.getAlphabet())) {
//                updateObjectPropertyAxiom(subject, "hasAlphabet", oldForm.getAlphabet(), newForm.getAlphabet(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
//            }
//        } else {
//            if (!oldForm.getAlphabet().equals(LexiconQuery.NO_ENTRY_FOUND)) {
//                removeObjectPropertyAxiom("ditmaolemon", subject, "hasAlphabet", oldForm.getAlphabet());
//            }
//        }
//    }
    private void updatePropertyValues(OWLNamedIndividual subject, FormData oldForm, FormData newForm) {
        if ((oldForm.getPoS() != null) && (newForm.getPoS() != null)) {
            updateObjectPropertyAxiom(subject, "partOfSpeech", oldForm.getPoS(), newForm.getPoS(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
        }
        if ((oldForm.getGender() != null) && (newForm.getGender() != null)) {
            updateObjectPropertyAxiom(subject, "gender", oldForm.getGender(), newForm.getGender(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
        }
        if ((oldForm.getNumber() != null) && (newForm.getNumber() != null)) {
            updateObjectPropertyAxiom(subject, "number", oldForm.getNumber(), newForm.getNumber(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
        }

        if ((oldForm.getPerson()!= null) && (newForm.getPerson()!= null)) {
            updateObjectPropertyAxiom(subject, "person", oldForm.getPerson(), newForm.getPerson(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
        }
        if ((oldForm.getMood()!= null) && (newForm.getMood() != null)) {
            updateObjectPropertyAxiom(subject, "mood", oldForm.getMood(), newForm.getMood(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
        }
        if ((oldForm.getVoice() != null) && (newForm.getVoice() != null)) {
            updateObjectPropertyAxiom(subject, "voice", oldForm.getVoice(), newForm.getVoice(), pm.getPrefixName2PrefixMap().get("lexinfo:"));
        }

        updateObjectPropertyAxiom(subject, "hasAlphabet", oldForm.getAlphabet(), newForm.getAlphabet(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        updateObjectPropertyAxiom(subject, "usedIn", oldForm.getUsedIn(), newForm.getUsedIn(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"), pm.getPrefixName2PrefixMap().get("lexicon:"));
    }

    private void updateTransliteration(OWLNamedIndividual sbj, FormData oldForm, FormData newForm) {
        if (newForm.getTransilterationType().equals(LexiconQuery.NO_TRANSLITERATION_FOUND)) {
            if (!oldForm.getTransilterationType().equals(LexiconQuery.NO_TRANSLITERATION_FOUND)) {
                removeDataPropertyAxiom(oldForm.getTransilterationType() + "Transliteration", sbj, oldForm.getTransliteration(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
            }
        } else {
            if (!oldForm.getTransilterationType().equals(newForm.getTransilterationType()) || !oldForm.getTransliteration().equals(newForm.getTransliteration())) {
                removeDataPropertyAxiom(oldForm.getTransilterationType() + "Transliteration", sbj, oldForm.getTransliteration(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
                addDataPropertyAxiom(newForm.getTransilterationType() + "Transliteration", sbj, newForm.getTransliteration(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
            }
        }
    }

    private void updateFormTypes(OWLNamedIndividual subject, String property, boolean oldValue, boolean newValue, String value, String ns) {
        if (oldValue == true && newValue == false) {
            removeObjectPropertyAxiom(ns, subject, property, value);
        } else {
            if (oldValue == false && newValue == true) {
                addProperty(ns, subject, property, value);
            }
        }
    }

    private void updateDataPropertyAxiom(OWLNamedIndividual subject, String dataProperty, String oldValue, String newValue, String ns) {
        OWLDataProperty p = factory.getOWLDataProperty(IRI.create(ns + "#" + dataProperty));
        OWLDataPropertyAssertionAxiom oldDataPropertyAssertion = factory.getOWLDataPropertyAssertionAxiom(p, subject, oldValue);
        ontology.removeAxiom(oldDataPropertyAssertion);
        OWLDataPropertyAssertionAxiom newDataPropertyAssertion = factory.getOWLDataPropertyAssertionAxiom(p, subject, newValue);
        manager.addAxiom(ontology, newDataPropertyAssertion);
    }

    // only for verified property
    private void updateDataPropertyAxiom(OWLNamedIndividual subject, String dataProperty, boolean oldValue, boolean newValue, String ns) {
        OWLDataProperty p = factory.getOWLDataProperty(IRI.create(ns + "#" + dataProperty));
        OWLDataPropertyAssertionAxiom oldDataPropertyAssertion = factory.getOWLDataPropertyAssertionAxiom(p, subject, String.valueOf(oldValue));
        ontology.removeAxiom(oldDataPropertyAssertion);
        OWLDataPropertyAssertionAxiom newDataPropertyAssertion = factory.getOWLDataPropertyAssertionAxiom(p, subject, String.valueOf(newValue));
        manager.addAxiom(ontology, newDataPropertyAssertion);
    }

    private void updateObjectPropertyAxiom(OWLNamedIndividual subject, String objProperty, String oldObj, String newObj, String ns) {
        oldObj = (oldObj.equals(LexiconQuery.NO_ENTRY_FOUND) || oldObj.isEmpty()) ? LexiconQuery.NO_ENTRY_FOUND : oldObj;
        newObj = (newObj.equals(LexiconQuery.NO_ENTRY_FOUND) || newObj.isEmpty()) ? LexiconQuery.NO_ENTRY_FOUND : newObj;
        if (!oldObj.equals(newObj)) {
            OWLObjectProperty p = factory.getOWLObjectProperty(IRI.create(ns + "#" + objProperty));
            if (oldObj.equals(LexiconQuery.NO_ENTRY_FOUND)) {
                OWLNamedIndividual newObject = factory.getOWLNamedIndividual((newObj.equals("syntagma") ? pm.getPrefixName2PrefixMap().get("ditmaolemon:") : ns), "#" + newObj);
                OWLObjectPropertyAssertionAxiom newObjPropertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(p, subject, newObject);
                manager.addAxiom(ontology, newObjPropertyAssertion);
            } else {
                if (newObj.equals(LexiconQuery.NO_ENTRY_FOUND)) {
                    OWLNamedIndividual oldObject = factory.getOWLNamedIndividual((oldObj.equals("syntagma") ? pm.getPrefixName2PrefixMap().get("ditmaolemon:") : ns), "#" + oldObj);
                    OWLObjectPropertyAssertionAxiom oldObjPropertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(p, subject, oldObject);
                    ontology.removeAxiom(oldObjPropertyAssertion);
                } else {
                    OWLNamedIndividual newObject = factory.getOWLNamedIndividual((newObj.equals("syntagma") ? pm.getPrefixName2PrefixMap().get("ditmaolemon:") : ns), "#" + newObj);
                    OWLObjectPropertyAssertionAxiom newObjPropertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(p, subject, newObject);
                    manager.addAxiom(ontology, newObjPropertyAssertion);
                    OWLNamedIndividual oldObject = factory.getOWLNamedIndividual((oldObj.equals("syntagma") ? pm.getPrefixName2PrefixMap().get("ditmaolemon:") : ns), "#" + oldObj);
                    OWLObjectPropertyAssertionAxiom oldObjPropertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(p, subject, oldObject);
                    ontology.removeAxiom(oldObjPropertyAssertion);
                }
            }
        }
    }

    // only usedIn calls this
    private void updateObjectPropertyAxiom(OWLNamedIndividual subject, String objProperty, String oldObj, String newObj, String propNs, String objNs) {
        oldObj = (oldObj.equals(LexiconQuery.NO_ENTRY_FOUND) || oldObj.isEmpty()) ? LexiconQuery.NO_ENTRY_FOUND : oldObj;
        newObj = (newObj.equals(LexiconQuery.NO_ENTRY_FOUND) || newObj.isEmpty()) ? LexiconQuery.NO_ENTRY_FOUND : newObj;
        if (!oldObj.equals(newObj)) {
            OWLObjectProperty p = factory.getOWLObjectProperty(IRI.create(propNs + "#" + objProperty));
            if (oldObj.equals(LexiconQuery.NO_ENTRY_FOUND)) {
                OWLNamedIndividual newObject = factory.getOWLNamedIndividual(objNs, "#" + "ditmao_" + newObj);
                OWLObjectPropertyAssertionAxiom newObjPropertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(p, subject, newObject);
                manager.addAxiom(ontology, newObjPropertyAssertion);
            } else {
                if (newObj.equals(LexiconQuery.NO_ENTRY_FOUND)) {
                    OWLNamedIndividual oldObject = factory.getOWLNamedIndividual(objNs, "#" + "ditmao_" + oldObj);
                    OWLObjectPropertyAssertionAxiom oldObjPropertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(p, subject, oldObject);
                    ontology.removeAxiom(oldObjPropertyAssertion);
                } else {
                    OWLNamedIndividual newObject = factory.getOWLNamedIndividual(objNs, "#" + "ditmao_" + newObj);
                    OWLObjectPropertyAssertionAxiom newObjPropertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(p, subject, newObject);
                    manager.addAxiom(ontology, newObjPropertyAssertion);
                    OWLNamedIndividual oldObject = factory.getOWLNamedIndividual(objNs, "#" + "ditmao_" + oldObj);
                    OWLObjectPropertyAssertionAxiom oldObjPropertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(p, subject, oldObject);
                    ontology.removeAxiom(oldObjPropertyAssertion);
                }
            }
        }
    }

    // write a specific triple
    private void addProperty(String ns, OWLNamedIndividual subject, String predicate, String object) {
        if (!object.equals(LexiconQuery.NO_ENTRY_FOUND) && !object.isEmpty()) {
            OWLNamedIndividual i;
            if (object.equals("syntagma")) {
                i = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("ditmaolemon:"), "#" + object);
            } else {
                i = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get(ns + ":"), "#" + object);
            }
            addObjectPropertyAxiom(predicate, subject, i, pm.getPrefixName2PrefixMap().get(ns + ":"));
        } else {
            if (object.isEmpty() && predicate.equals("lemmaInfo")) {
                OWLNamedIndividual i = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get(ns + ":"), "#attestedLemma");
                addObjectPropertyAxiom(predicate, subject, i, pm.getPrefixName2PrefixMap().get(ns + ":"));
            }
        }
    }

    private void addProperty(String nsProp, String nsObj, OWLNamedIndividual subject, String predicate, String object) {
        if (!object.equals(LexiconQuery.NO_ENTRY_FOUND) && !object.isEmpty()) {
            OWLNamedIndividual i;
            if (object.equals("syntagma")) {
                i = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("ditmaolemon:"), "#" + object);
            } else {
                i = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get(nsObj + ":"), "#" + object);
            }
            addObjectPropertyAxiom(predicate, subject, i, pm.getPrefixName2PrefixMap().get(nsProp + ":"));
        }
    }

    // remove a specific triple
    private void removeObjectPropertyAxiom(String ns, OWLNamedIndividual subject, String predicate, String object) {
        OWLNamedIndividual obj = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get(ns + ":"), "#" + object);
        OWLObjectProperty prop = factory.getOWLObjectProperty(pm.getPrefixName2PrefixMap().get(ns + ":"), "#" + predicate);
        OWLObjectPropertyAssertionAxiom propertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(prop, subject, obj);
        ontology.removeAxiom(propertyAssertion);
    }

    // remove a specific triple
    private void removeObjectPropertyAxiom(String ns, OWLNamedIndividual subject, String predicate, OWLNamedIndividual object) {
        OWLObjectProperty prop = factory.getOWLObjectProperty(pm.getPrefixName2PrefixMap().get(ns + ":"), "#" + predicate);
        OWLObjectPropertyAssertionAxiom propertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(prop, subject, object);
        ontology.removeAxiom(propertyAssertion);
    }

    // Save a new sense note
    public void saveSenseNote(SenseData sd, String oldNote) {
        OWLNamedIndividual sense = getIndividual(sd.getName());
        if (oldNote.isEmpty()) {
            // it needs to create the instance of the property note
            addDataPropertyAxiom("note", sense, sd.getNote(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        } else {
            // an instance of the property already exists and we have to modify its value
            updateDataPropertyAxiom(sense, "note", oldNote, sd.getNote(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
    }

    // NEW ACTION: write all triples about sense entry
    public void addSense(SenseData sd, LemmaData ld) {
        String entryInstance = ld.getIndividual().replace("_lemma", "_entry");
        String senseInstance = ld.getIndividual().replace("_lemma", "_sense");
        OWLNamedIndividual le = getIndividual(entryInstance);
        OWLObjectProperty sense = factory.getOWLObjectProperty(pm.getPrefixName2PrefixMap().get("lemon:"), "#sense");
        int senseNumber = EntitySearcher.getObjectPropertyValues(le, sense, ontology).collect(Collectors.toList()).size();
        OWLNamedIndividual s = getSense(senseInstance, senseNumber + 1);
        addObjectPropertyAxiom("sense", le, s, pm.getPrefixName2PrefixMap().get("lemon:"));
        sd.setName(s.getIRI().getShortForm());
    }

    public void deleteLemma(LemmaData ld) {
        String entryInstance = ld.getIndividual().replace("_lemma", "_entry");
        OWLNamedIndividual lemma = factory.getOWLNamedIndividual(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + ld.getIndividual()));
        OWLNamedIndividual lexicalEntry = factory.getOWLNamedIndividual(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + entryInstance));
        if (!ld.getType().equals("Word")) {
            deleteMultiwordDecomposition(ld);
        }
        OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
        remover.visit(lemma);
        manager.applyChanges(remover.getChanges());
        remover.visit(lexicalEntry);
        manager.applyChanges(remover.getChanges());

    }

    public void deleteSense(SenseData sd) {
        OWLNamedIndividual i = factory.getOWLNamedIndividual(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + sd.getName()));
        OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
        remover.visit(i);
        manager.applyChanges(remover.getChanges());
    }

    public void deleteForm(FormData fd) {
        OWLNamedIndividual i = factory.getOWLNamedIndividual(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + fd.getIndividual()));
        OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
        remover.visit(i);
        manager.applyChanges(remover.getChanges());
    }

    // NEW ACTION: write and delete all triples about new sense relations
    public void addSenseRelation(SenseData oldSense, SenseData newSense) {
        OWLNamedIndividual sbj = factory.getOWLNamedIndividual(IRI.create(pm.getPrefixName2PrefixMap().get("lexicon:") + "#" + oldSense.getName()));
        updateSenseScientificName(sbj, oldSense.getScientificName(), newSense.getScientificName());
//        updateSenseOntoClass(sbj, oldSense.getOWLClass(), newSense.getOWLClass());
        saveRelations(sbj, oldSense.getSynonym(), newSense.getSynonym(), "equivalent", "lemon");
        saveRelations(sbj, oldSense.getTranslation(), newSense.getTranslation(), "frenchTranslation", "ditmaolemon");
        saveRelations(sbj, oldSense.getEnglishTranslation(), newSense.getEnglishTranslation(), "englishTranslation", "ditmaolemon");
        saveRelations(sbj, oldSense.getCorrespondence(), newSense.getCorrespondence(), "correspondence", "ditmaolemon");

        saveOntologyReference(sbj, oldSense.getOWLClass(), newSense.getOWLClass());
    }

    private void saveOntologyReference(OWLNamedIndividual sbj, Openable oldR, Openable newR) {
        String prefix = "http://ditmao/ontology";
        if (newR.isViewButtonDisabled()) {
            if (oldR.isViewButtonDisabled()) {
                if (!oldR.getName().equals(newR.getName())) {
                    // delete old and write new
                    removeObjectPropertyAxiom("lemon", sbj, "reference", factory.getOWLNamedIndividual(prefix, "#" + oldR.getName()));
                    addObjectPropertyAxiom("reference", sbj, factory.getOWLNamedIndividual(prefix, "#" + newR.getName()), pm.getPrefixName2PrefixMap().get("lemon:"));
                }
            } else {
                // write new
                addObjectPropertyAxiom("reference", sbj, factory.getOWLNamedIndividual(prefix, "#" + newR.getName()), pm.getPrefixName2PrefixMap().get("lemon:"));
            }
        } else {
            if (oldR.isViewButtonDisabled()) {
                // delete old
                removeObjectPropertyAxiom("lemon", sbj, "reference", factory.getOWLNamedIndividual(prefix, "#" + oldR.getName()));
            }
        }
    }

    private void updateSenseOntoClass(OWLNamedIndividual sbj, Openable oldClass, Openable newClass) {
        if (!oldClass.getName().equals(newClass.getName())) {
            // remove oldClass
            removeSenseRelation("lemon", sbj, "reference", oldClass.getName());
            // add the new class
            addSenseRelation("lemon", sbj, "reference", newClass.getName());
        }
    }

    private void updateSenseScientificName(OWLNamedIndividual sbj, ArrayList<Openable> oldR, ArrayList<Openable> newR) {
        for (Openable o : oldR) {
            removeDataPropertyAxiom("scientificName", sbj, o.getName(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
        for (Openable o : newR) {
            addDataPropertyAxiom("scientificName", sbj, o.getName(), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
    }

    private void saveRelations(OWLNamedIndividual sbj, ArrayList<Openable> oldR, ArrayList<Openable> newR, String relType, String ns) {
        // for each old sense relation, if it is not in new sense relation then remove old sense relation
        for (Openable o : oldR) {
            if ((!contains(newR, o)) && (!o.getName().isEmpty())) {
                String name = getNormalizedName(o.getName());
                o.setName(name);
                removeSenseRelation(ns, sbj, relType, name.replaceAll("\\’", "_APOS_").replaceAll("\\'", "_APOS_"));
                if (!relType.equals("correspondence")) {
                    if (relType.equals("equivalent")) {
                        // HACK: it adds the symmetric property instead of using the reasoner !!!
                        removeSenseRelation(ns, getIndividual(name.replaceAll("\\’", "_APOS_").replaceAll("\\'", "_APOS_")), relType, sbj.getIRI().getShortForm());
                    } else {
                        // HACK: it adds the inverse property instead of using the reasoner !!!
                        removeSenseRelation(ns, getIndividual(name.replaceAll("\\’", "_APOS_").replaceAll("\\'", "_APOS_")), relType + "Of", sbj.getIRI().getShortForm());
                    }
                } else {
                    // HACK: it adds the symmetric property instead of using the reasoner !!!
                    removeSenseRelation(ns, getIndividual(name.replaceAll("\\’", "_APOS_").replaceAll("\\'", "_APOS_")), relType, sbj.getIRI().getShortForm());
                }
            }
        }
        // for each new sense relation, if it is not in old sense relation then add new sense relation
        for (Openable o : newR) {
            if ((!contains(oldR, o)) && (!o.getName().isEmpty())) {
                String name = getNormalizedName(o.getName());
                addSenseRelation(ns, sbj, relType, name.replaceAll("\\’", "_APOS_").replaceAll("\\'", "_APOS_"));
                o.setName(name);
                o.setDeleteButtonDisabled(false);
                o.setViewButtonDisabled(false);
                if (!relType.equals("correspondence")) {
                    if (relType.equals("equivalent")) {
                        // HACK: it adds the symmetric property instead of using the reasoner !!!
                        addSenseRelation(ns, getIndividual(name.replaceAll("\\’", "_APOS_").replaceAll("\\'", "_APOS_")), relType, sbj.getIRI().getShortForm());
                    } else {
                        // HACK: it adds the inverse property instead of using the reasoner !!!
                        addSenseRelation(ns, getIndividual(name.replaceAll("\\’", "_APOS_").replaceAll("\\'", "_APOS_")), relType + "Of", sbj.getIRI().getShortForm());
                    }
                } else {
                    // HACK: it adds the symmetric property instead of using the reasoner !!!
                    addSenseRelation(ns, getIndividual(name.replaceAll("\\’", "_APOS_").replaceAll("\\'", "_APOS_")), relType, sbj.getIRI().getShortForm());
                }
            }
        }
    }

    private boolean contains(ArrayList<Openable> alo, Openable o) {
        for (Openable _o : alo) {
            if (o.getName().equals(_o.getName())) {
                return true;
            }
        }
        return false;
    }

    // write a specific triple
    private void addSenseRelation(String ns, OWLNamedIndividual subject, String relType, String object) {
        OWLNamedIndividual obj = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#" + object);
        addObjectPropertyAxiom(relType, subject, obj, pm.getPrefixName2PrefixMap().get(ns + ":"));
    }

    private void removeSenseRelation(String ns, OWLNamedIndividual subject, String relType, String object) {
        OWLNamedIndividual obj = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#" + object);
        OWLObjectProperty prop = factory.getOWLObjectProperty(pm.getPrefixName2PrefixMap().get(ns + ":"), "#" + relType);
        OWLObjectPropertyAssertionAxiom propertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(prop, subject, obj);
        ontology.removeAxiom(propertyAssertion);
    }

    private String getNormalizedName(String t) {
        if (t.contains("@")) {
            return t.split("@")[0];
        } else {
            return t;
        }
    }

//    // returns the individual of the right lexicon
//    private OWLNamedIndividual getLex(String lex) {
//        return factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#" + lex);
//    }
//
//    // returns the individual form of a lemma
//    private OWLNamedIndividual getLexicalEntry(String uri) {
//        return factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#" + uri);
//    }
    // write the form as individual of the related class and returns it 
    private OWLNamedIndividual getForm(String uri) {
        OWLClass lexicalFormClass = factory.getOWLClass(pm.getPrefixName2PrefixMap().get("lemon:"), "#Form");
        OWLNamedIndividual form = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#" + uri);
        addIndividualAxiom(lexicalFormClass, form);
        return form;
    }

    // write the entry as individual of the related class and returns it 
    private OWLNamedIndividual getEntry(String uri) {
        OWLClass lexicalEntryClass = factory.getOWLClass(pm.getPrefixName2PrefixMap().get("lemon:"), "#LexicalEntry");
        OWLNamedIndividual lexicalEntry = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#" + uri);
        addIndividualAxiom(lexicalEntryClass, lexicalEntry);
        return lexicalEntry;
    }

    // write the sense as individual of the related class and returns it 
    private OWLNamedIndividual getSense(String senseName, int n) {
        OWLClass lexicalSenseClass = factory.getOWLClass(pm.getPrefixName2PrefixMap().get("lemon:"), "#LexicalSense");
        OWLNamedIndividual sense = factory.getOWLNamedIndividual(pm.getPrefixName2PrefixMap().get("lexicon:"), "#" + senseName + n);
        addIndividualAxiom(lexicalSenseClass, sense);
        return sense;
    }

    public StreamedContent export() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            manager.saveOntology(ontology, baos);
        } catch (OWLOntologyStorageException ex) {
            Logger.getLogger(LexiconModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
        return new DefaultStreamedContent(in, "application/txt", "lexicon.owl");
    }

    private void addAttestations(DocumentationManager documentationManager) {
        for (InternalAttestation ia : documentationManager.getInternalAttestations()) {
            OWLNamedIndividual entry = factory.getOWLNamedIndividual(ia.getAttestationUri());
            String attestationString = ((ia.getDocument() != null) ? ia.getDocument().getAbbreviation() : "") + " ";
            if (ia.getManuscript() != null) {
                if (ia.getManuscript().getSiglum().contains("P(=")) {
                    attestationString = attestationString + " P ";
                } else {
                    if (ia.getManuscript().getSiglum().contains("V(=")) {
                        attestationString = attestationString + " V ";
                    } else {
                        if (ia.getManuscript().getSiglum().contains("O(=")) {
                            attestationString = attestationString + " O ";
                        } else {
                            attestationString = attestationString + ia.getManuscript().getSiglum() + " ";
                        }
                    }
                }
            }
            if (ia.getChapterNumber() != null) {
                attestationString = attestationString + ia.getChapterNumber() + " ";
            }
            addDataPropertyAxiom("isDocumentedIn", entry, attestationString.trim().replaceAll("\\s\\s+", " "), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
    }

    private void removeAttestations(DocumentationManager documentationManager) {
        for (InternalAttestation ia : documentationManager.getInternalAttestations()) {
            OWLNamedIndividual entry = factory.getOWLNamedIndividual(ia.getAttestationUri());
            String attestationString = ((ia.getDocument() != null) ? ia.getDocument().getAbbreviation() : "") + " ";
            if (ia.getManuscript() != null) {
                if (ia.getManuscript().getSiglum().contains("P(=")) {
                    attestationString = attestationString + " P ";
                } else {
                    if (ia.getManuscript().getSiglum().contains("V(=")) {
                        attestationString = attestationString + " V ";
                    } else {
                        if (ia.getManuscript().getSiglum().contains("O(=")) {
                            attestationString = attestationString + " O ";
                        } else {
                            attestationString = attestationString + ia.getManuscript().getSiglum() + " ";
                        }
                    }
                }
            }
            if (ia.getChapterNumber() != null) {
                attestationString = attestationString + ia.getChapterNumber() + " ";
            }
            removeDataPropertyAxiom("isDocumentedIn", entry, attestationString.trim().replaceAll("\\s\\s+", " "), pm.getPrefixName2PrefixMap().get("ditmaolemon:"));
        }
    }

    public StreamedContent exportWithAttestations(DocumentationManager documentationManager) {
        addAttestations(documentationManager);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            manager.saveOntology(ontology, baos);
        } catch (OWLOntologyStorageException ex) {
            Logger.getLogger(LexiconModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
        removeAttestations(documentationManager);
        return new DefaultStreamedContent(in, "application/txt", "lexiconWithAttestations.owl");
    }

    public synchronized void persist() throws IOException, OWLOntologyStorageException {
        System.out.println("[" + getTimestamp() + "] LexO : persist start");
        File f = new File(DEFAULT_ONTOLOGY);
        File bkp = new File(DEFAULT_ONTOLOGY + "." + getTimestamp());
        if (!f.renameTo(bkp)) {
            throw new IOException("unable to rename " + bkp.getName());
        }
        try (FileOutputStream fos = new FileOutputStream(DEFAULT_ONTOLOGY)) {
            manager.saveOntology(ontology, fos);
            Runtime.getRuntime().exec("gzip " + bkp.getAbsolutePath());
            String[] cmd = {"/bin/sh", "-c", "grep \"rdf:Description\" " + DEFAULT_ONTOLOGY + " -m1 "};
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            if (stdInput.readLine() != null) {
                FacesContext context = FacesContext.getCurrentInstance();
                context.getExternalContext().getFlash().setKeepMessages(true);
                log(org.apache.log4j.Level.ERROR, loginController.getAccount(), "WRITER ERROR !!!");
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ATTENTION !!!", "Please, stop your work and contact us !"));
            }
        }

        System.out.println("[" + getTimestamp() + "] LexO : persist end");
    }

    private String getTimestamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void addIndividualAxiom(OWLClass c, OWLNamedIndividual i) {
        OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(c, i);
        manager.addAxiom(ontology, classAssertion);
    }

    private void removeIndividualAxiom(OWLClass c, OWLNamedIndividual i) {
        OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(c, i);
        ontology.removeAxiom(classAssertion);
    }

    private void addObjectPropertyAxiom(String objProp, OWLNamedIndividual src, OWLNamedIndividual trg, String ns) {
        OWLObjectProperty p = factory.getOWLObjectProperty(ns, "#" + objProp);
        OWLObjectPropertyAssertionAxiom propertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(p, src, trg);
        manager.addAxiom(ontology, propertyAssertion);
    }

    private void addDataPropertyAxiom(String dataProp, OWLNamedIndividual src, String trg, String ns) {
        OWLDataProperty p = factory.getOWLDataProperty(ns, "#" + dataProp);
        OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory.getOWLDataPropertyAssertionAxiom(p, src, trg);
        manager.addAxiom(ontology, dataPropertyAssertion);
    }

    private void removeDataPropertyAxiom(String dataProp, OWLNamedIndividual src, String trg, String ns) {
        OWLDataProperty p = factory.getOWLDataProperty(ns, "#" + dataProp);
        OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory.getOWLDataPropertyAssertionAxiom(p, src, trg);
        ontology.removeAxiom(dataPropertyAssertion);
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

    public String getLEMON_NS() {
        return LEMON_NS;
    }

    public String getLEXINFO_NS() {
        return LEXINFO_NS;
    }

    public String getLEXICON_NS() {
        return LEXICON_NS;
    }

    public String getDITMAO_LEMON_NS() {
        return DITMAO_LEMON_NS;
    }

    public OWLOntologyManager getManager() {
        return manager;
    }

    public OWLOntology getOntology() {
        return ontology;
    }

    public OWLDataFactory getFactory() {
        return factory;
    }

}
