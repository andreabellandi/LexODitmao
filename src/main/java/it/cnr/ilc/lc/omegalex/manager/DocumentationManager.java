/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.manager;

import it.cnr.ilc.lc.omegalex.HibernateUtil;
import it.cnr.ilc.lc.omegalex.domain.Document;
import it.cnr.ilc.lc.omegalex.domain.ExternalAttestation;
import it.cnr.ilc.lc.omegalex.domain.InternalAttestation;
import it.cnr.ilc.lc.omegalex.domain.Manuscript;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author andrea
 */
public class DocumentationManager implements Serializable {

    @Inject
    private DomainManager domainManager;

    public List<Document> getDocuments() {
        return HibernateUtil.getSession().createCriteria(Document.class).addOrder(Order.asc("abbreviation")).list();
    }

    public List<Manuscript> getManuscripts() {
        return HibernateUtil.getSession().createCriteria(Manuscript.class).addOrder(Order.asc("siglum")).list();
    }

    private final String sqlExternalAttestations = "SELECT d.abbreviation, ea.id, ea.attestationUri, d.type, ea.pageNumber, ea.attestationUriLemma "
            //            + ", " +
            //            "ea.articleNumber, ea.book, ea.chapterNumberRoman, ea.columnNumber, ea.entryNumber, ea.fascicle, ea.folioNumber, ea.footnoteNumber, ea.form, "
            //            + "ea.glossaryNumber, ea.lineNumber, ea.numberOfGeographicPoint, ea.numberOfMap, ea.other, ea.pageNumber, ea.paragraphNumber, ea.part, ea.sec, "
            //            + "ea.subvolume, ea.svSublemma, ea.url, ea.volume "
            + "FROM ExternalAttestation ea join Document d on ea.document_id = d.id "
            + "WHERE ea.status = 1 and d.status = 1 ORDER BY ea.attestationUri, d.abbreviation";

    public List<Object[]> getExternalAttestations() {
        SQLQuery sqlQuery = HibernateUtil.getSession().createSQLQuery(sqlExternalAttestations);
        return sqlQuery.list();
    }

    private final String sqlInternalAttestations = "SELECT d.abbreviation, ia.id, ia.attestationUri, d.type, ia.pageNumber "
            + "FROM InternalAttestation ia join Document d on ia.document_id = d.id "
            + "WHERE ia.status = 1 and d.status = 1 ORDER BY ia.attestationUri, d.abbreviation";

    public List<Object[]> _getInternalAttestations() {
        SQLQuery sqlQuery = HibernateUtil.getSession().createSQLQuery(sqlInternalAttestations);
        return sqlQuery.list();
    }

    public List<InternalAttestation> getInternalAttestations() {
        return HibernateUtil.getSession().createCriteria(InternalAttestation.class).addOrder(Order.asc("attestationUri")).list();
    }

    public Document getDocumentByID(Long id) {
        return (Document) (HibernateUtil.getSession().get(Document.class, id));
    }

    public Document getDocumentByAbbreviation(String abbreviation) {
        return (Document) HibernateUtil.getSession().createCriteria(Document.class).add(Restrictions.eq("abbreviation", abbreviation)).list().get(0);
    }

    public Manuscript getManuscriptByID(Long id) {
        return (Manuscript) (HibernateUtil.getSession().get(Manuscript.class, id));
    }

    private final String docsAbb = "SELECT abbreviation FROM Document WHERE status = 1 and sourceType = '_SOURCE_TYPE_' ORDER BY abbreviation";

    public List<String> getAbbreviationDocuments(String sourceType) {
        SQLQuery sqlQuery = HibernateUtil.getSession().createSQLQuery(docsAbb.replaceAll("_SOURCE_TYPE_", sourceType));
        return sqlQuery.list();
    }

    public boolean abbreviationDocumentAlreadyExists(String abbreviation) {
        return (HibernateUtil.getSession().createCriteria(Document.class).add(Restrictions.eq("abbreviation", abbreviation)).list()).size() > 0;
    }

    public boolean abbreviationManuscriptAlreadyExists(String siglum) {
        return (HibernateUtil.getSession().createCriteria(Manuscript.class).add(Restrictions.eq("siglum", siglum)).list()).size() > 0;
    }

    public Manuscript getManuscriptByAbbreviation(String siglum) {
        return (Manuscript) HibernateUtil.getSession().createCriteria(Manuscript.class).add(Restrictions.eq("siglum", siglum)).list().get(0);
    }

    public ExternalAttestation getExternalAttestationByID(Long id) {
        return (ExternalAttestation) HibernateUtil.getSession().createCriteria(ExternalAttestation.class).add(Restrictions.eq("id", id)).list().get(0);
    }

    public InternalAttestation getInternalAttestationByID(Long id) {
        return (InternalAttestation) HibernateUtil.getSession().createCriteria(InternalAttestation.class).add(Restrictions.eq("id", id)).list().get(0);
    }

    public List<ExternalAttestation> getExternalAttestationByLexicalEntry(String lexicalEntry) {
        return HibernateUtil.getSession().createCriteria(ExternalAttestation.class).add(Restrictions.eq("attestationUri", lexicalEntry)).list();
    }

