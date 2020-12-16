/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.domain.Document;
import it.cnr.ilc.lc.omegalex.domain.InternalAttestation;
import it.cnr.ilc.lc.omegalex.domain.Manuscript;
import it.cnr.ilc.lc.omegalex.manager.DocumentData;
import it.cnr.ilc.lc.omegalex.manager.DocumentationManager;
import it.cnr.ilc.lc.omegalex.manager.ExternalAttestationData;
import it.cnr.ilc.lc.omegalex.manager.InternalAttestationData;
import it.cnr.ilc.lc.omegalex.manager.LexiconManager;
import it.cnr.ilc.lc.omegalex.manager.ManuscriptData;
import it.cnr.ilc.lc.omegalex.manager.OntologyManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.log4j.Level;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author andrea
 */
@ViewScoped
@Named
public class LexiconCreationControllerTabViewList extends BaseController implements Serializable {

    @Inject
    private LexiconCreationControllerFormDetail lexiconCreationControllerFormDetail;
    @Inject
    private LexiconCreationControllerSenseDetail lexiconCreationControllerSenseDetail;
    @Inject
    private LexiconCreationControllerRelationDetail lexiconCreationControllerRelationDetail;
    @Inject
    private LexiconCreationControllerDocumentationDetail lexiconCreationControllerDocumentationDetail;
    @Inject
    private LexiconExplorationControllerDictionary lexiconExplorationControllerDictionary;
    @Inject
    private LexiconCreationControllerIntExtAttestationDetail lexiconCreationControllerIntExtAttestationDetail;
    @Inject
    private LexiconManager lexiconManager;
    @Inject
    private DocumentationManager documentationManager;
    @Inject
    private LoginController loginController;
    @Inject
    private OntologyManager ontologyManager;
    @Inject
    private LexiconCreationLemmaFilterController lexiconCreationLemmaFilterController;
    @Inject
    private LexiconCreationDocumentFilterController lexiconCreationDocumentFilterController;
    @Inject
    private LexiconCreationAttestationFilterController lexiconCreationAttestationFilterController;
    @Inject
    private LexiconCreationFormFilterController lexiconCreationFormFilterController;
    @Inject
    private LexiconCreationSenseFilterController lexiconCreationSenseFilterController;
    @Inject
    private LexiconCreationOntologyDetailController lexiconCreationOntologyDetailController;

    private String lemmaField;
    private String formField;
    private String senseField;
    private String docField;
    private String attField;

    private Integer activeTab = 0;
    private int ontoCounter = 0;
    private String ontologyField;

    private final TreeNode lemmaRoot = new DefaultTreeNode("Root", null);
    private final TreeNode formRoot = new DefaultTreeNode("Root", null);
    private final TreeNode senseRoot = new DefaultTreeNode("Root", null);
    private final TreeNode docRoot = new DefaultTreeNode("Root", null);
    private final TreeNode attRoot = new DefaultTreeNode("Root", null);

    private final TreeNode ontoRoot = new DefaultTreeNode("Root", null);

    private TreeNode selection;

    private String lemmaCorrTabTitle = "Lemma";

    private final ArrayList<String> dynamicLexicaMenuItems = new ArrayList<>();
    private String headerLabel = "Multilingual lexicon: aoc";
    private String lexiconLanguage;
    private String corrLanguage;
    private String lemmaStartsWith;
    private boolean enabledFilter = false;

    private List<Map<String, String>> cachedLemmaList = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> copyOfcachedLemmaList = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> cachedFormList = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> cachedSenseList = new ArrayList<Map<String, String>>();

    private List<DocumentData> cachedDocumentList = new ArrayList<DocumentData>();
    private List<ManuscriptData> cachedManuscriptList = new ArrayList<ManuscriptData>();
    private List<ExternalAttestationData> cachedExternalAttestationList = new ArrayList<ExternalAttestationData>();
    private List<InternalAttestationData> cachedInternalAttestationList = new ArrayList<InternalAttestationData>();

    public String getCorrLanguage() {
        return corrLanguage;
    }

    public void setCorrLanguage(String corrLanguage) {
        this.corrLanguage = corrLanguage;
    }

    public String getLemmaCorrTabTitle() {
        return lemmaCorrTabTitle;
    }

    public void setLemmaCorrTabTitle(String lemmaCorrTabTitle) {
        this.lemmaCorrTabTitle = lemmaCorrTabTitle;
    }

    public Integer getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(Integer activeTab) {
        this.activeTab = activeTab;
    }

    public String getLemmaField() {
        return lemmaField;
    }

    public void setLemmaField(String lemmaField) {
        this.lemmaField = lemmaField;
    }

    public String getFormField() {
        return formField;
    }

    public String getOntologyField() {
        return ontologyField;
    }

    public void setOntologyField(String ontologyField) {
        this.ontologyField = ontologyField;
    }

    public void setFormField(String formField) {
        this.formField = formField;
    }

    public String getSenseField() {
        return senseField;
    }

    public void setSenseField(String senseField) {
        this.senseField = senseField;
    }

    public TreeNode getLemmaRoot() {
        return lemmaRoot;
    }

    public String getHeaderLabel() {
        return headerLabel;
    }

    public void setHeaderLabel(String headerLabel) {
        this.headerLabel = headerLabel;
    }

    public String getAttField() {
        return attField;
    }

    public void setAttField(String attField) {
        this.attField = attField;
    }

    public TreeNode getFormRoot() {
        return formRoot;
    }

    public TreeNode getOntoRoot() {
        return ontoRoot;
    }

    public TreeNode getSenseRoot() {
        return senseRoot;
    }

    public TreeNode getSelection() {
        return selection;
    }

    public void setSelection(TreeNode selection) {
        this.selection = selection;
    }

    public TreeNode getAttRoot() {
        return attRoot;
    }

    public String getLemmaStartsWith() {
        return lemmaStartsWith;
    }

    public void setLemmaStartsWith(String lemmaStartsWith) {
        this.lemmaStartsWith = lemmaStartsWith;
    }

    public boolean isEnabledFilter() {
        return enabledFilter;
    }

    public void setEnabledFilter(boolean enabledFilter) {
        this.enabledFilter = enabledFilter;
    }

    public ArrayList<String> getDynamicLexicaMenuItems() {
        return dynamicLexicaMenuItems;
    }

    public String getLexiconLanguage() {
        return lexiconLanguage;
    }

    public void setLexiconLanguage(String lexiconLanguage) {
        this.lexiconLanguage = lexiconLanguage;
    }

    public String getLemmaCounter() {
        return Integer.toString(lemmaRoot.getChildCount());
    }

    public String getFormCounter() {
        return Integer.toString(formRoot.getChildCount());
    }

    public String getSenseCounter() {
        return Integer.toString(senseRoot.getChildCount());
    }

    public String getDocCounter() {
        return Integer.toString(docRoot.getChildCount());
    }

    public String getAttCounter() {
        return Integer.toString(attRoot.getChildCount());
    }

    public String getDocField() {
        return docField;
    }

    public TreeNode getDocRoot() {
        return docRoot;
    }

    public void setDocField(String docField) {
        this.docField = docField;
    }

    @PostConstruct
    public void INIT() {
        lexiconManager.deafult_loadLexicon();
        ontologyManager.deafult_loadOntology();
        initLexicaMenu("aoc");
        setLexiconLanguage("aoc");
        initLemmaTabView("aoc");
        initFormTabView("aoc");
        initDocumentTabView();
        initAttestationTabView();
        initDomainOntologyTabView();
        setEnabledFilter(true);
    }

    public void loadLexicon(String lang) {
        log(Level.INFO, loginController.getAccount(), "LOAD lexicon " + lang);
//        savePendingChange();
        resetPanels();
//        dynamicLexicaMenuItems.remove(lang);
//        dynamicLexicaMenuItems.add(getLexiconLanguage());
        Collections.sort(dynamicLexicaMenuItems);
        this.lexiconLanguage = lang;
        setLexiconLanguage(lang);
        setHeaderLabel("Multilingual lexicon: " + lang);
        initLemmaTabView(lang);
        initFormTabView(lang);
        lemmaField = "";
        formField = "";
        senseField = "";
        setLemmaCorrTabTitle("Lemma");
    }

