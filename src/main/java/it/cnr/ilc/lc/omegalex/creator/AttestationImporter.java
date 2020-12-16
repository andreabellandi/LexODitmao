package it.cnr.ilc.lc.omegalex.creator;

import it.cnr.ilc.lc.omegalex.HibernateUtil;
import it.cnr.ilc.lc.omegalex.domain.Document;
import it.cnr.ilc.lc.omegalex.domain.InternalAttestation;
import it.cnr.ilc.lc.omegalex.domain.Manuscript;
import it.cnr.ilc.lc.omegalex.manager.DomainManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author davide albanesi
 */
public class AttestationImporter {

    private static final Logger LOG = LogManager.getLogger(AttestationImporter.class);

    private final DomainManager domainManager = new DomainManager();

    private static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://ditmao.ilc.cnr.it:5432/ditman";
        String user = "pinakes3";
        String pass = "pippo";
        Connection connection = DriverManager.getConnection(url, user, pass);
        return connection;
    }

    void importAttestations() throws ClassNotFoundException, SQLException {
        try (Connection connection = getConnection()) {
            deleteAttestations();
            deleteDocuments();
            deleteManuscripts();
            importDocuments(connection);
            importManuscripts(connection);
            importAttestations(connection);
        }
    }

    private void deleteDocuments() {
        HibernateUtil.getSession().createSQLQuery("delete from Document").executeUpdate();
        HibernateUtil.getSession().flush();
    }

    private static final String SELECT_GROUP_OF_DOCUMENT = ""
            + "select distinct replace(replace(vr.value, ')', ''), '(', '') title\n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 851";

    private void importDocuments(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(SELECT_GROUP_OF_DOCUMENT);
            while (result.next()) {
                insertDocument(result);
            }
        }
        HibernateUtil.getSession().flush();
    }

    private void insertDocument(ResultSet result) throws SQLException {
        Document document = new Document();
        document.setAbbreviation(result.getString("title"));
        document.setType("Text");
        document.setSourceType("Internal");
        domainManager.insert(document);
    }

    private void deleteManuscripts() {
        HibernateUtil.getSession().createSQLQuery("delete from Manuscript").executeUpdate();
        HibernateUtil.getSession().flush();
    }

    private static final String SELECT_DOCUMENT = ""
            + "select distinct replace(replace(m.title, ')', ''), '(', '') title\n"
            + "from (\n"
            + "select p.idinstclassdomain, vr.value title\n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 760\n"
            + ") m join (\n"
            + "select p.idinstclassdomain, vr.value\n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 832 and vr.value = 'manuscrit'\n"
            + ") t on m.idinstclassdomain = t.idinstclassdomain";

    private void importManuscripts(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(SELECT_DOCUMENT);
            while (result.next()) {
                insertManuscript(result);
            }
        }
        HibernateUtil.getSession().flush();
    }

    private void insertManuscript(ResultSet result) throws SQLException {
        Manuscript manuscript = new Manuscript();
        manuscript.setSiglum(result.getString("title"));
        checkDoubleManuscript(manuscript);
        domainManager.insert(manuscript);
    }

    private void checkDoubleManuscript(Manuscript manuscript) {
        if (manuscript.getSiglum().equals("P")) {
            Manuscript manuscript2 = new Manuscript();
            manuscript2.setSiglum("P(=Paris1)");
            domainManager.insert(manuscript2);
            manuscript2 = new Manuscript();
            manuscript2.setSiglum("P(=Paris2)");
            domainManager.insert(manuscript2);
            manuscript.setSiglum("P(=Princeton)");
        } else if (manuscript.getSiglum().equals("O")) {
            Manuscript manuscript2 = new Manuscript();
            manuscript2.setSiglum("O(=Oxford2)");
            domainManager.insert(manuscript2);
            manuscript.setSiglum("O(=Oxford1)");
        } else if (manuscript.getSiglum().equals("V")) {
            Manuscript manuscript2 = new Manuscript();
            manuscript2.setSiglum("V(=Vatican2)");
            domainManager.insert(manuscript2);
            manuscript.setSiglum("V(=Vatican1)");
        }
    }

    private void deleteAttestations() {
        HibernateUtil.getSession().createSQLQuery("delete from InternalAttestation").executeUpdate();
        HibernateUtil.getSession().createSQLQuery("delete from ExternalAttestation").executeUpdate();
        HibernateUtil.getSession().flush();
    }

    private static final String SELECT_DOCUMENTED_IN_LEMMA = ""
            + "select t.lemma, t.langtype, t.\"document\", t.manuscript, t.volume, t.\"column\", string_agg(t.paragraph, ', ') paragraph, t.line, t.page, t.chapter from (\n"
            + "select al.value lemma, alt.value langtype, replace(replace(d.value, ')', ''), '(', '') \"document\", replace(replace(replace(m.value, ' [manuscrit]' , ''), ')', ''), '(', '') manuscript, v.value volume, c.value \"column\", p.value paragraph, l.value line, g.value page, h.value chapter\n"
            + "from (\n"
            + "select p.idinstclassdomain, p.idinstclassrank \n"
            + "from instpredicate p \n"
            + "where p.idpredicate = 781\n"
            + ") a join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 800\n"
            + ") d on a.idinstclassrank = d.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 854\n"
            + ") m on a.idinstclassrank = m.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 807\n"
            + ") v on a.idinstclassrank = v.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 809\n"
            + ") c on a.idinstclassrank = c.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 817\n"
            + ") p on a.idinstclassrank = p.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 867\n"
            + ") l on a.idinstclassrank = l.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 806\n"
            + ") g on a.idinstclassrank = g.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 805\n"
            + ") h on a.idinstclassrank = h.idinstclassdomain join (\n"
            + "select p.idinstclassdomain, c.idclass, v.value \n"
            + "from instpredicate p \n"
            + "join instclass c on (p.idinstclassdomain = c.idinstclass)\n"
            + "join stringvalue v on (p.idscalarvalue = v.idscalarvalue)\n"
            + "where p.idpredicate = 722\n"
            + ") al on a.idinstclassdomain = al.idinstclassdomain join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 700\n"
            + ") alt on a.idinstclassdomain = alt.idinstclassdomain\n"
            + ") t group by t.lemma, t.langtype, t.\"document\", t.manuscript, t.volume, t.\"column\", t.line, t.page, t.chapter";

    private static final String SELECT_DOCUMENTED_IN_VARIANT = ""
            + "select t.variant, t.langtype, t.lemma, t.\"document\", t.manuscript, t.volume, t.\"column\", string_agg(t.paragraph, ', ') paragraph, t.line, t.page, t.chapter from (\n"
            + "select al.value variant, alt.value langtype, alm.value lemma, replace(replace(d.value, ')', ''), '(', '') \"document\", replace(replace(replace(m.value, ' [manuscrit]' , ''), ')', ''), '(', '') manuscript, v.value volume, c.value \"column\", p.value paragraph, l.value line, g.value page, h.value chapter\n"
            + "from (\n"
            + "select p.idinstclassdomain, p.idinstclassrank \n"
            + "from instpredicate p \n"
            + "where p.idpredicate = 783\n"
            + ") a join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 800\n"
            + ") d on a.idinstclassrank = d.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 854\n"
            + ") m on a.idinstclassrank = m.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 807\n"
            + ") v on a.idinstclassrank = v.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 809\n"
            + ") c on a.idinstclassrank = c.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 817\n"
            + ") p on a.idinstclassrank = p.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 867\n"
            + ") l on a.idinstclassrank = l.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 806\n"
            + ") g on a.idinstclassrank = g.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 805\n"
            + ") h on a.idinstclassrank = h.idinstclassdomain join (\n"
            + "select p.idinstclassdomain, c.idclass, v.value \n"
            + "from instpredicate p \n"
            + "join instclass c on (p.idinstclassdomain = c.idinstclass)\n"
            + "join stringvalue v on (p.idscalarvalue = v.idscalarvalue)\n"
            + "where p.idpredicate = 727\n"
            + ") al on a.idinstclassdomain = al.idinstclassdomain join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 706\n"
            + ") alt on a.idinstclassdomain = alt.idinstclassdomain join (\n"
            + "select p.idinstclassdomain, p.idinstclassrank, vr.value\n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassdomain and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 705\n"
            + ") alm on a.idinstclassdomain = alm.idinstclassrank\n"
            + ") t group by t.variant, t.langtype, t.lemma, t.\"document\", t.manuscript, t.volume, t.\"column\", t.line, t.page, t.chapter";

    private static final String SELECT_DOCUMENTED_IN_OTHER_LANGUAGE = ""
            + "select t.otherLanguage, t.langtype, t.\"document\", t.manuscript, t.volume, t.\"column\", string_agg(t.paragraph, ', ') paragraph, t.line, t.page, t.chapter from (\n"
            + "select al.value otherLanguage, alt.value langtype, replace(replace(d.value, ')', ''), '(', '') \"document\", replace(replace(replace(m.value, ' [manuscrit]' , ''), ')', ''), '(', '') manuscript, v.value volume, c.value \"column\", p.value paragraph, l.value line, g.value page, h.value chapter\n"
            + "from (\n"
            + "select p.idinstclassdomain, p.idinstclassrank \n"
            + "from instpredicate p \n"
            + "where p.idpredicate = 825\n"
            + ") a join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 800\n"
            + ") d on a.idinstclassrank = d.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 854\n"
            + ") m on a.idinstclassrank = m.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 807\n"
            + ") v on a.idinstclassrank = v.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 809\n"
            + ") c on a.idinstclassrank = c.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 817\n"
            + ") p on a.idinstclassrank = p.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 867\n"
            + ") l on a.idinstclassrank = l.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 806\n"
            + ") g on a.idinstclassrank = g.idinstclassdomain left join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 805\n"
            + ") h on a.idinstclassrank = h.idinstclassdomain join (\n"
            + "select p.idinstclassdomain, c.idclass, v.value \n"
            + "from instpredicate p \n"
            + "join instclass c on (p.idinstclassdomain = c.idinstclass)\n"
            + "join stringvalue v on (p.idscalarvalue = v.idscalarvalue)\n"
            + "where p.idpredicate = 793\n"
            + ") al on a.idinstclassdomain = al.idinstclassdomain join (\n"
            + "select p.idinstclassdomain, vr.value \n"
            + "from instpredicate p\n"
            + "join instpredicate pr on (pr.idinstclassdomain = p.idinstclassrank and pr.idpredicate = 13)\n"
            + "join stringvalue vr on (pr.idscalarvalue = vr.idscalarvalue)\n"
            + "where p.idpredicate = 794\n"
            + ") alt on a.idinstclassdomain = alt.idinstclassdomain\n"
            + ") t group by t.otherLanguage, t.langtype, t.\"document\", t.manuscript, t.volume, t.\"column\", t.line, t.page, t.chapter";

    private void importAttestations(Connection connection) throws SQLException {
        Map<String, Document> documents = ((List<Document>) HibernateUtil.getSession().createCriteria(Document.class).list()).stream().collect(Collectors.toMap(d -> d.getAbbreviation(), d -> d));
        Map<String, Manuscript> manuscripts = ((List<Manuscript>) HibernateUtil.getSession().createCriteria(Manuscript.class).list()).stream().collect(Collectors.toMap(m -> m.getSiglum(), m -> m));
        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(SELECT_DOCUMENTED_IN_LEMMA);
            while (result.next()) {
                insertAttestationForLemma(documents, manuscripts, result);
            }
        }
        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(SELECT_DOCUMENTED_IN_VARIANT);
            while (result.next()) {
                insertAttestationForVariant(documents, manuscripts, result);
            }
        }
        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(SELECT_DOCUMENTED_IN_OTHER_LANGUAGE);
            while (result.next()) {
                insertAttestationForOtherLanguage(documents, manuscripts, result);
            }
        }
        HibernateUtil.getSession().flush();
    }

    private void insertAttestationForLemma(Map<String, Document> documents, Map<String, Manuscript> manuscripts, ResultSet result) throws SQLException {
        InternalAttestation attestation = new InternalAttestation();
        attestation.setDocument(documents.get(result.getString("document")));
        attestation.setManuscript(manuscripts.get(checkDoubleManuscript(result.getString("manuscript"), attestation.getDocument().getAbbreviation())));
        if (attestation.getManuscript() == null) {
            LOG.warn("Manuscript missing: " + result.getString("manuscript"));
        }
        String chapter = result.getString("chapter");
        if (chapter != null && chapter.matches("[a-zA-Z]+\\d+")) {
            attestation.setListEntry(chapter);
        } else {
            attestation.setChapterNumber(chapter);
        }
        attestation.setLineNumber(result.getString("line"));
        attestation.setPageNumber(result.getString("page"));
        attestation.setParagraphNumber(result.getString("paragraph"));
        attestation.setAttestationUri(getAttestationURILemma(result.getString("lemma"), result.getString("langtype")));
        attestation.setAttestationUriLemma(getAttestationLemma(result.getString("lemma")));
        domainManager.insert(attestation);
    }

    private void insertAttestationForVariant(Map<String, Document> documents, Map<String, Manuscript> manuscripts, ResultSet result) throws SQLException {
        InternalAttestation attestation = new InternalAttestation();
        attestation.setDocument(documents.get(result.getString("document")));
        attestation.setManuscript(manuscripts.get(checkDoubleManuscript(result.getString("manuscript"), attestation.getDocument().getAbbreviation())));
        if (attestation.getManuscript() == null) {
            LOG.warn("Manuscript missing: " + result.getString("manuscript"));
        }
        String chapter = result.getString("chapter");
        if (chapter != null && chapter.matches("[a-zA-Z]+\\d+")) {
            attestation.setListEntry(chapter);
        } else {
            attestation.setChapterNumber(chapter);
        }
        attestation.setLineNumber(result.getString("line"));
        attestation.setPageNumber(result.getString("page"));
        attestation.setParagraphNumber(result.getString("paragraph"));
        attestation.setAttestationUri(getAttestationURIVariant(result.getString("lemma"), result.getString("langtype"), result.getString("variant")));
        attestation.setAttestationUriLemma(getAttestationLemma(result.getString("variant")));
        domainManager.insert(attestation);
    }

    private void insertAttestationForOtherLanguage(Map<String, Document> documents, Map<String, Manuscript> manuscripts, ResultSet result) throws SQLException {
        InternalAttestation attestation = new InternalAttestation();
        attestation.setDocument(documents.get(result.getString("document")));
        attestation.setManuscript(manuscripts.get(checkDoubleManuscript(result.getString("manuscript"), attestation.getDocument().getAbbreviation())));
        if (attestation.getManuscript() == null) {
            LOG.warn("Manuscript missing: " + result.getString("manuscript"));
        }
        String chapter = result.getString("chapter");
        if (chapter != null && chapter.matches("[a-zA-Z]+\\d+")) {
            attestation.setListEntry(chapter);
        } else {
            attestation.setChapterNumber(chapter);
        }
        attestation.setLineNumber(result.getString("line"));
        attestation.setPageNumber(result.getString("page"));
        attestation.setParagraphNumber(result.getString("paragraph"));
        attestation.setAttestationUri(getAttestationURIOtherLanguage(result.getString("otherLanguage"), result.getString("langtype")));
        attestation.setAttestationUriLemma(getAttestationLemma(result.getString("otherLanguage")));
        domainManager.insert(attestation);
    }

    private String checkDoubleManuscript(String siglum, String abbreviation) {
        if (siglum == null) {
            return null;
        } else if (siglum.equals("P")) {
            if (abbreviation.equals("ShS1")) {
                return "P(=Paris1)";
            } else if (abbreviation.equals("ShS2")) {
                return "P(=Paris2)";
            } else {
                return "P(=Princeton)";
            }
        } else if (siglum.equals("O")) {
            if (abbreviation.equals("ShS1")) {
                return "O(=Oxford1)";
            } else if (abbreviation.equals("ShS2")) {
                return "O(=Oxford2)";
            }
        } else if (siglum.equals("V")) {
            if (abbreviation.equals("ShS1")) {
                return "V(=Vatican1)";
            } else if (abbreviation.equals("ShS2")) {
                return "V(=Vatican2)";
            }
        }
        return siglum;
    }

    private static final String ATTESTATION_URI_LEMMA = "http://ditmao.ilc.cnr.it/ditmao#@encodedLemma_lemma";
    private static final String ATTESTATION_URI_VARIANT = "http://ditmao.ilc.cnr.it/ditmao#@encodedVariant_form";
    private static final String ATTESTATION_URI_OTHER_LANGUAGE = "http://ditmao.ilc.cnr.it/ditmao#@encodedLemma_lemma";

    private static final Map<String, String> LANG_TYPE = new HashMap<>();

    static {
        LANG_TYPE.put("a. cat.", "acat");
        LANG_TYPE.put("a. fr.", "afr");
        LANG_TYPE.put("a. occ.", "aoc");
        LANG_TYPE.put("a. occ.-arab.", "aocarab");
        LANG_TYPE.put("a. occ.-hébr.", "aocheb");
        LANG_TYPE.put("a. occ.-lat.", "aoclat");
        LANG_TYPE.put("a. occ./a. cat.", "aocacat");
        LANG_TYPE.put("ar.", "arab");
        LANG_TYPE.put("arab.", "arab");
        LANG_TYPE.put("aram.", "aram");
        LANG_TYPE.put("cat.", "cat");
        LANG_TYPE.put("fr.", "fr");
        LANG_TYPE.put("gr.", "gr");
        LANG_TYPE.put("gr./lat.", "grlat");
        LANG_TYPE.put("héb.", "heb");
        LANG_TYPE.put("héb.-arab.", "hebarab");
        LANG_TYPE.put("lat.", "lat");
        LANG_TYPE.put("lat. m. ", "latm");
        LANG_TYPE.put("lat./a.occ.", "lataoc");
        LANG_TYPE.put("m. fr.", "mfr");
        LANG_TYPE.put("angl.", "en");
        LANG_TYPE.put("incertain", "uncertain");
        LANG_TYPE.put(null, "nolang");
    }

    private String getAttestationURILemma(String lemma, String langType) {
        lemma = encodeLemma(purge(lemma), LANG_TYPE.get(langType));
        return ATTESTATION_URI_LEMMA.replaceFirst("@encodedLemma", lemma);
    }

    private String getAttestationURIVariant(String lemma, String langType, String variant) {
        variant = encodeLemma(purge(lemma), LANG_TYPE.get(langType)) + "_" + encodeLemma(purge(variant), null);
        return ATTESTATION_URI_VARIANT.replaceFirst("@encodedVariant", variant);
    }

    private String getAttestationURIOtherLanguage(String otherLanguage, String langType) {
        otherLanguage = purge(otherLanguage);
        otherLanguage = purgeOtherLanguage(otherLanguage, langType);
        langType = LANG_TYPE.get(langType);
        otherLanguage = encodeLemma(otherLanguage, langType);
        return ATTESTATION_URI_OTHER_LANGUAGE.replaceFirst("@encodedLemma", otherLanguage);
    }

    private static String purgeOtherLanguage(String otherLanguage, String langtype) {
        if (otherLanguage.startsWith(langtype)) {
            otherLanguage = otherLanguage.substring(langtype.length()).trim();
        }
        int index = otherLanguage.indexOf("-");
        if (index != -1) {
            otherLanguage = otherLanguage.substring(0, index).trim();
        }
        return otherLanguage;
    }

    private String getAttestationLemma(String lemma) {
        lemma = purge(lemma);
        if (lemma.startsWith("#")) {
            lemma = lemma.substring(1);
        }
        if (lemma.startsWith("*")) {
            lemma = lemma.substring(1);
        }
        return lemma;
    }

    private static String encodeLemma(String entry, String lang) {
        entry = entry.replaceAll("#", "");
        entry = entry.replaceAll("\\*", "");
        entry = entry.replaceAll(" ", "_");
        entry = entry.replaceAll("\\(", "");
        entry = entry.replaceAll("\\)", "");
        entry = entry.replaceAll("\\[", "");
        entry = entry.replaceAll("\\]", "");
        entry = entry.replaceAll("\\{", "");
        entry = entry.replaceAll("\\{", "");
        entry = entry.replaceAll("\\?", "");
        entry = entry.replaceAll("\\.", "");
        entry = entry.replaceAll("\\,", "");
        entry = entry.replaceAll("\\:", "");
        entry = entry.replaceAll("\\;", "");
        entry = entry.replaceAll("\\!", "");
        entry = entry.replaceAll("\"", "");
        entry = entry.replaceAll("\\'", "_");
        entry = entry.replaceAll("/", "_");
        return entry + (lang == null ? "" : ("_" + lang));
    }

    private static String purge(String value) {
        value = value.trim();
        value = value.replaceAll("\\*", " ");
        value = value.replaceAll("#", " ");
        value = value.replaceAll("­", " ");
        value = value.replaceAll("&", " ");
        value = value.replaceAll("<", " ");
        value = value.replaceAll(">", " ");
        value = value.replaceAll("", "");
        value = value.replaceAll(" ", " ");
        value = value.replaceAll("\\s+", " ");
        value = value.replaceAll(" (\\d)", "$1");
        value = value.replaceAll(" I$", "1");
        value = value.replaceAll(" II$", "2");
        value = value.replaceAll(" III$", "3");
        return value.trim();
    }

}