    public List<InternalAttestation> getInternalAttestationByLexicalEntry(String lexicalEntry) {
        return HibernateUtil.getSession().createCriteria(InternalAttestation.class).add(Restrictions.eq("attestationUri", lexicalEntry)).list();
    }

    public void deleteDocumentByID(Long id, String type) {
        domainManager.delete(type.equals("Manuscript") ? getManuscriptByID(id) : getDocumentByID(id));
    }

    public void deleteExternalAttestationByID(Long id) {
        domainManager.delete(getExternalAttestationByID(id));
    }

    public void deleteInternalAttestationByID(Long id) {
        domainManager.delete(getInternalAttestationByID(id));
    }

    public List<InternalAttestation> getInternalAttestation() {
        return HibernateUtil.getSession().createCriteria(InternalAttestation.class).add(Restrictions.eq("status", 1)).list();
    }

    public Long insertDocument(DocumentData dd) {
        if (dd.getType().equals("Manuscript")) {
            Manuscript m = new Manuscript();
            m.setSiglum(dd.getAbbreviation());
            m.setTitle(dd.getTitle());
            return domainManager.insert(m);
        } else {
            Document d = new Document();
            d.setAbbreviation(dd.getAbbreviation());
            d.setSourceType(dd.getSourceType());
            d.setTitle(dd.getTitle());
            d.setType(dd.getType());
            return domainManager.insert(d);
        }
    }

    private List<Manuscript> getReferencedManuscripts(List<ManuscriptData> mdl) {
        List<Manuscript> ml = new ArrayList<Manuscript>();
        for (ManuscriptData md : mdl) {
            if (md.getSiglum() != null && !md.getSiglum().isEmpty()) {
                Manuscript m = getManuscriptByAbbreviation(md.getSiglum());
                ml.add(m);
            }
        }
        return ml;
    }

    public void updateDocument(DocumentData dd) {
        if (dd.getType().equals("Manuscript")) {
            Manuscript m = new Manuscript();
            m.setSiglum(dd.getAbbreviation());
            m.setTitle(dd.getTitle());
            m.setId(dd.getDocId());
            domainManager.update(m);
        } else {
            Document d = getDocumentByID(dd.getDocId());
            d.setAbbreviation(dd.getAbbreviation());
            d.setSourceType(dd.getSourceType());
            d.setType(dd.getType());
            d.setTitle(dd.getTitle());
            domainManager.update(d);
        }
    }

    public void updateExtAttestation(ExternalAttestationData ead) {
        domainManager.update(setExtAttestation(ead));
    }

    public void updateIntAttestation(InternalAttestationData iad) {
        domainManager.update(setIntAttestation(iad));
    }

    public void updateIntAttestationUri(InternalAttestationData oldAtt, LemmaData newLemma) {
        InternalAttestation ia = setIntAttestation(oldAtt);
        ia.setAttestationUri("http://ditmao.ilc.cnr.it/ditmao#" + getIRI(newLemma.getFormWrittenRepr(), newLemma.getLanguage(), "lemma"));
        ia.setAttestationUriLemma(newLemma.getFormWrittenRepr());
        domainManager.update(ia);
    }

    public void updateIntAttestationFormUri(InternalAttestationData oldAtt, FormData newForm, LemmaData ld) {
        InternalAttestation ia = setIntAttestation(oldAtt);
        ia.setAttestationUri("http://ditmao.ilc.cnr.it/ditmao#" + getIRI(ld.getFormWrittenRepr(), ld.getLanguage(), newForm.getFormWrittenRepr(), "form"));
//        ia.setAttestationUri("http://ditmao.ilc.cnr.it/ditmao#" + ld.getFormWrittenRepr() + "_" + ld.getLanguage() + "_" + newForm.getFormWrittenRepr() + "_form");
        ia.setAttestationUriLemma(newForm.getFormWrittenRepr());
        domainManager.update(ia);
    }

    public void updateExtAttestationUri(ExternalAttestationData oldAtt, LemmaData newLemma) {
        ExternalAttestation ea = setExtAttestation(oldAtt);
        ea.setAttestationUri("http://ditmao.ilc.cnr.it/ditmao#" + getIRI(newLemma.getFormWrittenRepr(), newLemma.getLanguage(), "lemma"));
        ea.setAttestationUriLemma(newLemma.getFormWrittenRepr());
        domainManager.update(ea);
    }

    private final String sql = "SELECT count(*)"
            + "FROM Document_Manuscript dm join Manuscript m on dm.manuscripts_id = m.id join Document d on d.id = dm.document_id "
            + "WHERE m.status = 1 and d.status = 1 and m.siglum = 'ABBR_MAN' ";

    public boolean isEditedManuscript(String abbr) {
        SQLQuery sqlQuery = HibernateUtil.getSession().createSQLQuery(sql.replace("ABBR_MAN", abbr));
        return ((Number) sqlQuery.uniqueResult()).intValue() > 0;
    }