    public void correspondenceLexicon(String lang) {
        log(Level.INFO, loginController.getAccount(), "LOAD correspondence lexicon ");
        resetPanels();
        this.lexiconLanguage = "corr";
        setLexiconLanguage("corr");
        this.corrLanguage = lang;
        setHeaderLabel("Multilingual lexicon: " + "Correspondence");
        initLemmaTabViewCorrByLang("Corr", lang);
        initFormTabView("Corr");
        lemmaField = "";
        formField = "";
        senseField = "";
        setLemmaCorrTabTitle("Corr");
    }

    public void initDomainOntologyTabView() {
        ontoRoot.getChildren().clear();
        ontoCounter = ontologyManager.getOntologyHierarchy(ontoRoot);
    }

    public void initDomainOntologyTabView(String nameToSelect) {
        ontoRoot.getChildren().clear();
        ontoCounter = ontologyManager.getOntologyHierarchy(ontoRoot, nameToSelect);
    }

    public void initOntoTabView() {
        ontoRoot.getChildren().clear();
        TreeNode thing = new DefaultTreeNode("Thing", ontoRoot);
        thing.setExpanded(true);
        TreeNode node10 = new DefaultTreeNode("arterias", thing);
        node10.setExpanded(true);
        TreeNode node11 = new DefaultTreeNode("aurelas", thing);
        node11.setExpanded(true);
        TreeNode node12 = new DefaultTreeNode("barras", thing);
        node12.setExpanded(true);
        TreeNode node13 = new DefaultTreeNode("bocaDejosDelEstomach", thing);
        node13.setExpanded(true);
        TreeNode node14 = new DefaultTreeNode("cervel", thing);
        node14.setExpanded(true);
//        TreeNode node15 = new DefaultTreeNode("codena", ontoRoot);
//        node15.setExpanded(true);
//        TreeNode node16 = new DefaultTreeNode("colera", ontoRoot);
//        node16.setExpanded(true);
        TreeNode node17 = new DefaultTreeNode("costas", thing);
        node17.setExpanded(true);
        TreeNode node18 = new DefaultTreeNode("craneum", thing);
        node18.setExpanded(true);

        TreeNode node19 = new DefaultTreeNode("epiglot", thing);
        node19.setExpanded(true);
        TreeNode node20 = new DefaultTreeNode("espinaDelDors", thing);
        node20.setExpanded(true);
        TreeNode node21 = new DefaultTreeNode("estomac", thing);
        node21.setExpanded(true);
        TreeNode node22 = new DefaultTreeNode("fege", thing);
        node22.setExpanded(true);
        TreeNode node23 = new DefaultTreeNode("fleuma", thing);
        node23.setExpanded(true);
        TreeNode node24 = new DefaultTreeNode("front", thing);
        node24.setExpanded(true);
//        TreeNode node25 = new DefaultTreeNode("gip", ontoRoot);
//        node25.setExpanded(true);
        TreeNode node26 = new DefaultTreeNode("gola", thing);
        node26.setExpanded(true);

        TreeNode node27 = new DefaultTreeNode("hismon", thing);
        node27.setExpanded(true);
//        TreeNode node28 = new DefaultTreeNode("huelh", ontoRoot);
//        node28.setExpanded(true);
        TreeNode node29 = new DefaultTreeNode("hysofagon", thing);
        node29.setExpanded(true);
        TreeNode node30 = new DefaultTreeNode("malencolia", thing);
        node30.setExpanded(true);
        TreeNode node31 = new DefaultTreeNode("mandibula", thing);
        node31.setExpanded(true);

        TreeNode node1 = new DefaultTreeNode("meringe", thing);
        node1.setExpanded(true);
        node1.getChildren().add(new DefaultTreeNode("duraMater"));
        node1.getChildren().add(new DefaultTreeNode("piadossaMayre"));

        TreeNode node2 = new DefaultTreeNode("nervis", thing);
        node2.setExpanded(true);
        TreeNode _node21 = new DefaultTreeNode("nervisMovedors", node2);
        _node21.setExpanded(true);
        _node21.getChildren().add(new DefaultTreeNode("nervisVocals"));
        _node21.getChildren().add(new DefaultTreeNode("nervisEspina"));
        _node21.getChildren().add(new DefaultTreeNode("nervisBarrasDesus"));
        _node21.getChildren().add(new DefaultTreeNode("nervisBarrasDejos"));
        TreeNode __node21 = new DefaultTreeNode("nervisSensibles", node2);
        __node21.setExpanded(true);
        __node21.getChildren().add(new DefaultTreeNode("nervisObtici"));

        TreeNode node32 = new DefaultTreeNode("san", thing);
        node32.setExpanded(true);
        TreeNode node33 = new DefaultTreeNode("sancDelVentre", thing);
        node33.setExpanded(true);
        TreeNode node34 = new DefaultTreeNode("uva", thing);
        node34.setExpanded(true);
        TreeNode node35 = new DefaultTreeNode("venas", thing);
        node35.setExpanded(true);
        TreeNode node36 = new DefaultTreeNode("ventre", thing);
        node36.setExpanded(true);

    }

    private List<Map<String, String>> correspondenceFilter(List<Map<String, String>> l, boolean corr) {
        ArrayList<Map<String, String>> al = new ArrayList();
        for (Map<String, String> m : l) {
            if (corr) {
                if (m.get("corr").equals("true")) {
                    if (!alreadyExists(al, m.get("writtenRep"))) {
                        al.add(m);
                    }
                }
            } else {
                if (m.get("corr") == null) {
                    if (!alreadyExists(al, m.get("writtenRep"))) {
                        al.add(m);
                    }
                } else {
                    if (m.get("corr").equals("false")) {
                        if (!alreadyExists(al, m.get("writtenRep"))) {
                            al.add(m);
                        }
                    }
                }
            }
        }
        return al;
    }

    private boolean alreadyExists(List<Map<String, String>> l, String wr) {
        for (Map<String, String> m : l) {
            if (m.get("writtenRep").equals(wr)) {
                return true;
            }
        }
        return false;
    }

    private void updateCache(List<Map<String, String>> l, String typeList) {
        Collections.sort(l, new LexiconComparator("writtenRep"));
        switch (typeList) {
            case "Lemma":
                cachedLemmaList.clear();
//                cachedLemmaList.addAll(l);
                cachedLemmaList.addAll(correspondenceFilter(l, false));
                break;
            case "Form":
                cachedFormList.clear();
                cachedFormList.addAll(l);
                break;
            case "Sense":
                cachedSenseList.clear();
                cachedSenseList.addAll(l);
                break;
            case "Corr":
                cachedLemmaList.clear();
                cachedLemmaList.addAll(l);
//                cachedLemmaList.addAll(correspondenceFilter(l, true));
                break;
            default:
        }
    }

    private void updateDocumentationCache() {
        cachedDocumentList.clear();
        for (Document d : documentationManager.getDocuments()) {
            cachedDocumentList.add(new DocumentData(d));
        }
        cachedManuscriptList.clear();
        for (Manuscript m : documentationManager.getManuscripts()) {
            cachedManuscriptList.add(new ManuscriptData(m));
        }
    }

    private void updateAttestationCache() {
        cachedExternalAttestationList.clear();
        for (Object[] row : documentationManager.getExternalAttestations()) {
            cachedExternalAttestationList.add(getExternalAttestation(row));
        }
        cachedInternalAttestationList.clear();
        for (InternalAttestation row : documentationManager.getInternalAttestations()) {
            cachedInternalAttestationList.add(getInternalAttestation(row));
        }
    }