    private final String sqlEditedIn = "SELECT d.abbreviation, d.title "
            + "FROM Document_Manuscript dm join Manuscript m on dm.manuscripts_id = m.id join Document d on dm.Document_id = d.id "
            + "WHERE m.status = 1 and d.status = 1 and m.siglum = 'ABBR_MAN'";

    public List<Object[]> getEditions(String abbr) {
        SQLQuery sqlQuery = HibernateUtil.getSession().createSQLQuery(sqlEditedIn.replace("ABBR_MAN", abbr));
        return sqlQuery.list();
    }

    public Long insertExtAttestation(ExternalAttestationData ead) {
        return domainManager.insert(setExtAttestation(ead));
    }

    private final String sqlDocumentType = "SELECT d.type "
            + "FROM ExternalAttestation ea join Document d on ea.document_id = d.id "
            + "WHERE ea.status = 1 and d.status = 1 and d.id = _ID_";

    public List<Object[]> getDocumentType(Long id) {
        SQLQuery sqlQuery = HibernateUtil.getSession().createSQLQuery(sqlDocumentType.replace("_ID_", id.toString()));
        return sqlQuery.list();
    }

    private ExternalAttestation setExtAttestation(ExternalAttestationData ead) {
        ExternalAttestation ea = new ExternalAttestation();
        ea.setId(ead.getId());
        ea.setOther(ead.getOther());
        ea.setSec(ead.getSec());
        ea.setArticleNumber(ead.getArticleNumber());
        ea.setAttestationUri(ead.getAttestationUri());
        ea.setAttestationUriLemma(ead.getAttestationUriLemma());
        ea.setBook(ead.getBook());
        ea.setChapterNumberRoman(ead.getChapterNumberRoman());
        ea.setColumnNumber(ead.getColumnNumber());
        ea.setEntryNumber(ead.getEntryNumber());
        ea.setFascicle(ead.getFascicle());
        ea.setFolioNumber(ead.getFolioNumber());
        ea.setFootnoteNumber(ead.getFootnoteNumber());
        ea.setGlossaryNumber(ead.getFootnoteNumber());
        ea.setLineNumber(ead.getLineNumber());
        ea.setNumberOfGeographicPoint(ead.getNumberOfGeographicPoint());
        ea.setNumberOfMap(ead.getNumberOfMap());
        ea.setPageNumber(ead.getPageNumber());
        ea.setParagraphNumber(ead.getParagraphNumber());
        ea.setPart(ead.getPart());
        ea.setSubvolume(ead.getSubvolume());
        ea.setSvSublemma(ead.getSvSublemma());
        ea.setVolume(ead.getVolume());
        ea.setForm(ead.getForm());
        ea.setDocument(getDocumentByAbbreviation(ead.getAbbreviationId()));
        ea.setUrl(ead.getUrl());
        return ea;
    }

    public Long insertIntAttestation(InternalAttestationData iad) {
        return domainManager.insert(setIntAttestation(iad));
    }

    private InternalAttestation setIntAttestation(InternalAttestationData iad) {
        InternalAttestation ia = new InternalAttestation();
        ia.setId(iad.getId());
        ia.setAttestationUri(iad.getAttestationUri());
        ia.setAttestationUriLemma(iad.getAttestationUriLemma());
        ia.setChapterNumber(iad.getChapterNumber());
        ia.setLineNumber(iad.getLineNumber());
//        ia.setListEntryLetter(iad.getListEntryLetter());
//        ia.setListEntryNumber(iad.getListEntryNumber());
        ia.setListEntry(iad.getListEntry());
        ia.setPageNumber(iad.getPageNumber());
        ia.setParagraphNumber(iad.getParagraphNumber());
        ia.setSectionNumber(iad.getSectionNumber());
        ia.setManuscript(loadManuscript(iad.getManSigla()));
        ia.setDocument(getDocumentByAbbreviation(iad.getDocAbbreviation()));
        return ia;
    }

    private Manuscript loadManuscript(String siglum) {
        return (Manuscript) HibernateUtil.getSession().createCriteria(Manuscript.class).add(Restrictions.eq("siglum", siglum)).uniqueResult();
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
        instance = instance.replaceAll("\\?", "_QUEST");
        instance = instance.replaceAll("\\,", "COMA_");
        instance = instance.replaceAll("-", "_HYPEN_");
        instance = instance.replaceAll("\\{", "");
        instance = instance.replaceAll("\\}", "");
        instance = instance.replaceAll("\\.", "");
        instance = instance.replaceAll("\\:", "");
        instance = instance.replaceAll("\\;", "");
        instance = instance.replaceAll("\\!", "");
        instance = instance.replaceAll("\\'", "_APOS_");
//        instance = instance.replaceAll("\\'", "_");
        instance = instance.replaceAll("\\’", "");
        instance = instance.replaceAll("\\‘", "");
        instance = instance.replaceAll("\\s+", "_");
        instance = instance.replaceAll("/", "_");
        instance = instance.replaceAll(" ", " ");
        instance = instance.replaceAll(" +(\\d)", "$1");
        return instance;
    }

}