    private ExternalAttestationData getExternalAttestation(Object[] row) {
        // d.abbreviation, ea.id, ea.attestationUri, d.attType, ea.pageNumber, ea.attestationUriLemma
        // ea.articleNumber, ea.book, ea.chapterNumberRoman, ea.columnNumber, ea.entryNumber, ea.fascicle, 
        // ea.folioNumber, ea.footnoteNumber, ea.form, ea.glossaryNumber, ea.lineNumber, ea.numberOfGeographicPoint, 
        // ea.numberOfMap, ea.other, ea.pageNumber, ea.paragraphNumber, ea.part, ea.sec, ea.subvolume, ea.svSublemma, 
        // ea.url, ea.volume
        ExternalAttestationData ead = new ExternalAttestationData();
        if (row != null) {
            ead.setAbbreviationId(row[0].toString());
            ead.setId(((Number) row[1]).longValue());
            ead.setAttestationUri(row[2].toString());
            ead.setDocType(row[3].toString());
            ead.setPageNumber(row[4].toString());
            ead.setAttestationUriLemma(row[5].toString());

//            ead.setArticleNumber(row[6].toString());
//            ead.setBook(row[7].toString());
//            ead.setChapterNumberRoman(row[8].toString());
//            ead.setColumnNumber(row[9].toString());
//            ead.setEntryNumber(row[10].toString());
//            ead.setFascicle(row[11].toString());
//            ead.setFolioNumber(row[12].toString());
//            ead.setFootnoteNumber(row[13].toString());
//            ead.setForm(row[14].toString());
//
//            ead.setGlossaryNumber(row[15].toString());
//            ead.setLineNumber(row[16].toString());
//            ead.setNumberOfGeographicPoint(row[17].toString());
//            ead.setNumberOfMap(row[18].toString());
//            ead.setOther(row[19].toString());
//
//            ead.setParagraphNumber(row[20].toString());
//            ead.setPart(row[21].toString());
//            ead.setSec(row[22].toString());
//            ead.setSubvolume(row[23].toString());
//            ead.setSvSublemma(row[24].toString());
//            ead.setUrl(row[25].toString());
//            ead.setVolume(row[26].toString());
        }
        return ead;
    }

    private InternalAttestationData getInternalAttestation(InternalAttestation row) {
        // d.abbreviation, ea.id, ea.attestationUri, d.attType, ea.pageNumber
        InternalAttestationData iad = new InternalAttestationData();
        iad.setAbbreviationId("");
        iad.setId(row.getId());
        iad.setAttestationUri(row.getAttestationUri());
        iad.setDocType("Internal");
        iad.setChapterNumber(row.getChapterNumber());
        iad.setDocAbbreviation(row.getDocument().getAbbreviation());
        if (row.getManuscript() != null) {
            iad.setManSigla(row.getManuscript().getSiglum());
        } else {
            iad.setManSigla("");
        }
        iad.setAttestationUriLemma(row.getAttestationUriLemma());
        return iad;
    }

    // invoked by CNL query filter and advanced one
    public void cnlqFilterLemmaTabView(List<Map<String, String>> ll) {
        lemmaRoot.getChildren().clear();
        updateCache(ll, "Lemma");
        for (Map<String, String> m : cachedLemmaList) {
            DataTreeNode dtn = new DataTreeNode(m, 0);
            lemmaRoot.getChildren().add(new DefaultTreeNode(dtn));
        }
    }

    // invoked by CNL query filter and advanced one
    public void cnlqFilterFormTabView(List<Map<String, String>> fl) {
        formRoot.getChildren().clear();
        updateCache(fl, "Form");
        for (Map<String, String> m : cachedFormList) {
            DataTreeNode dtn = new DataTreeNode(m, 0);
            formRoot.getChildren().add(new DefaultTreeNode(dtn));
        }
    }

    // invoked by CNL query filter and advanced one
    public void cnlqFilterSenseTabView(List<Map<String, String>> sl) {
        senseRoot.getChildren().clear();
        updateCache(sl, "Sense");
        for (Map<String, String> m : cachedSenseList) {
            DataTreeNode dtn = new DataTreeNode(m, 0);
            senseRoot.getChildren().add(new DefaultTreeNode(dtn));
        }
    }

    public void initLemmaTabView(String lang) {
        lemmaRoot.getChildren().clear();
//        if ("corr".equals(lang)) {
//            updateCache(lexiconManager.lemmasList(lang), "Corr");
//        } else {
        updateCache(lexiconManager.lemmasList(lang), "Lemma");
//        }
        for (Map<String, String> m : cachedLemmaList) {
            if (m.get("writtenRep").contains("aranhons")) {
                int i = 0;
            }
            DataTreeNode dtn = new DataTreeNode(m, 0);
            lemmaRoot.getChildren().add(new DefaultTreeNode(dtn));
        }
    }

    public void initLemmaTabViewCorrByLang(String corr, String lang) {
        lemmaRoot.getChildren().clear();
        updateCache(lexiconManager.corrsList(lang), "Corr");
        for (Map<String, String> m : cachedLemmaList) {
            if (m.get("corr").equals("true")) {
                DataTreeNode dtn = new DataTreeNode(m, 0);
                dtn.info = "";
                lemmaRoot.getChildren().add(new DefaultTreeNode(dtn));
            }
        }
    }

    public void initFormTabView(String lang) {
        formRoot.getChildren().clear();
        if (!lang.equals("Corr")) {
//        List<Map<String, String>> fl = lexiconManager.formsList(lang);
//        Collections.sort(fl, new LexiconComparator("writtenRep"));
            updateCache(lexiconManager.formsList(lang), "Form");
            for (Map<String, String> m : cachedFormList) {
                DataTreeNode dtn = new DataTreeNode(m, 0);
                formRoot.getChildren().add(new DefaultTreeNode(dtn));
            }
        }
    }

    public void initDocumentTabView() {
        docRoot.getChildren().clear();
        updateDocumentationCache();
        for (DocumentData d : cachedDocumentList) {
            DocTreeNode dtn = new DocTreeNode(d.getDocId(), d.getAbbreviation(), d.getType(), d.getTitle(), d.getSourceType(), 0);
            docRoot.getChildren().add(new DefaultTreeNode(dtn));
        }
        for (ManuscriptData md : cachedManuscriptList) {
            DocTreeNode dtn = new DocTreeNode(md.getDocId(), md.getSiglum(), md.getTitle(), 0);
            docRoot.getChildren().add(new DefaultTreeNode(dtn));
        }
    }

    private void addInfo(AttTreeNode atn, ExternalAttestationData ead, String type) {
        switch (type) {
            case "Dictionary": {
                atn.addExternalAttestationDictionary(ead.getSvSublemma(), ead.getFascicle(), ead.getVolume(), ead.getSubvolume(), ead.getPageNumber(), ead.getEntryNumber(), ead.getColumnNumber());
                break;
            }
            case "Map": {
                atn.addExternalAttestationMap(ead.getNumberOfMap(), ead.getNumberOfGeographicPoint());
                break;
            }
            case "Book": {
                atn.addExternalAttestationBook(ead.getBook(), ead.getChapterNumberRoman(), ead.getPart(), ead.getVolume(), ead.getSec(), ead.getEntryNumber(), ead.getFolioNumber(), ead.getPageNumber());
                break;
            }
        }
    }

    public void initAttestationTabView() {
        ArrayList<AttTreeNode> atnList = new ArrayList<AttTreeNode>();
        attRoot.getChildren().clear();
        updateAttestationCache();
        for (ExternalAttestationData ead : cachedExternalAttestationList) {
            AttTreeNode atn = new AttTreeNode(ead.getId(), ead.getAbbreviationId(), "External", ead.getAttestationUri(), ead.getAttestationUriLemma(), ead.getDocType(), ead.getPageNumber(), "", "", 0);
//            attRoot.getChildren().add(new DefaultTreeNode(atn));
//            addInfo(atn, ead, ead.getDocType());
            atnList.add(atn);
        }
        for (InternalAttestationData iad : cachedInternalAttestationList) {
            AttTreeNode atn = new AttTreeNode(iad.getId(), iad.getDocAbbreviation(), "Internal", iad.getAttestationUri(), iad.getAttestationUriLemma(), iad.getManSigla(), iad.getChapterNumber(), iad.getParagraphNumber(), iad.getListEntry(), 0);
//            attRoot.getChildren().add(new DefaultTreeNode(atn));
            atnList.add(atn);
        }
        Collections.sort(atnList, new AttestationComparator());
        for (AttTreeNode atn : atnList) {
            attRoot.getChildren().add(new DefaultTreeNode(atn));
        }
    }

    public void initLexicaMenu() {
        if (null == lexiconManager) {
            throw new RuntimeException("lexiconManager is null!");
        }
        dynamicLexicaMenuItems.clear();
        for (String lang : lexiconManager.lexicaLanguagesList()) {
            dynamicLexicaMenuItems.add(lang);
        }
        Collections.sort(dynamicLexicaMenuItems);
    }

    public void OLD_DYNAMIC_initLexicaMenu(String initLang) {
        if (null == lexiconManager) {
            throw new RuntimeException("lexiconManager is null!");
        }
        dynamicLexicaMenuItems.clear();
        for (String lang : lexiconManager.lexicaLanguagesList()) {
            if (initLang.equals(lang)) {
                dynamicLexicaMenuItems.add("All languages");
            } else {
                dynamicLexicaMenuItems.add(lang);
            }
        }
        Collections.sort(dynamicLexicaMenuItems);
    }

    public void initLexicaMenu(String initLang) {
        if (null == lexiconManager) {
            throw new RuntimeException("lexiconManager is null!");
        }
        dynamicLexicaMenuItems.clear();
        dynamicLexicaMenuItems.add("All languages");
        for (String lang : lexiconManager.lexicaLanguagesList()) {
            dynamicLexicaMenuItems.add(lang);
        }
        Collections.sort(dynamicLexicaMenuItems);
    }

    public void verify() {
        String entry = ((DataTreeNode) selection.getData()).getVerified();
        int i = 0;
    }

    public void navigationEntry(String entry) {
        System.err.println("Entry " + entry);
        String entryType = "Lemma";
        resetPanels();
        lexiconCreationControllerFormDetail.setNewAction(false);
        lexiconCreationControllerFormDetail.setLemmaRendered(true);
        lexiconCreationControllerFormDetail.setAddFormButtonDisabled(false);
        lexiconCreationControllerFormDetail.setFormAlreadyExists(false);
        lexiconCreationControllerFormDetail.setLemmAlreadyExists(false);
        lexiconCreationControllerFormDetail.setIsAdmissibleLemma(true);
        switch (entryType) {
            case "Lemma":
//                if (corr) {
//                    log(Level.INFO, loginController.getAccount(), "SELECT Correspondence " + entry);
//                } else {
//                    log(Level.INFO, loginController.getAccount(), "SELECT Lemma " + entry);
//                }
                lexiconCreationControllerFormDetail.addLemma(entry);
                break;
            case "Form":
                log(Level.INFO, loginController.getAccount(), "SELECT Form " + entry);
                lexiconCreationControllerFormDetail.addForm(entry);
                break;
            default:
        }
        lexiconCreationControllerSenseDetail.setSenseRendered(true);
        lexiconCreationControllerSenseDetail.setAddSenseButtonDisabled(false);
        lexiconCreationControllerSenseDetail.setSenseToolbarRendered(true);
        lexiconCreationControllerSenseDetail.addSense(entry, entryType);
        lexiconCreationControllerFormDetail.updateAttestationBoxes();
        checkForLock(entry);
        lexiconManager.getLexiconLocker().print();
        lexiconCreationControllerRelationDetail.setActiveTab(0);
        long endTime = System.currentTimeMillis();
    }

    public void onSelect(NodeSelectEvent event) {
        boolean verified = ((DataTreeNode) event.getTreeNode().getData()).getVerified().equals("true") ? true : false;
        lexiconCreationControllerFormDetail.setVerified(verified);
        lexiconCreationControllerSenseDetail.setVerified(verified);
        long startTime = System.currentTimeMillis();
        resetPanels();
        UIComponent component = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
        String entry = ((DataTreeNode) event.getTreeNode().getData()).getOWLname();
        System.err.println("onSelect Entry: " + entry);
//        boolean corr = ((DataTreeNode) event.getTreeNode().getData()).getCorr().equals("true") ? true : false;
        String entryType = (String) component.getAttributes().get("LexicalEntryType");
        lexiconCreationControllerFormDetail.setNewAction(false);
        lexiconCreationControllerFormDetail.setLemmaRendered(true);
        lexiconCreationControllerFormDetail.setAddFormButtonDisabled(false);
        lexiconCreationControllerFormDetail.setFormAlreadyExists(false);
        lexiconCreationControllerFormDetail.setLemmAlreadyExists(false);
        lexiconCreationControllerFormDetail.setIsAdmissibleLemma(true);
        switch (entryType) {
            case "Lemma":
//                if (corr) {
//                    log(Level.INFO, loginController.getAccount(), "SELECT Correspondence " + entry);
//                } else {
//                    log(Level.INFO, loginController.getAccount(), "SELECT Lemma " + entry);
//                }
                lexiconCreationControllerFormDetail.addLemma(entry);
                break;
            case "Form":
                log(Level.INFO, loginController.getAccount(), "SELECT Form " + entry);
                lexiconCreationControllerFormDetail.addForm(entry);
                break;
            default:
        }
        lexiconCreationControllerSenseDetail.setSenseRendered(true);
        lexiconCreationControllerSenseDetail.setAddSenseButtonDisabled(false);
        lexiconCreationControllerSenseDetail.setSenseToolbarRendered(true);
        lexiconCreationControllerSenseDetail.addSense(entry, entryType);
        lexiconCreationControllerFormDetail.updateAttestationBoxes();
        checkForLock(entry);
        lexiconManager.getLexiconLocker().print();
        lexiconCreationControllerRelationDetail.setActiveTab(0);
        long endTime = System.currentTimeMillis();
        System.out.println("DURATA CONTROLLER CHE CONTIENE LE QUERIES: " + (endTime - startTime));
        log(org.apache.log4j.Level.INFO, null, "DURATA QUERY LEMMA: " + (endTime - startTime));

    }

    public void onDocumentationSelect(NodeSelectEvent event) {
        resetPanels();
        DocTreeNode dtn = ((DocTreeNode) event.getTreeNode().getData());
        lexiconCreationControllerDocumentationDetail.setNewAction(false);
        lexiconCreationControllerDocumentationDetail.setDocRendered(true);
        lexiconCreationControllerDocumentationDetail.setDocAlreadyExists(false);
        lexiconCreationControllerDocumentationDetail.addDocument(dtn);
        log(Level.INFO, loginController.getAccount(), "SELECT Document " + dtn.getAbbreviation());
        lexiconCreationControllerRelationDetail.setActiveTab(0);
    }

    public void onAttestationSelect(NodeSelectEvent event) {
        resetPanels();
        AttTreeNode atn = ((AttTreeNode) event.getTreeNode().getData());
        lexiconCreationControllerIntExtAttestationDetail.setAttRendered(true);
        lexiconCreationControllerIntExtAttestationDetail.addAttestation(atn);
        log(Level.INFO, loginController.getAccount(), "SELECT Attestation " + atn.getDocAbbreviation() + " " + atn.getManSigla() + " of " + atn.getAttestationUriLemma());
        lexiconCreationControllerRelationDetail.setActiveTab(0);
    }

    public void onOntoSelect(NodeSelectEvent event) {
        resetPanels();
        String entry = ((DataTreeNode) event.getTreeNode().getData()).getName();
        log(Level.INFO, loginController.getAccount(), "SELECT Ontology class: " + entry);
        lexiconCreationOntologyDetailController.addOntologyClassDetails(entry);
        lexiconCreationOntologyDetailController.setOntologyClassRendered(true);
    }

    private void checkForLock(String entry) {

//        System.out.println("Prima della select: ");
//        Map<String, String> ll = lexiconManager.getLexiconLockTable();
//        Iterator it = ll.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry e = (Map.Entry) it.next();
//            System.out.println(e.getKey() + " - " + e.getValue());
//        }
        // unlock the previous lexical entry
        boolean unlocked = lexiconManager.unlock();
        lexiconCreationControllerFormDetail.setPanelHeader("Lexical entry");
        if (unlocked) {
            log(Level.INFO, loginController.getAccount(), "UNLOCK the lexical entry related to " + entry);
            lexiconCreationControllerFormDetail.setLocker("");
            lexiconCreationControllerFormDetail.setLocked(false);
            lexiconCreationControllerSenseDetail.setLocked(false);
        }

        // unlock the previous relational lexical entry
        unlocked = lexiconManager.unlock();
        if (unlocked) {
            log(Level.INFO, loginController.getAccount(), "UNLOCK the lexical entry related to " + entry);
            lexiconCreationControllerRelationDetail.setLocker("");
            lexiconCreationControllerRelationDetail.setLocked(false);
        }

        // check if the lexical entry is available and lock it
        boolean locked = lexiconManager.checkForLock(entry);
        if (locked) {
            lexiconCreationControllerFormDetail.setLocked(true);
            lexiconCreationControllerSenseDetail.setLocked(true);
            lexiconCreationControllerFormDetail.setPanelHeader("Lexical entry: LOCKED by " + lexiconManager.getLockingUser(entry));
            lexiconCreationControllerFormDetail.setLocker(lexiconManager.getLockingUser(entry) + " is working ... ");
            log(Level.INFO, loginController.getAccount(), "ACCESS TO THE LOCKED lexical entry related to " + entry);
        } else {
            lexiconCreationControllerFormDetail.setLocked(false);
            lexiconCreationControllerSenseDetail.setLocked(false);
            lexiconCreationControllerFormDetail.setPanelHeader("Lexical entry");
            lexiconCreationControllerFormDetail.setLocker("");
            log(Level.INFO, loginController.getAccount(), "LOCK the lexical entry related to " + entry);
        }
    }

    // invoked when the user types a char
    public void lemmaKeyupFilterEvent(AjaxBehaviorEvent e) {
        String keyFilter = (String) e.getComponent().getAttributes().get("value");
        lemmaField = keyFilter;
        getFilteredList(lemmaRoot, cachedLemmaList, keyFilter, "Lemma");
    }

    // invoked when the user select the filter mode (starts with or contains)
    public void lemmaKeyupFilterEvent(String keyFilter) {
        getFilteredList(lemmaRoot, cachedLemmaList, keyFilter, "Lemma");
    }

    // invoked when the user types a char
    public void formKeyupFilterEvent(AjaxBehaviorEvent e) {
        String keyFilter = (String) e.getComponent().getAttributes().get("value");
        getFilteredList(formRoot, cachedFormList, keyFilter, "Form");
    }

    // invoked when the user select the filter mode (starts with or contains)
    public void formKeyupFilterEvent(String keyFilter) {
        getFilteredList(formRoot, cachedFormList, keyFilter, "Form");
    }

    // invoked when the user types a char
    public void senseKeyupFilterEvent(AjaxBehaviorEvent e) {
        String keyFilter = (String) e.getComponent().getAttributes().get("value");
        getFilteredList(senseRoot, cachedSenseList, keyFilter, "Sense");
    }

    // invoked when the user select the filter mode (starts with or contains)
    public void senseKeyupFilterEvent(String keyFilter) {
        getFilteredList(senseRoot, cachedFormList, keyFilter, "Sense");
    }

    // invoked when the user types a char
    public void docKeyupFilterEvent(AjaxBehaviorEvent e) {
        String keyFilter = (String) e.getComponent().getAttributes().get("value");
        getFilteredDocumentationList(docRoot, cachedDocumentList, cachedManuscriptList, keyFilter.toLowerCase(), false);
    }

    // invoked when the user types a char
    public void attKeyupFilterEvent(AjaxBehaviorEvent e) {
        String keyFilter = (String) e.getComponent().getAttributes().get("value");
        getFilteredAttestationList(attRoot, cachedExternalAttestationList, cachedInternalAttestationList, keyFilter.toLowerCase());
    }

    // invoked when the user select the filter mode (starts with or contains)
    public void docKeyupFilterEvent(String keyFilter) {
        getFilteredDocumentationList(docRoot, cachedDocumentList, cachedManuscriptList, keyFilter, false);
    }

// invoked when the user select the filter mode (starts with or contains)
    public void attKeyupFilterEvent(String keyFilter) {
        getFilteredAttestationList(attRoot, cachedExternalAttestationList, cachedInternalAttestationList, keyFilter);
    }

    private void resetPanels() {
        lexiconCreationControllerFormDetail.setLemmaRendered(false);
        lexiconCreationControllerFormDetail.resetFormDetails();
        lexiconCreationControllerSenseDetail.resetSenseDetails();
        lexiconCreationControllerRelationDetail.resetRelationDetails();
        lexiconCreationControllerSenseDetail.setSenseRendered(false);
        lexiconCreationControllerDocumentationDetail.setDocRendered(false);
        lexiconCreationControllerIntExtAttestationDetail.setAttRendered(false);
        lexiconCreationOntologyDetailController.setOntologyClassRendered(false);
    }

    public void searchReset(String entryType) {
        switch (entryType) {
            case "Lemma":
                if (lexiconLanguage.equals("corr")) {
                    log(Level.INFO, loginController.getAccount(), "RESET Corr Search Filter ");
                    initLemmaTabView("corr");
                    lemmaField = "";
                } else {
                    log(Level.INFO, loginController.getAccount(), "RESET Lemma Search Filter ");
                    updateCache(lexiconManager.lemmasList(getLexiconLanguage()), "Lemma");
                    getFilteredList(lemmaRoot, cachedLemmaList, "", "Lemma");
                    setLemmaField("");
                }
                break;
            case "Form":
                log(Level.INFO, loginController.getAccount(), "RESET Form Search Filter ");
                updateCache(lexiconManager.formsList(getLexiconLanguage()), "Form");
                getFilteredList(formRoot, cachedFormList, "", "Form");
                setFormField("");
                break;
            case "Document":
                log(Level.INFO, loginController.getAccount(), "RESET Document Search Filter ");
                updateDocumentationCache();
                resetDocumentFilter();
                getFilteredDocumentationList(docRoot, cachedDocumentList, cachedManuscriptList, "", true);
                break;
            case "Attestation":
                log(Level.INFO, loginController.getAccount(), "RESET Attestation Search Filter ");
//                updateAttestationCache();
                initAttestationTabView();
                resetAttestationFilter();
//                getFilteredAttestationList(attRoot, cachedExternalAttestationList, cachedInternalAttestationList, "");
                break;
            default:
        }
    }

    private void resetDocumentFilter() {
        lexiconCreationDocumentFilterController.setDocType("All");
        lexiconCreationDocumentFilterController.setAll(true);
        lexiconCreationDocumentFilterController.setExternal(false);
        lexiconCreationDocumentFilterController.setInternal(false);
        setDocField("");
    }

    private void resetAttestationFilter() {
        lexiconCreationAttestationFilterController.setDocType("All");
        lexiconCreationAttestationFilterController.setAll(true);
        lexiconCreationAttestationFilterController.setExternal(false);
        lexiconCreationAttestationFilterController.setInternal(false);
        setAttField("");
    }

    private void getFilteredList(TreeNode tn, List<Map<String, String>> list, String keyFilter, String type) {
        tn.getChildren().clear();
        Collections.sort(list, new LexiconComparator("writtenRep"));
        if (!keyFilter.equals("")) {
            if ((lexiconCreationLemmaFilterController.isStartWith() && type.equals("Lemma"))
                    || ((lexiconCreationFormFilterController.isStartWith() && type.equals("Form")))
                    || ((lexiconCreationSenseFilterController.isStartWith() && type.equals("Sense")))) {
                for (Map<String, String> m : list) {
                    if ((m.get("writtenRep").startsWith(keyFilter)) && (!m.get("writtenRep").isEmpty())) {
                        
                        if (m.get("writtenRep").contains("aromatic")) {
                            int i = 0;
                        }
                        
                        if (type.equals("Sense")) {
                            m.put("individual", m.get("writtenRep"));
                        }
                        if (type.equals("Form")) {
                            DataTreeNode dtn = new DataTreeNode(m, 0);
                            formRoot.getChildren().add(new DefaultTreeNode(dtn));
                        }
                        if (type.equals("Lemma") && isLemmaFilterable(m)) {
                            DataTreeNode dtn = new DataTreeNode(m, 0);
                            tn.getChildren().add(new DefaultTreeNode(dtn));
                        }
                    }
                }
            }
            if ((lexiconCreationLemmaFilterController.isContains() && type.equals("Lemma"))
                    || ((lexiconCreationFormFilterController.isContains() && type.equals("Form")))
                    || ((lexiconCreationSenseFilterController.isContains() && type.equals("Sense")))) {
                for (Map<String, String> m : list) {
                    if ((m.get("writtenRep").contains(keyFilter)) && (!m.get("writtenRep").isEmpty())) {
                        if (type.equals("Sense")) {
                            m.put("individual", m.get("writtenRep"));
                        }
                        if (type.equals("Form")) {
                            DataTreeNode dtn = new DataTreeNode(m, 0);
                            formRoot.getChildren().add(new DefaultTreeNode(dtn));
                        }
                        if (type.equals("Lemma") && isLemmaFilterable(m)) {
                            DataTreeNode dtn = new DataTreeNode(m, 0);
                            tn.getChildren().add(new DefaultTreeNode(dtn));
                        }
                    }
                }
            }
        } else {
            if (type.equals("Lemma")) {
                for (Map<String, String> m : list) {
                    if (isLemmaFilterable(m)) {
                        DataTreeNode dtn = new DataTreeNode(m, 0);
                        tn.getChildren().add(new DefaultTreeNode(dtn));
                    }
                }
            } else {
                if (type.equals("Form")) {
                    for (Map<String, String> m : list) {
                        DataTreeNode dtn = new DataTreeNode(m, 0);
                        formRoot.getChildren().add(new DefaultTreeNode(dtn));
                    }
                }
            }
        }
    }

    private void getFilteredDocumentationList(TreeNode tn, List<DocumentData> ddList, List<ManuscriptData> mdList, String keyFilter, boolean reset) {
        tn.getChildren().clear();
        keyFilter = (keyFilter == null) ? "" : keyFilter;
        if (lexiconCreationDocumentFilterController.isStartWith()) {
            for (DocumentData dd : ddList) {
                if (dd.getAbbreviation().toLowerCase().startsWith(keyFilter) || keyFilter.equals("")) {
                    if (isDocumentFilterable(dd) || reset) {
                        DocTreeNode dtn = new DocTreeNode(dd.getDocId(), dd.getAbbreviation(), dd.getType(), dd.getTitle(), dd.getSourceType(), 0);
                        tn.getChildren().add(new DefaultTreeNode(dtn));
                    }
                }
            }
            for (ManuscriptData md : mdList) {
                if (md.getSiglum().toLowerCase().startsWith(keyFilter) || keyFilter.equals("")) {
                    if (isManuscriptFilterable() || reset) {
                        DocTreeNode dtn = new DocTreeNode(md.getDocId(), md.getSiglum(), md.getTitle(), 0);
                        tn.getChildren().add(new DefaultTreeNode(dtn));
                    }
                }
            }
        }
        if (lexiconCreationDocumentFilterController.isContains()) {
            for (DocumentData dd : ddList) {
                if (dd.getAbbreviation().toLowerCase().contains(keyFilter) || keyFilter.equals("")) {
                    if (isDocumentFilterable(dd) || reset) {
                        DocTreeNode dtn = new DocTreeNode(dd.getDocId(), dd.getAbbreviation(), dd.getType(), dd.getTitle(), dd.getSourceType(), 0);
                        tn.getChildren().add(new DefaultTreeNode(dtn));
                    }
                }
            }
            for (ManuscriptData md : mdList) {
                if (md.getSiglum().toLowerCase().contains(keyFilter) || keyFilter.equals("")) {
                    if (isManuscriptFilterable() || reset) {
                        DocTreeNode dtn = new DocTreeNode(md.getDocId(), md.getSiglum(), md.getTitle(), 0);
                        tn.getChildren().add(new DefaultTreeNode(dtn));
                    }
                }
            }
        }
        log(Level.INFO, loginController.getAccount(), "SEARCH performed about document " + lexiconCreationDocumentFilterController.getDocType() + " type - "
                + "external: " + Boolean.toString(lexiconCreationDocumentFilterController.isExternal()) + " - "
                + "internal: " + Boolean.toString(lexiconCreationDocumentFilterController.isInternal()) + " - "
                + "all: " + Boolean.toString(lexiconCreationDocumentFilterController.isAll()) + " - "
                + (lexiconCreationDocumentFilterController.isStartWith() ? "startsWith: " : "") + keyFilter
                + (lexiconCreationDocumentFilterController.isContains() ? "contains: " : "") + keyFilter);
    }

    private boolean filteredStart(String s, String k) {
        if (k.toLowerCase().startsWith("s") || k.toLowerCase().startsWith("ṣ")) {
            if (s.startsWith(k) || s.startsWith(k.replaceFirst("s", "ṣ")) || s.startsWith(k.replaceFirst("ṣ", "s")) || k.equals("")) {
                return true;
            } else {
                return false;
            }
        } else {
            if (s.startsWith(k) || k.equals("")) {
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean filteredContain(String s, String k) {
        if (k.toLowerCase().contains("s") || k.toLowerCase().contains("ṣ")) {
            if (s.contains(k) || s.contains(k.replaceFirst("s", "ṣ")) || s.contains(k.replaceFirst("ṣ", "s")) || k.equals("")) {
                return true;
            } else {
                return false;
            }
        } else {
            if (s.contains(k) || k.equals("")) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void getFilteredAttestationList(TreeNode tn, List<ExternalAttestationData> eadList, List<InternalAttestationData> iadList, String keyFilter) {
        tn.getChildren().clear();
        keyFilter = (keyFilter == null) ? "" : keyFilter;
        if (lexiconCreationAttestationFilterController.isStartWith()) {
            for (ExternalAttestationData ead : eadList) {
//                if (ead.getAbbreviationId().toLowerCase().startsWith(keyFilter) || keyFilter.equals("")) {
                if (filteredStart(ead.getAbbreviationId().toLowerCase(), keyFilter)) {
                    if (isExternalAttestationFilterable(ead)) {
                        AttTreeNode atn = new AttTreeNode(ead.getId(), ead.getAbbreviationId(), "External", ead.getAttestationUri(), ead.getAttestationUriLemma(), ead.getDocType(), ead.getPageNumber(), "", "", 0);
//                        addInfo(atn, ead, ead.getDocType());
                        tn.getChildren().add(new DefaultTreeNode(atn));
                    }
                }
            }
            for (InternalAttestationData iad : iadList) {
                String search = iad.getDocAbbreviation() + " " + iad.getManSigla();
                if (filteredStart(search.toLowerCase(), keyFilter)) {
//                if (search.toLowerCase().startsWith(keyFilter) || keyFilter.equals("")) {
                    if (isInternalAttestationFilterable(iad)) {
                        AttTreeNode atn = new AttTreeNode(iad.getId(), iad.getDocAbbreviation(), "Internal", iad.getAttestationUri(), iad.getAttestationUriLemma(), iad.getManSigla(), iad.getChapterNumber(), iad.getParagraphNumber(), iad.getListEntry(), 0);
                        tn.getChildren().add(new DefaultTreeNode(atn));
                    }
                }
            }
        }
        if (lexiconCreationAttestationFilterController.isContains()) {
            for (ExternalAttestationData ead : eadList) {
//                if (ead.getAbbreviationId().toLowerCase().contains(keyFilter) || keyFilter.equals("")) {
                if (filteredContain(ead.getAbbreviationId().toLowerCase(), keyFilter)) {
                    if (isExternalAttestationFilterable(ead)) {
                        AttTreeNode atn = new AttTreeNode(ead.getId(), ead.getAbbreviationId(), "External", ead.getAttestationUri(), ead.getAttestationUriLemma(), ead.getDocType(), ead.getPageNumber(), "", "", 0);
//                        addInfo(atn, ead, ead.getDocType());
                        tn.getChildren().add(new DefaultTreeNode(atn));
                    }
                }
            }
            for (InternalAttestationData iad : iadList) {
                String search = iad.getDocAbbreviation() + " " + iad.getManSigla();
//                if (search.toLowerCase().contains(keyFilter) || keyFilter.equals("")) {
                if (filteredContain(search.toLowerCase(), keyFilter)) {
                    if (isInternalAttestationFilterable(iad)) {
                        AttTreeNode atn = new AttTreeNode(iad.getId(), iad.getDocAbbreviation(), "Internal", iad.getAttestationUri(), iad.getAttestationUriLemma(), iad.getManSigla(), iad.getChapterNumber(), iad.getParagraphNumber(), iad.getListEntry(), 0);
                        tn.getChildren().add(new DefaultTreeNode(atn));
                    }
                }
            }
        }
        log(Level.INFO, loginController.getAccount(), "SEARCH performed about attestation " + lexiconCreationAttestationFilterController.getDocType() + " type - "
                + "external: " + Boolean.toString(lexiconCreationAttestationFilterController.isExternal()) + " - "
                + "internal: " + Boolean.toString(lexiconCreationAttestationFilterController.isInternal()) + " - "
                + "all: " + Boolean.toString(lexiconCreationAttestationFilterController.isAll()) + " - "
                + (lexiconCreationAttestationFilterController.isStartWith() ? "startsWith: " : "") + keyFilter
                + (lexiconCreationAttestationFilterController.isContains() ? "contains: " : "") + keyFilter);
    }

    private boolean isDocumentFilterable(DocumentData dd) {
        if (lexiconCreationDocumentFilterController.isAll()) {
            // both internal and external documents are required
            if (lexiconCreationDocumentFilterController.getDocType().equals("All")) {
                // all document types are required
                return true;
            } else {
                return dd.getType().equals(lexiconCreationDocumentFilterController.getDocType());
            }
        } else {
            if (lexiconCreationDocumentFilterController.getDocType().equals("All")) {
                // all document types are required
                if (lexiconCreationDocumentFilterController.isExternal()) {
                    return dd.getSourceType().equals("External");
                } else {
                    return dd.getSourceType().equals("Internal");
                }
            } else {
                if (lexiconCreationDocumentFilterController.isExternal()) {
                    return dd.getSourceType().equals("External") && dd.getType().equals(lexiconCreationDocumentFilterController.getDocType());
                } else {
                    return dd.getSourceType().equals("Internal") && dd.getType().equals(lexiconCreationDocumentFilterController.getDocType());
                }
            }
        }
    }

    private boolean isManuscriptFilterable() {
        if (lexiconCreationDocumentFilterController.getDocType().equals("Manuscript")) {
            if (lexiconCreationDocumentFilterController.isInternal() || lexiconCreationDocumentFilterController.isAll()) {
                return true;
            } else {
                return false;
            }
        } else {
            if (lexiconCreationDocumentFilterController.getDocType().equals("All")) {
                if (lexiconCreationDocumentFilterController.isInternal() || lexiconCreationDocumentFilterController.isAll()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private boolean isExternalAttestationFilterable(ExternalAttestationData ead) {
        if (lexiconCreationAttestationFilterController.isAll()) {
            // both internal and external attestation are required
            if (lexiconCreationAttestationFilterController.getDocType().equals("All")) {
                // all document types are required
                return true;
            } else {
                return ead.getDocType().equals(lexiconCreationAttestationFilterController.getDocType());
            }
        } else {
            if (lexiconCreationAttestationFilterController.getDocType().equals("All")) {
                // all attestation types are required
                if (lexiconCreationAttestationFilterController.isExternal()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (lexiconCreationAttestationFilterController.isExternal()) {
                    return ead.getDocType().equals(lexiconCreationAttestationFilterController.getDocType());
                } else {
                    return false;
                }
            }
        }
    }

    private boolean isInternalAttestationFilterable(InternalAttestationData iad) {
        if (lexiconCreationAttestationFilterController.isAll()) {
            // both internal and external attestation are required
            if (lexiconCreationAttestationFilterController.getDocType().equals("All")) {
                // all document types are required
                return true;
            } else {
                return iad.getDocType().equals(lexiconCreationAttestationFilterController.getDocType());
            }
        } else {
            if (lexiconCreationAttestationFilterController.getDocType().equals("All")) {
                // all attestation types are required
                if (lexiconCreationAttestationFilterController.isInternal()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (lexiconCreationAttestationFilterController.isInternal()) {
                    return iad.getDocType().equals(lexiconCreationAttestationFilterController.getDocType());
                } else {
                    return false;
                }
            }
        }
    }

    private boolean isLemmaFilterable(Map<String, String> m) {
        if (lexiconCreationLemmaFilterController.isAll()) {
            return true;
        } else {
            if (lexiconCreationLemmaFilterController.isExt()) {
                if (m.get("lemmaInfo") != null) {
                    return m.get("lemmaInfo").equals("corpusExternalLemma");
                } else {
                    return false;
                }
            } else {
                if (lexiconCreationLemmaFilterController.isRec()) {
                    if (m.get("lemmaInfo") != null) {
                        return m.get("lemmaInfo").equals("reconstructedForm");
                    } else {
                        return false;
                    }
                } else {
                    if (lexiconCreationLemmaFilterController.isHyp()) {
                        if (lexiconCreationLemmaFilterController.isRec()) {
                            if (m.get("lemmaInfo") != null) {
                                return m.get("lemmaInfo").equals("hypoteticalForm");
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    public String getAttestationLabel(AttTreeNode atn) {
        if (atn.getType().contains("Int")) {
            return atn.getDocAbbreviation() + " " + atn.getManSigla() + " " + (atn.getChapterNumber() != null ? atn.getChapterNumber() : "")
                    + " " + (atn.getParagraphNumber() != null ? atn.getParagraphNumber() : "")
                    + " " + (atn.getListEntry() != null ? atn.getListEntry() : "");
        } else {
            return getExternalAttestationLabelByType(atn);
        }
    }

    private String getExternalAttestationLabelByType(AttTreeNode atn) {
//        String ret = "";
//        switch (atn.getManSigla()) {
//            case "Dictionary": {
//                ret = atn.getDocAbbreviation() + " " + atn.getSvSubvoce() + " " + atn.getFascicle() + " " + atn.getVolume()
//                        + " " + atn.getSubVolume() + " " + atn.getPageNumber() + " " + atn.getEntryNumber() + " " + atn.getColumnNumber();
//                break;
//            }
//            case "Map": {
//                ret = atn.getDocAbbreviation() + " " + atn.getNumberOfMap() + " " + atn.getNumberOfGeoPoint();
//                break;
//            }
//            case "Book": {
//                ret = atn.getDocAbbreviation() + " " + atn.getBookNumber() + " " + atn.getChapterNumber() + " " + atn.getPart()
//                        + " " + atn.getVolume() + " " + atn.getSection() + " " + atn.getEntryNumber() + " " + atn.getFolioNumber()
//                        + " " + atn.getPageNumber();
//                break;
//            }
//            default: {
//                break;
//            }
//        }
//        return ret;
        return atn.getDocAbbreviation();
    }

    public String getAttestationLabelStyleClass(AttTreeNode atn) {
        return atn.getAttestationUri().contains("_form") ? "formInAttestationLabel" : "lemmaInAttestationLabel";
    }

    public void collapseOnto() {
        initDomainOntologyTabView("collapse");
        setSelection(null);
    }

    public void expandOnto() {
        initDomainOntologyTabView();
        setSelection(null);
    }

    /*
    French #5858FA
    English #AC58FA

    Hebrew #58FAF4
    Arabic #9AFE2E
    Aramaic #FACC2E
    Greek #AC58FA
    Latin #2EFE9A

    Old Occitan #FE2E2E
    Old Catalan #2E2EFE
    [11:07:49] Anja Weingart: for cat: #5F04B4 and for uncertain: #F6CECE . For the mixed languages we do not need an extra color (aocheb, aocarab, aoclat). lataoc will disappear because its all foreignterms
     */
    public String getLanguageColor(String lang) {
        switch (lang) {
            case "fr":
                return "color: #5858FA;";
            case "lat":
                return "color: #2EFE9A;";
            case "acat":
                return "color: #2E2EFE;";
            case "aoc":
                return "color: #FE2E2E;";
            case "cat":
                return "color: #5F04B4;";
            case "uncertain":
                return "color: #F6CECE;";
            case "heb":
                return "color: #58FAF4;";
            case "arab":
                return "color: #9AFE2E;";
            case "aram":
                return "color: #FACC2E;";
            case "gr":
                return "color: #AC58FA;";
            case "en":
                return "color: #AC58FA;";
        }
        return "color: #9E9E9E";

    }

    public static class DataTreeNode {

        private int hierarchyLevel;
        private String name;
        private String OWLname;
        private String language;
        private String type;
        private String verified;
        private String corr;
        private String info;

        public DataTreeNode(String name, String OWLname, String language, String type, String verified, String corr, String info, int hierarchyLevel) {
            this.name = name;
            this.OWLname = OWLname;
            this.hierarchyLevel = hierarchyLevel;
            this.language = language;
            this.type = type;
            this.verified = "false";
            this.corr = "false";
            this.info = info;
        }

        public DataTreeNode(Map<String, String> m, int hierarchyLevel) {
            this.hierarchyLevel = hierarchyLevel;
            for (Map.Entry<String, String> entry : m.entrySet()) {
                switch (entry.getKey()) {
                    case "writtenRep":
                        this.name = entry.getValue();
                        break;
                    case "individual":
                        this.OWLname = entry.getValue();
                        break;
                    case "lang":
                        this.language = entry.getValue();
                        break;
                    case "type":
                        this.type = entry.getValue();
                        break;
                    case "corr":
                        this.corr = entry.getValue();
                        break;
                    case "verified":
                        this.verified = entry.getValue();
                        break;
                    case "lemmaInfo":
                        this.info = entry.getValue();
                        break;
                }
            }
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public String getCorr() {
            return corr;
        }

        public void setCorr(String corr) {
            this.corr = corr;
        }

        public String getOWLname() {
            return OWLname;
        }

        public void setOWLname(String OWLname) {
            this.OWLname = OWLname;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public int getHierarchyLevel() {
            return hierarchyLevel;
        }

        public void setHierarchyLevel(int hierarchyLevel) {
            this.hierarchyLevel = hierarchyLevel;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getVerified() {
            return verified;
        }

        public void setVerified(String verified) {
            this.verified = verified;
        }

    }

    public static class DocTreeNode {

        private int hierarchyLevel;
        private Long docId;
        private String abbreviation;
        private String type;
        private String title;
        private String sourceType;

        public DocTreeNode(Long docId, String abbreviation, String type, String title, String sourceType, int hierarchyLevel) {
            this.docId = docId;
            this.abbreviation = abbreviation;
            this.hierarchyLevel = hierarchyLevel;
            this.sourceType = sourceType;
            this.type = type;
            this.title = title;
        }

        // only for manuscripts
        public DocTreeNode(Long docId, String abbreviation, String title, int hierarchyLevel) {
            this.docId = docId;
            this.abbreviation = abbreviation;
            this.hierarchyLevel = hierarchyLevel;
            this.sourceType = "Internal";
            this.type = "Manuscript";
            this.title = title;
        }

        public Long getDocId() {
            return docId;
        }

        public void setDocId(Long docId) {
            this.docId = docId;
        }

        public int getHierarchyLevel() {
            return hierarchyLevel;
        }

        public void setHierarchyLevel(int hierarchyLevel) {
            this.hierarchyLevel = hierarchyLevel;
        }

        public String getAbbreviation() {
            return abbreviation;
        }

        public void setAbbreviation(String abbreviation) {
            this.abbreviation = abbreviation;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

    }

    public static class AttTreeNode {

        private int hierarchyLevel;
        private Long attId;
        private String docAbbreviation;
        private String chapterNumber;
        private String type;
        private String attestationUri;
        private String attestationUriLemma;
        private String manSigla;
        private String paragraphNumber;
        private String listEntry;

        private String svSubvoce;
        private String fascicle;
        private String volume;
        private String pageNumber;
        private String entryNumber;
        private String columnNumber;
        private String numberOfMap;
        private String numberOfGeoPoint;
        private String bookNumber;
        private String part;
        private String section;
        private String folioNumber;
        private String subVolume;

        public AttTreeNode(Long attId, String docAbbreviation, String type, String attestationUri, String attestationUriLemma, String manSigla, String chapterNumber, String paragraphNumber, String listEntry, int hierarchyLevel) {
            this.attId = attId;
            this.docAbbreviation = docAbbreviation;
            this.hierarchyLevel = hierarchyLevel;
            this.type = type;
            this.attestationUri = attestationUri;
            this.manSigla = manSigla;
            this.chapterNumber = chapterNumber;
            this.attestationUriLemma = attestationUriLemma;
            this.paragraphNumber = paragraphNumber;
            this.listEntry = listEntry;
        }

        public void addExternalAttestationDictionary(String svSublemma, String fascicle, String volume, String subvolume, String pageNumber, String entryNumber, String columnNumber) {
            this.svSubvoce = svSublemma;
            this.fascicle = fascicle;
            this.volume = volume;
            this.subVolume = subvolume;
            this.pageNumber = pageNumber;
            this.entryNumber = entryNumber;
            this.columnNumber = columnNumber;
        }

        public void addExternalAttestationMap(String numberOfMap, String numberOfGeoPoint) {
            this.numberOfMap = numberOfMap;
            this.numberOfGeoPoint = numberOfGeoPoint;
        }

        public void addExternalAttestationBook(String bookNumber, String chapterNumber, String part, String volume, String section, String entryNumber, String folioNumber, String pageNumber) {
            this.bookNumber = bookNumber;
            this.chapterNumber = chapterNumber;
            this.part = part;
            this.volume = volume;
            this.section = section;
            this.entryNumber = entryNumber;
            this.folioNumber = folioNumber;
            this.pageNumber = pageNumber;
        }

        public String getSubVolume() {
            return subVolume;
        }

        public void setSubVolume(String subVolume) {
            this.subVolume = subVolume;
        }

        public String getSvSubvoce() {
            return svSubvoce;
        }

        public void setSvSubvoce(String svSubvoce) {
            this.svSubvoce = svSubvoce;
        }

        public String getFascicle() {
            return fascicle;
        }

        public void setFascicle(String fascicle) {
            this.fascicle = fascicle;
        }

        public String getVolume() {
            return volume;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }

        public String getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(String pageNumber) {
            this.pageNumber = pageNumber;
        }

        public String getEntryNumber() {
            return entryNumber;
        }

        public void setEntryNumber(String entryNumber) {
            this.entryNumber = entryNumber;
        }

        public String getColumnNumber() {
            return columnNumber;
        }

        public void setColumnNumber(String columnNumber) {
            this.columnNumber = columnNumber;
        }

        public String getNumberOfMap() {
            return numberOfMap;
        }

        public void setNumberOfMap(String numberOfMap) {
            this.numberOfMap = numberOfMap;
        }

        public String getNumberOfGeoPoint() {
            return numberOfGeoPoint;
        }

        public void setNumberOfGeoPoint(String numberOfGeoPoint) {
            this.numberOfGeoPoint = numberOfGeoPoint;
        }

        public String getBookNumber() {
            return bookNumber;
        }

        public void setBookNumber(String bookNumber) {
            this.bookNumber = bookNumber;
        }

        public String getPart() {
            return part;
        }

        public void setPart(String part) {
            this.part = part;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public String getFolioNumber() {
            return folioNumber;
        }

        public void setFolioNumber(String folioNumber) {
            this.folioNumber = folioNumber;
        }

        public String getParagraphNumber() {
            return paragraphNumber;
        }

        public void setParagraphNumber(String paragraphNumber) {
            this.paragraphNumber = paragraphNumber;
        }

        public String getListEntry() {
            return listEntry;
        }

        public void setListEntry(String listEntry) {
            this.listEntry = listEntry;
        }

        public String getDocAbbreviation() {
            return docAbbreviation;
        }

        public void setDocAbbreviation(String docAbbreviation) {
            this.docAbbreviation = docAbbreviation;
        }

        public String getManSigla() {
            return manSigla;
        }

        public void setManSigla(String manSigla) {
            this.manSigla = manSigla;
        }

        public int getHierarchyLevel() {
            return hierarchyLevel;
        }

        public void setHierarchyLevel(int hierarchyLevel) {
            this.hierarchyLevel = hierarchyLevel;
        }

        public Long getAttId() {
            return attId;
        }

        public void setAttId(Long attId) {
            this.attId = attId;
        }

        public String getChapterNumber() {
            return chapterNumber;
        }

        public void setChapterNumber(String chapterNumber) {
            this.chapterNumber = chapterNumber;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAttestationUri() {
            return attestationUri;
        }

        public void setAttestationUri(String attestationUri) {
            this.attestationUri = attestationUri;
        }

        public String getAttestationUriLemma() {
            return attestationUriLemma;
        }

        public void setAttestationUriLemma(String attestationUriLemma) {
            this.attestationUriLemma = attestationUriLemma;
        }

    }

}
