/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.manager;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryBinding;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import it.cnr.ilc.lc.omegalex.controller.BaseController;
import it.cnr.ilc.lc.omegalex.controller.LexiconComparator;
import it.cnr.ilc.lc.omegalex.domain.ExternalAttestation;
import it.cnr.ilc.lc.omegalex.domain.InternalAttestation;
import it.cnr.ilc.lc.omegalex.manager.LemmaData.Word;
import it.cnr.ilc.lc.omegalex.manager.OntologyData.LinguisticReference;
import it.cnr.ilc.lc.omegalex.manager.SenseData.Openable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrea
 */
public class LexiconQuery extends BaseController {

    private static final Logger LOG = LogManager.getLogger(LexiconQuery.class);

    private OWLOntologyManager ontologyManager;
    private OWLOntology ontology;
    private OWLDataFactory owlDataFactory;

    private static int FIELD_MAX_LENGHT = 50;

    StructuralReasonerFactory reasonerFactory = null;
    OWLReasoner reasoner = null;

    public static final String NO_ENTRY_FOUND = "No entry specified";
    public static final String NO_TRANSLITERATION_FOUND = "none";

    // for query optimization
    public static Pattern pattern = Pattern.compile("([a-z]+_lemma)");

    // query prefixes
    public final static String QUERY_PREFIXES
            = "PREFIX lemon: <http://ditmao.ilc.cnr.it:8082/DitmaoOntologies/lemon.rdf#>\n"
            + "PREFIX lexinfo: <http://ditmao.ilc.cnr.it:8082/DitmaoOntologies/lexinfo.owl#>\n"
            + "PREFIX ditmaolemon: <http://ditmao.ilc.cnr.it/ditmaoLemon.owl#>\n"
            + "PREFIX lexicon: <http://ditmao.ilc.cnr.it/ditmao#>\n"
    + "PREFIX onto: <http://ditmao/ontology#>\n";
   
//    private final static String QUERY_PREFIXES
//            = "PREFIX lemon: <https://dl.dropboxusercontent.com/u/24160459/lemon_mod.rdf#>\n"
//            + "PREFIX lexinfo: <https://dl.dropboxusercontent.com/u/24160459/LexInfo_mod_2.owl#>\n"
//            + "PREFIX ditmaolemon: <http://ditmao.ilc.cnr.it/ditmaoLemon.owl#>\n"
//            + "PREFIX lexicon: <http://ditmao.ilc.cnr.it/ditmao#>\n";

    // queries for for getting properties values
    private final String GET_LEMMA_INFO = "SELECT ?li WHERE {"
            + " Type(?li, ditmaolemon:LemmaInfo) }";
    private final String GET_VARIANT_TYPES = "SELECT ?vt WHERE {"
            + " Type(?vt, ditmaolemon:Variant) }";
    private final String GET_PoS_CLASS = "SELECT ?posClass WHERE {"
            + " DirectSubClassOf(?posClass, lexinfo:PartOfSpeech) }";
    private final String GET_PoS = "SELECT ?pos WHERE {"
            + " DirectType(?pos, lexinfo:POS_CLASS) }";
    private final String GET_GENDER = "SELECT ?g WHERE {"
            + " Type(?g, lexinfo:Gender) }";
    private final String GET_NUMBER = "SELECT ?n WHERE {"
            + " Type(?n, lexinfo:Number) }";
    private final String GET_TRANSLITERATION_TYPES = "SELECT ?tt WHERE {"
            + " Type(?tt, ditmaolemon:TransliterationType) }";
    private final String GET_ALPHABETS = "SELECT ?a WHERE {"
            + " Type(?a, ditmaolemon:Alphabet) }";
    private final String GET_SCIENTIFIC_NAMES = "SELECT ?sn WHERE {"
            + " PropertyValue(?s, ditmaolemon:scientificName, ?sn) }";
    // query for getting lexicon values
    private final String GET_LEXICON_INSTANCE = "SELECT ?lexicon WHERE {"
            + " PropertyValue(?lexicon, lemon:language, \"_LANG_\") }";

    private final String GET_SENSES_BY_LANGUAGE = "SELECT ?sense WHERE {"
            + " Type(?l, lemon:Lexicon), "
            + " PropertyValue(?l, lemon:language, \"_LANG_\"),"
            + " PropertyValue(?l, lemon:entry, ?le),"
            + " PropertyValue(?le, lemon:sense, ?sense) }";
    private final String GET_LANGUAGES = "SELECT ?lang WHERE {"
            + " Type(?l, lemon:Lexicon),"
            + " PropertyValue(?l, lemon:language, ?lang) }";
    private final String GET_FORM_INSTANCES_OF_LEMMA = "SELECT ?of WHERE {"
            + " PropertyValue(?l, lemon:canonicalForm, lexicon:_LEMMA_),"
            + " PropertyValue(?l, lemon:otherForm, ?of) }";
    private final String GET_FORM_REPRESENTATION = "SELECT ?wr WHERE {"
            + " PropertyValue(lexicon:_FORM_, lemon:writtenRep, ?wr) }";
    private final String GET_FORM_POS = "SELECT ?pos WHERE {"
            + " PropertyValue(lexicon:_FORM_, lexinfo:partOfSpeech, ?pos) }";
    private final String GET_FORM_GENDER = "SELECT ?g WHERE {"
            + " PropertyValue(lexicon:_FORM_, lexinfo:gender, ?g) }";

    private final String GET_FORM_PERSON = "SELECT ?p WHERE {"
            + " PropertyValue(lexicon:_FORM_, lexinfo:person, ?p) }";
    private final String GET_FORM_MOOD = "SELECT ?m WHERE {"
            + " PropertyValue(lexicon:_FORM_, lexinfo:mood, ?m) }";
    private final String GET_FORM_VOICE = "SELECT ?v WHERE {"
            + " PropertyValue(lexicon:_FORM_, lexinfo:voice, ?v) }";

    private final String GET_FORM_NUMBER = "SELECT ?n WHERE {"
            + " PropertyValue(lexicon:_FORM_, lexinfo:number, ?n) }";
    private final String GET_FORM_ALPHABET = "SELECT ?a WHERE {"
            + " PropertyValue(lexicon:_FORM_, ditmaolemon:hasAlphabet, ?a) }";
    private final String GET_FORM_TRANSLITERATION = "SELECT ?t WHERE {"
            + " PropertyValue(lexicon:_FORM_, ditmaolemon:_RELTYPE_, ?t) }";
    private final String GET_FORM_TYPE = "SELECT ?t WHERE {"
            + " PropertyValue(lexicon:_FORM_, ditmaolemon:variant, ?t) }";

    private final String GET_FORM_DOCIN = "SELECT ?writtenRep WHERE {"
            + " PropertyValue(lexicon:_FORM_, ditmaolemon:isDocumentedIn, ?writtenRep) }";

    private final String GET_SENSES_OF_LEMMA = "SELECT ?s WHERE {"
            + " PropertyValue(?l, lemon:canonicalForm, lexicon:_LEMMA_),"
            + " PropertyValue(?l, lemon:sense, ?s) }";
    private final String GET_LEMMA_INSTANCE_OF_FORM = "SELECT ?cf WHERE {"
            + " PropertyValue(?l, lemon:otherForm, lexicon:_FORM_),"
            + " PropertyValue(?l, lemon:canonicalForm, ?cf) }";
    private final String GET_LEMMA_REPRESENTATION = "SELECT ?wr WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, lemon:writtenRep, ?wr) }";
//    private final String GET_LEMMA_LANG = "SELECT ?lang WHERE {"
//            + " PropertyValue(?le, lemon:canonicalForm, lexicon:_LEMMA_),"
//            + " PropertyValue(?l, lemon:entry, ?le),"
//            + " PropertyValue(?l, lemon:language, ?lang) }";

    private final String GET_ENTRY_VERIFIED = "SELECT ?v WHERE {"
            + " PropertyValue(lexicon:_ENTRY_, ditmaolemon:verified, ?v) }";

    private final String GET_ISCORR = "SELECT ?corr WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, ditmaolemon:isCorrespondence, ?corr) }";

    private final String GET_LEMMA_ALPHABET = "SELECT ?a WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, ditmaolemon:hasAlphabet, ?a) }";
    private final String GET_LEMMA_TRANSLITERATION = "SELECT ?t WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, ditmaolemon:_RELTYPE_, ?t) }";

    private final String GET_LEMMAINFO = "SELECT ?info WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, ditmaolemon:lemmaInfo, ?info) }";
    private final String GET_LEMMA_POS = "SELECT ?pos WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, lexinfo:partOfSpeech, ?pos) }";
    private final String GET_LEMMA_GENDER = "SELECT ?g WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, lexinfo:gender, ?g) }";

    private final String GET_LEMMA_PERSON = "SELECT ?p WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, lexinfo:person, ?p) }";
    private final String GET_LEMMA_VOICE = "SELECT ?v WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, lexinfo:voice, ?v) }";
    private final String GET_LEMMA_MOOD = "SELECT ?m WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, lexinfo:mood, ?m) }";

    private final String GET_LEMMA_NUMBER = "SELECT ?n WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, lexinfo:number, ?n) }";
    private final String GET_SENSES_OF_FORM = "SELECT ?s WHERE {"
            + " PropertyValue(?l, lemon:otherForm, lexicon:_FORM_),"
            //            + " PropertyValue(?of, lemon:writtenRep, lexicon:_FORM_),"
            + " PropertyValue(?l, lemon:sense, ?s) }";
    private final String GET_LEMMA_INSTANCE_OF_SENSE = "SELECT ?cf WHERE {"
            + " PropertyValue(?l, lemon:sense, lexicon:_SENSE_),"
            + " PropertyValue(?l, lemon:canonicalForm, ?cf) }";
    private final String GET_OTHER_INSTANCES_OF_SENSES = "SELECT ?s WHERE {"
            + " PropertyValue(?l, lemon:sense, lexicon:_SENSE_),"
            + " PropertyValue(?l, lemon:sense, ?s) }";
    private final String GET_SENSE_TRANSLATION = "SELECT ?tr WHERE {"
            + " PropertyValue(lexicon:_SENSE_, ditmaolemon:frenchTranslation, ?tr) }";
    private final String GET_SENSE_ENGLISH_TRANSLATION = "SELECT ?tr WHERE {"
            + " PropertyValue(lexicon:_SENSE_, ditmaolemon:englishTranslation, ?tr) }";
    private final String GET_SENSE_TRANSLATION_OF = "SELECT ?tr WHERE {"
            + " PropertyValue(lexicon:_SENSE_, ditmaolemon:frenchTranslationOf, ?tr) }";
    private final String GET_SENSE_ENGLISH_TRANSLATION_OF = "SELECT ?tr WHERE {"
            + " PropertyValue(lexicon:_SENSE_, ditmaolemon:englishTranslationOf, ?tr) }";

    private final String GET_SENSE_SYNONYM = "SELECT ?syn WHERE {"
            + " PropertyValue(lexicon:_SENSE_, lemon:equivalent, ?syn) }";
    private final String GET_SENSE_CORRESPONDENCE = "SELECT ?corr WHERE {"
            + " PropertyValue(lexicon:_SENSE_, ditmaolemon:correspondence, ?corr) }";
    private final String GET_SENSE_REFERENCE = "SELECT ?ref WHERE {"
            + " PropertyValue(lexicon:_SENSE_, lemon:reference, ?ref) }";

    // new queries
    private final String GET_LEMMA_BILINGUAL_BASIC = "SELECT ?writtenRep ?individual ?lang WHERE {"
            + " PropertyValue(?l, lemon:language, ?lang), "
            + " PropertyValue(?l, lemon:entry, ?le), "
            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
            + " PropertyValue(?individual, ditmaolemon:bilingualVariant, \"true\"), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep) }";

    private final String GET_LEMMA_BILINGUAL_BASIC_BY_LANGUAGE = "SELECT ?writtenRep ?individual ?lang WHERE {"
            + " PropertyValue(?l, lemon:language, \"_LANG_\"), "
            + " PropertyValue(?l, lemon:language, ?lang), "
            + " PropertyValue(?l, lemon:entry, ?le), "
            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
            + " PropertyValue(?individual, ditmaolemon:bilingualVariant, \"true\"), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep) }";

    private final String GET_LEMMA_BASIC = "SELECT DISTINCT ?writtenRep ?individual ?lang ?type ?verified ?lemmaInfo ?corr WHERE {"
//            + " PropertyValue(?l, lemon:language, ?lang), "
//            + " PropertyValue(?l, lemon:entry, ?le), "
//            + " DirectType(?le, ?type), "
//            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
//            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
//            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
//            + " PropertyValue(?individual, ditmaolemon:isCorrespondence, ?corr) }"
//            + " OR WHERE { "
            + " PropertyValue(?l, lemon:language, ?lang), "
            + " PropertyValue(?l, lemon:entry, ?le), "
            + " DirectType(?le, ?type), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
            + " PropertyValue(?individual, ditmaolemon:lemmaInfo, ?lemmaInfo), "
            + " PropertyValue(?individual, ditmaolemon:isCorrespondence, ?corr) }";

    private final String GET_CORR_BASIC = "SELECT ?writtenRep ?individual ?lang ?type ?verified ?corr WHERE {"
            + " PropertyValue(?l, lemon:language, ?lang), "
            + " PropertyValue(?l, lemon:entry, ?le), "
            + " DirectType(?le, ?type), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
            + " PropertyValue(?individual, ditmaolemon:isCorrespondence, ?corr) }";

    private final String GET_LEMMA_BASIC_BY_LANGUAGE = "SELECT DISTINCT ?writtenRep ?individual ?lang ?type ?verified ?lemmaInfo ?corr WHERE {"
//            + " PropertyValue(?l, lemon:language, \"_LANG_\"), "
//            + " PropertyValue(?l, lemon:language, ?lang), "
//            + " PropertyValue(?l, lemon:entry, ?le), "
//            + " DirectType(?le, ?type), "
//            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
//            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
//            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
//            + " PropertyValue(?individual, ditmaolemon:isCorrespondence, ?corr) }"
//            + " OR WHERE { "
            + " PropertyValue(?l, lemon:language, \"_LANG_\"), "
            + " PropertyValue(?l, lemon:language, ?lang), "
            + " PropertyValue(?l, lemon:entry, ?le), "
            + " DirectType(?le, ?type), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
            + " PropertyValue(?individual, ditmaolemon:lemmaInfo, ?lemmaInfo), "
            + " PropertyValue(?individual, ditmaolemon:isCorrespondence, ?corr) }";

    private final String GET_CORR_BASIC_BY_LANGUAGE = "SELECT ?writtenRep ?individual ?lang ?type ?verified ?corr WHERE {"
            + " PropertyValue(?l, lemon:language, \"_LANG_\"), "
            + " PropertyValue(?l, lemon:language, ?lang), "
            + " PropertyValue(?l, lemon:entry, ?le), "
            + " DirectType(?le, ?type), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
            + " PropertyValue(?individual, ditmaolemon:isCorrespondence, ?corr) }";

    private final String GET_FORM_BASIC = "SELECT ?writtenRep ?individual ?lang ?lemma ?verified WHERE {"
            + " PropertyValue(?l, lemon:language, ?lang), "
            + " PropertyValue(?l, lemon:entry, ?le), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " PropertyValue(?le, lemon:otherForm, ?individual), "
            + " PropertyValue(?le, lemon:canonicalForm, ?lemma), "
            + " PropertyValue(?individual, lemon:writtenRep, "
            + "?writtenRep) }";

    private final String GET_FORM_BASIC_BY_LANGUAGE = "SELECT ?writtenRep ?individual ?lang ?lemma ?verified WHERE {"
            + " PropertyValue(?l, lemon:language, \"_LANG_\"), "
            + " PropertyValue(?l, lemon:language, ?lang), "
            + " PropertyValue(?l, lemon:entry, ?le), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " PropertyValue(?le, lemon:otherForm, ?individual), "
            + " PropertyValue(?le, lemon:canonicalForm, ?lemma), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep) }";

    private final String GET_SENSE_BASIC = "SELECT ?writtenRep ?lang ?verified WHERE {"
            + " PropertyValue(?l, lemon:language, ?lang), "
            + " PropertyValue(?l, lemon:entry, ?le), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " PropertyValue(?le, lemon:sense, ?writtenRep) }";

    private final String GET_SENSE_BASIC_BY_LANGUAGE = "SELECT ?writtenRep ?lang ?verified WHERE {"
            + " PropertyValue(?l, lemon:language, \"_LANG_\"), "
            + " PropertyValue(?l, lemon:language, ?lang), "
            + " PropertyValue(?l, lemon:entry, ?le), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " PropertyValue(?le, lemon:sense, ?writtenRep) }";

    private final String GET_LEMMA_TYPE = "SELECT ?t WHERE {"
            + " PropertyValue(?le, lemon:canonicalForm, lexicon:_LEMMA_), "
            + " DirectType(?le, ?t) }";

    private final String GET_LEMMA_NOTE = "SELECT ?note WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, ditmaolemon:note, ?note) } ";

    private final String GET_LEMMA_ISBILINGUAL = "SELECT ?b WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, ditmaolemon:bilingualVariant, ?b) }";

    private final String GET_LEXICAL_RELATION_WORD = "SELECT ?individual WHERE {"
            + " PropertyValue(?l, lemon:language, \"_LANG_\"), "
            + " PropertyValue(?l, lemon:entry, ?le), "
            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
            + " PropertyValue(?individual, lemon:writtenRep, \"_LEMMA_\") }";

    private final String GET_LEMMA_OF_LINGUISTIC_VARIANT = "SELECT ?individual ?lang ?writtenRep WHERE {"
            + " PropertyValue(lexicon:_FORM_, ditmaolemon:seeAOc, ?individual), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
            + " PropertyValue(?l, lemon:entry, ?le), "
            + " PropertyValue(?l, lemon:language, ?lang) }";

    private final String GET_LEMMA_SUBLEMMA = "SELECT ?individual ?lang ?writtenRep WHERE { "
            + "PropertyValue(?_le, lemon:canonicalForm, lexicon:_LEMMA_),   "
            + "PropertyValue(?_le, ditmaolemon:hasSublemma, ?le),   "
            + "PropertyValue(?le, lemon:canonicalForm, ?individual),  "
            //            + "PropertyValue(?l, lemon:entry, ?le),   "
            //            + "PropertyValue(?l, lemon:language, ?lang),   "
            //            + "PropertyValue(?component, lemon:canonicalForm, ?individual),  "
            + "PropertyValue(?individual, lemon:writtenRep, ?writtenRep) }";

    private final String GET_LEMMA_COLLOCATION = "SELECT ?individual ?lang ?writtenRep WHERE { "
            + "PropertyValue(?_le, lemon:canonicalForm, lexicon:_LEMMA_),   "
            + "PropertyValue(?_le, ditmaolemon:hasCollocation, ?le),   "
            + "PropertyValue(?le, lemon:canonicalForm, ?individual),  "
            //            + "PropertyValue(?l, lemon:entry, ?le),   "
            //            + "PropertyValue(?l, lemon:language, ?lang),   "
            //            + "PropertyValue(?component, lemon:canonicalForm, ?individual),  "
            + "PropertyValue(?individual, lemon:writtenRep, ?writtenRep) }";

    private final String GET_LEMMA_REFERENCE = "SELECT ?individual ?lang ?writtenRep WHERE { "
            + "PropertyValue(?_le, lemon:canonicalForm, lexicon:_LEMMA_),   "
            + "PropertyValue(?_le, ditmaolemon:seeAlso, ?le),   "
            + "PropertyValue(?le, lemon:canonicalForm, ?individual),  "
            //            + "PropertyValue(?l, lemon:entry, ?le),   "
            //            + "PropertyValue(?l, lemon:language, ?lang),   "
            //            + "PropertyValue(?component, lemon:canonicalForm, ?individual),  "
            + "PropertyValue(?individual, lemon:writtenRep, ?writtenRep) }";
    
        public static final String LING_REF_BY_CONCEPT = "SELECT ?s ?wr WHERE {"
            + " PropertyValue(?s, lemon:reference, onto:_CLAZZ_), "
            + " PropertyValue(?le, lemon:sense, ?s), "
            + " PropertyValue(?le, lemon:canonicalForm, ?cf), "
            + " PropertyValue(?cf, lemon:writtenRep, ?wr)  }";
        
            public static final String SENSE_DEFINITION = "SELECT ?def WHERE {"
            + " PropertyValue(lexicon:_SENSE_, ditmaolemon:scientificName, ?def) }";

    private final String GET_LEMMA_BILINGUALS = "SELECT ?individual ?lang ?writtenRep WHERE { "
            + "PropertyValue(lexicon:_LEMMA_, ditmaolemon:hasBilingualVariant, ?individual),   "
            + "PropertyValue(?individual, lemon:writtenRep, ?writtenRep) }";

    private final String GET_LEMMA_DOCIN = "SELECT ?writtenRep WHERE { "
            + "PropertyValue(lexicon:_LEMMA_, ditmaolemon:isDocumentedIn, ?writtenRep) }";

    // writtenRep contains the string "type@description" where type can be Dictionary or Other text and description is a string
    private final String GET_LEMMA_BIBLIO = "SELECT ?writtenRep WHERE { "
            + "PropertyValue(lexicon:_LEMMA_, ditmaolemon:alsoDocumentedIn, ?writtenRep) }";

    private static final String GET_CONSTITUENTS = "SELECT ?constituent ?position WHERE { "
            + "PropertyValue(lexicon:_ENTRY_, ditmaolemon:constituent, ?constituent), "
            + "PropertyValue(?constituent, ditmaolemon:hasPosition, ?position) }";

    private static final String GET_CONSTITUENT_AT_POSITION = "SELECT ?constituent WHERE { "
            + "PropertyValue(lexicon:_ENTRY_, ditmaolemon:constituent, ?constituent), "
            + "PropertyValue(?constituent, ditmaolemon:hasPosition, \"_POSITION_\") }";

    private static final String GET_WORDS_OF_MULTIWORD = "SELECT ?individual ?lang ?writtenRep WHERE { "
            + "PropertyValue(lexicon:_CONST_, ditmaolemon:correspondsTo, ?le), "
            + "PropertyValue(?l, lemon:language, ?lang), "
            + "PropertyValue(?l, lemon:entry, ?le), "
            + "PropertyValue(?le, lemon:canonicalForm, ?individual), "
            + "PropertyValue(?individual, lemon:writtenRep, ?writtenRep) }";

    private final String GET_SENSE_NOTE = "SELECT ?note WHERE {"
            + " PropertyValue(lexicon:_SENSE_, ditmaolemon:note, ?note) } ";

    private final String GET_SENSE_SCIENTIFICNAME = "SELECT ?sn WHERE {"
            + " PropertyValue(lexicon:_SENSE_, ditmaolemon:scientificName, ?sn) } ";

    private final String GET_FORM_NOTE = "SELECT ?note WHERE {"
            + " PropertyValue(lexicon:_FORM_, ditmaolemon:note, ?note) } ";

    private final String GET_LEMMA_USEDIN = "SELECT ?u WHERE {"
            + " PropertyValue(lexicon:_LEMMA_, ditmaolemon:usedIn, ?u) }";

    private final String GET_FORM_USEDIN = "SELECT ?u WHERE {"
            + " PropertyValue(lexicon:_FORM_, ditmaolemon:usedIn, ?u) }";

    private String ADVANCED_FILTER_LEMMA = "SELECT ?le ?individual ?writtenRep ?sense ?verified ?type ?sn ?pos ?a WHERE {"
            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
            + " DirectType(?le, ?type), "
            + " PropertyValue(?le, lemon:sense, ?sense) }"
            + " OR WHERE { "
            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " PropertyValue(?individual, lexinfo:partOfSpeech, ?pos), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
            + " DirectType(?le, ?type), "
            + " PropertyValue(?le, lemon:sense, ?sense) }"
            + " OR WHERE { "
            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " PropertyValue(?individual, ditmaolemon:hasAlphabet, ?a), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
            + " DirectType(?le, ?type), "
            + " PropertyValue(?le, lemon:sense, ?sense) }"
            + " OR WHERE { "
            + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
            + " DirectType(?le, ?type), "
            + " PropertyValue(?le, lemon:sense, ?sense), "
            + " PropertyValue(?sense, ditmaolemon:scientificName, ?sn) }";

    private String ADVANCED_FILTER_FORM = "SELECT ?le ?individual ?writtenRep ?verified ?type ?pos ?a ?sn WHERE {"
            + " PropertyValue(?le, lemon:otherForm, ?individual), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " DirectType(?le, ?type), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
            + " PropertyValue(?le, lemon:sense, ?sense) }"
            + " OR WHERE { "
            + " PropertyValue(?le, lemon:otherForm, ?individual), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " DirectType(?le, ?type), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
            + " PropertyValue(?le, lemon:sense, ?sense), "
            + " PropertyValue(?sense, ditmaolemon:scientificName, ?sn) }"
            + " OR WHERE { "
            + " PropertyValue(?le, lemon:otherForm, ?individual), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " DirectType(?le, ?type), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
            + " PropertyValue(?individual, lexinfo:partOfSpeech, ?pos), "
            + " PropertyValue(?le, lemon:sense, ?sense) }"
            + " OR WHERE { "
            + " PropertyValue(?le, lemon:otherForm, ?individual), "
            + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
            + " DirectType(?le, ?type), "
            + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
            + " PropertyValue(?individual, ditmaolemon:hasAlphabet, ?a), "
            + " PropertyValue(?le, lemon:sense, ?sense) }";

    public LexiconQuery() {
    }

    public LexiconQuery(LexiconModel lm) {
        ontologyManager = lm.getManager();
        ontology = lm.getOntology();
        owlDataFactory = lm.getFactory();
        reasonerFactory = new StructuralReasonerFactory();
//        reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
        reasoner = reasonerFactory.createReasoner(ontology);

    }

    public OWLOntologyManager getManager() {
        return ontologyManager;
    }

    public void setManager(OWLOntologyManager manager) {
        this.ontologyManager = manager;
    }

    public OWLOntology getOntology() {
        return ontology;
    }

    public void setOntology(OWLOntology ontology) {
        this.ontology = ontology;
    }

    public OWLDataFactory getFactory() {
        return owlDataFactory;
    }

    public void setFactory(OWLDataFactory factory) {
        this.owlDataFactory = factory;
    }

    private ArrayList<String> getList(List<Map<String, String>> l) {
        ArrayList<String> al = new ArrayList();
        for (Map<String, String> m : l) {
            for (Map.Entry<String, String> entry : m.entrySet()) {
                al.add(entry.getValue());
            }
        }
        if (al.isEmpty()) {
            al.add(NO_ENTRY_FOUND);
        }
        return al;
    }

    public ArrayList<String> getLemmaInfo() {
        return getList(processQuery(QUERY_PREFIXES + GET_LEMMA_INFO));
    }

    public ArrayList<String> getVariantTypes() {
        return getList(processQuery(QUERY_PREFIXES + GET_VARIANT_TYPES));
    }

    public ArrayList<String> getTranslitertionType() {
        return getList(processQuery(QUERY_PREFIXES + GET_TRANSLITERATION_TYPES));
    }

    public ArrayList<String> getAlphabets() {
        return getList(processQuery(QUERY_PREFIXES + GET_ALPHABETS));
    }

    public List<SelectItem> getPoS() {
        List<SelectItem> groupedPosList = new ArrayList<>();
        ArrayList<String> posClasses = getList(processQuery(QUERY_PREFIXES + GET_PoS_CLASS));
        posClasses.sort(String::compareToIgnoreCase);
        for (String posClass : posClasses) {
            groupedPosList.add(getGroupedPoS(posClass));
        }
        return groupedPosList;
    }

    private SelectItemGroup getGroupedPoS(String posClass) {
        SelectItemGroup g = new SelectItemGroup(posClass);
        ArrayList<String> PoS = getList(processQuery(QUERY_PREFIXES + GET_PoS.replace("POS_CLASS", posClass)));
        PoS.sort(String::compareToIgnoreCase);
        SelectItem[] selPoS = new SelectItem[PoS.size()];
        for (int i = 0; i < PoS.size(); i++) {
            selPoS[i] = new SelectItem(PoS.get(i), PoS.get(i));
        }
        g.setSelectItems(selPoS);
        return g;
    }

    public ArrayList<String> getGenders() {
        return getList(processQuery(QUERY_PREFIXES + GET_GENDER));
    }

    public ArrayList<String> getNumbers() {
        return getList(processQuery(QUERY_PREFIXES + GET_NUMBER));
    }

    public ArrayList<String> getScientificNames() {
        return getList(processQuery(QUERY_PREFIXES + GET_SCIENTIFIC_NAMES));
    }

    public List<Map<String, String>> getBilingualLemmas(String lang) {
        if (lang.equals("All languages")) {
            return processQuery(QUERY_PREFIXES + GET_LEMMA_BILINGUAL_BASIC);
        } else {
            return processQuery(QUERY_PREFIXES + GET_LEMMA_BILINGUAL_BASIC_BY_LANGUAGE.replace("_LANG_", lang));
        }
    }

    public List<Map<String, String>> getLemmas(String lang) {
        if (lang.equals("All languages")) {
//             if (lang.equals("All languages") || lang.equals("corr")) {
            return getFilteredLanguages(processQuery(QUERY_PREFIXES + GET_LEMMA_BASIC));
        } else {
            return processQuery(QUERY_PREFIXES + GET_LEMMA_BASIC_BY_LANGUAGE.replace("_LANG_", lang));
        }
    }

    public List<Map<String, String>> getCorrs(String lang) {
        if (lang.equals("All languages")) {
            return getFilteredLanguages(processQuery(QUERY_PREFIXES + GET_CORR_BASIC));
        } else {
            return processQuery(QUERY_PREFIXES + GET_CORR_BASIC_BY_LANGUAGE.replace("_LANG_", lang));
        }
    }

    public List<Map<String, String>> getForms(String lang) {
        if (lang.equals("All languages")) {
            return getFilteredLanguages(processQuery(QUERY_PREFIXES + GET_FORM_BASIC));
        } else {
            return processQuery(QUERY_PREFIXES + GET_FORM_BASIC_BY_LANGUAGE.replace("_LANG_", lang));
        }
    }

    public List<Map<String, String>> getSenses(String lang) {
        if (lang.equals("All languages")) {
            return getFilteredLanguages(processQuery(QUERY_PREFIXES + GET_SENSE_BASIC));
        } else {
            return processQuery(QUERY_PREFIXES + GET_SENSE_BASIC_BY_LANGUAGE.replace("_LANG_", lang));
        }
    }

    // filter our @en and @fr (that are "tranlsation languages")
    private List<Map<String, String>> getFilteredLanguages(List<Map<String, String>> l) {
        List<Map<String, String>> _l = new ArrayList<>();
        for (Map<String, String> m : l) {
            if ((!m.get("lang").equals("fr")) && (!m.get("lang").equals("en"))) {
                _l.add(m);
            }
        }
        return _l;
    }

    public String getLexicon(String lang) {
        List<Map<String, String>> lexicon = processQuery(QUERY_PREFIXES + GET_LEXICON_INSTANCE.replace("_LANG_", lang));
        if (!lexicon.isEmpty()) {
            return lexicon.get(0).get("lexicon");
        } else {
            return NO_ENTRY_FOUND;
        }
    }

    public List<Map<String, String>> advancedFilter_lemmas() {
        return processQuery(QUERY_PREFIXES + ADVANCED_FILTER_LEMMA);
    }

    public List<Map<String, String>> advancedFilter_forms() {
        return processQuery(QUERY_PREFIXES + ADVANCED_FILTER_FORM);
    }

    public List<SelectItem> getSensesByLanguage(String lang) {
//        if (lang == null) {
//            return processQuery(QUERY_PREFIXES + GET_SENSES);
//        } else {
//            return processQuery(QUERY_PREFIXES + GET_SENSES_BY_LANGUAGE.replace("_LANG_", lang));
//        }
        List<SelectItem> groupedSenseList = new ArrayList<>();
        ArrayList<String> senses = getList(processQuery(QUERY_PREFIXES + GET_SENSES_BY_LANGUAGE.replace("_LANG_", lang)));
        senses.sort(String::compareToIgnoreCase);
        SelectItemGroup g = new SelectItemGroup(lang);
        SelectItem[] selSenses = new SelectItem[senses.size()];
        for (int i = 0; i < senses.size(); i++) {
            selSenses[i] = new SelectItem(senses.get(i), senses.get(i));
        }
        g.setSelectItems(selSenses);
        groupedSenseList.add(g);
        return groupedSenseList;
    }

    public ArrayList<String> getLexicaLanguages() {
        return getList(processQuery(QUERY_PREFIXES + GET_LANGUAGES));
    }

    // invoked in order to get lemma attributes of a specific lemma
    public LemmaData getLemmaAttributes(String lemma, DocumentationManager documentationManager) {
        LemmaData ld = new LemmaData();
//        ArrayList<String> results = getList(processQuery(QUERY_PREFIXES + GET_LEMMA_INSTANCE.replace("_LEMMA_", lemma)));
//        String _lemma = results.get(0);
        setLemmaData(lemma, ld);
        setLemmaAttestation(ld, documentationManager);
        return ld;
    }

    // invoked in order to get a lemma attributes of a specific sense
    public LemmaData getLemmaOfSense(String sense, DocumentationManager documentationManager) {
        LemmaData ld = new LemmaData();
        ArrayList<String> results = getList(processQuery(QUERY_PREFIXES + GET_LEMMA_INSTANCE_OF_SENSE.replace("_SENSE_", sense)));
        String lemma = results.get(0);
        setLemmaData(lemma, ld);
        setLemmaAttestation(ld, documentationManager);
        return ld;
    }

    // invoked in order to get a lemma attributes of a specific form
    public LemmaData getLemmaEntry(String form, DocumentationManager documentationManager) {
        LemmaData ld = new LemmaData();
        ArrayList<String> results = getList(processQuery(QUERY_PREFIXES + GET_LEMMA_INSTANCE_OF_FORM.replace("_FORM_", form)));
        String lemma = results.get(0);
        setLemmaData(lemma, ld);
        setLemmaAttestation(ld, documentationManager);
        return ld;
    }

    public LemmaData getLemmaOfSense(String sense) {
        LemmaData ld = new LemmaData();
        ArrayList<String> results = getList(processQuery(QUERY_PREFIXES + GET_LEMMA_INSTANCE_OF_SENSE.replace("_SENSE_", sense)));
        String lemma = results.get(0);
        setLemmaData(lemma, ld);
        return ld;
    }

    private void setLemmaAttestation(LemmaData ld, DocumentationManager documentationManager) {
        ld.setExternalAttestation(getExternalAttestationList("http://ditmao.ilc.cnr.it/ditmao#" + ld.getIndividual(), documentationManager));
        ld.setInternalAttestation(getInternalAttestationList("http://ditmao.ilc.cnr.it/ditmao#" + ld.getIndividual(), documentationManager));
    }

    private ArrayList<ExternalAttestationData> getExternalAttestationList(String le, DocumentationManager documentationManager) {
        ArrayList<ExternalAttestationData> _eadl = new ArrayList();
        for (ExternalAttestation ea : documentationManager.getExternalAttestationByLexicalEntry(le)) {
            ExternalAttestationData _ead = new ExternalAttestationData();
            _ead.setId(ea.getId());
            _ead.setOther(ea.getOther());
            _ead.setSec(ea.getSec());
            _ead.setArticleNumber(ea.getArticleNumber());
            _ead.setAttestationUri(ea.getAttestationUri());
            _ead.setAttestationUriLemma(ea.getAttestationUriLemma());
            _ead.setBook(ea.getBook());
            _ead.setChapterNumberRoman(ea.getChapterNumberRoman());
            _ead.setColumnNumber(ea.getColumnNumber());
            _ead.setEntryNumber(ea.getEntryNumber());
            _ead.setFascicle(ea.getFascicle());
            _ead.setFolioNumber(ea.getFolioNumber());
            _ead.setFootnoteNumber(ea.getFootnoteNumber());
            _ead.setGlossaryNumber(ea.getFootnoteNumber());
            _ead.setLineNumber(ea.getLineNumber());
            _ead.setNumberOfGeographicPoint(ea.getNumberOfGeographicPoint());
            _ead.setNumberOfMap(ea.getNumberOfMap());
            _ead.setForm(ea.getForm());
            _ead.setPageNumber(ea.getPageNumber());
            _ead.setParagraphNumber(ea.getParagraphNumber());
            _ead.setPart(ea.getPart());
            _ead.setSubvolume(ea.getSubvolume());
            _ead.setSvSublemma(ea.getSvSublemma());
            _ead.setVolume(ea.getVolume());
            _ead.setAbbreviationId(ea.getDocument().getAbbreviation());
            _ead.setUrl(ea.getUrl());
            _ead.setDocType(ea.getDocument().getType());
            _eadl.add(_ead);
        }
        return _eadl;
    }

    private ArrayList<InternalAttestationData> getInternalAttestationList(String le, DocumentationManager documentationManager) {
        ArrayList<InternalAttestationData> _iadl = new ArrayList();
        for (InternalAttestation ia : documentationManager.getInternalAttestationByLexicalEntry(le)) {
            InternalAttestationData _iad = new InternalAttestationData();
            _iad.setAttestationUri(ia.getAttestationUri());
            _iad.setAttestationUriLemma(ia.getAttestationUriLemma());
            _iad.setChapterNumber(ia.getChapterNumber());
            _iad.setDocAbbreviation(ia.getDocument().getAbbreviation());
            _iad.setDocType(ia.getDocument().getType());
            _iad.setId(ia.getId());
            _iad.setLineNumber(ia.getLineNumber());
//            _iad.setListEntryLetter(ia.getListEntryLetter());
//            _iad.setListEntryNumber(ia.getListEntryNumber());
            _iad.setListEntry(ia.getListEntry());
            _iad.setManSigla(ia.getManuscript() == null ? null : ia.getManuscript().getSiglum());
            _iad.setPageNumber(ia.getPageNumber());
            _iad.setParagraphNumber(ia.getParagraphNumber());
            _iadl.add(_iad);
        }
        return _iadl;
    }

    private void setLemmaData(String lemma, LemmaData ld) {

        long startTime = System.currentTimeMillis();

        ld.setIndividual(lemma);
        // It is the type of the entry of the lemma
        ld.setType(getLemmaType(lemma));
        ld.setFormWrittenRepr(getLemmaWrittenRep(lemma));

//        long endTime = System.currentTimeMillis();
//        System.out.println("DURATA LEMMA TYPE: " + (endTime - startTime));
//        ld.setLanguage(getLemmaLanguage(lemma));
        Matcher matcher = pattern.matcher(lemma);
        if (matcher.find()) {
            ld.setLanguage(matcher.group(1).split("_lemma")[0]);
        } else {
            ld.setLanguage("");
        }

        ld.setVerified(getEntryVerified(lemma).equals("true") ? true : false);

        ld.setCorrespondence(isCorrespondance(lemma));

//        startTime = System.currentTimeMillis();
//        System.out.println("DURATA LEMMA LANG: " + (startTime - endTime));
        ld.setPoS(getLemmaPoS(lemma));

//        endTime = System.currentTimeMillis();
//        System.out.println("DURATA LEMMA POS: " + (endTime - startTime));
        ld.setInfo(getLemmaInfo(lemma));

//        startTime = System.currentTimeMillis();
//        System.out.println("DURATA LEMMA INFO: " + (startTime - endTime));
        ld.setGender(getLemmaGender(lemma));

        ld.setPerson(getLemmaPerson(lemma));
        ld.setMood(getLemmaMood(lemma));
        ld.setVoice(getLemmaVoice(lemma));

//        endTime = System.currentTimeMillis();
//        System.out.println("DURATA LEMMA GENDER: " + (endTime - startTime));
        ld.setNumber(getLemmaNumber(lemma));

//        startTime = System.currentTimeMillis();
//        System.out.println("DURATA LEMMA NUMBER: " + (startTime - endTime));
        ld.setNote(getLemmaNote(lemma));

//        endTime = System.currentTimeMillis();
//        System.out.println("DURATA LEMMA NOTE: " + (endTime - startTime));
        ld.setSeeAlso(getLemmaReference(lemma));

//        startTime = System.currentTimeMillis();
//        System.out.println("DURATA LEMMA REF: " + (startTime - endTime));
        ld.setUsedIn(getLemmaUsedIn(lemma).replace("ditmao_", ""));

//        endTime = System.currentTimeMillis();
//        System.out.println("DURATA LEMMA USEDIN: " + (endTime - startTime));
        ld.setAlphabet(getLemmaAlphabet(lemma));

//        startTime = System.currentTimeMillis();
//        System.out.println("DURATA LEMMA ALPHABET: " + (startTime - endTime));
        ld.setDocumentedIn(getLemmaDocIn(lemma));

//        endTime = System.currentTimeMillis();
//        System.out.println("DURATA LEMMA DOCIN: " + (endTime - startTime));
        setTransliteration(ld, lemma);

//        startTime = System.currentTimeMillis();
//        System.out.println("DURATA LEMMA TRANSLITT: " + (startTime - endTime));
        ld.setReference(getLemmaBiblio(lemma));

//        endTime = System.currentTimeMillis();
//        System.out.println("DURATA LEMMA BIBLIO: " + (endTime - startTime));
        if (!ld.getType().equals("Word")) {
            ld.setMultiword(getLemmaMultiword(lemma, ld.getFormWrittenRepr()));
            ld.setBilingualVariant(getBilingualType(lemma));
            ld.setBilingual(getBilinguals(lemma));
//            startTime = System.currentTimeMillis();
//            System.out.println("DURATA LEMMA MULTI: " + (startTime - endTime));
        } else {
            ld.setSublemma(getLemmaSublemma(lemma));
            ld.setCollocation(getLemmaCollocation(lemma));
//            startTime = System.currentTimeMillis();
//            System.out.println("DURATA LEMMA WORD: " + (startTime - endTime));
        }
        ld.setLinguisticType(getFormType(lemma, "linguistic"));
        ld.setLinguisticTypeEntry(getLemmaOfLinguisticVariant(lemma, ld.isLinguisticType()));
//        endTime = System.currentTimeMillis();
//        System.out.println("DURATA QUERY LEMMA: " + (endTime - startTime));
//        log(org.apache.log4j.Level.INFO, null, "DURATA QUERY LEMMA: " + (endTime - startTime));
    }

    private void setTransliteration(LemmaData ld, String lemma) {
        String transliteration = getLemmaTransliteration(lemma, "hebrewTransliteration");
        if (!transliteration.equals(NO_ENTRY_FOUND) && (!transliteration.isEmpty())) {
            ld.setTransliteration(transliteration);
            ld.setTransilterationType("hebrew");
        } else {
            transliteration = getLemmaTransliteration(lemma, "arabicTransliteration");
            if (!transliteration.equals(NO_ENTRY_FOUND) && (!transliteration.isEmpty())) {
                ld.setTransliteration(transliteration);
                ld.setTransilterationType("arabic");
            } else {
                ld.setTransliteration("");
                ld.setTransilterationType(NO_TRANSLITERATION_FOUND);
            }
        }
    }

    private boolean getBilingualType(String lemma) {
        String isBilingual = getEntryAttribute(QUERY_PREFIXES + GET_LEMMA_ISBILINGUAL, "_LEMMA_", lemma);
        if (isBilingual.contains(NO_ENTRY_FOUND)) {
            return false;
        } else {
            return true;
        }
    }

    private ArrayList<Word> getBilinguals(String lemma) {
        return getEntryAttributeWordList(QUERY_PREFIXES + GET_LEMMA_BILINGUALS, "_LEMMA_", lemma);
    }

    private String getLemmaNote(String lemma) {
        String note = getEntryAttribute(QUERY_PREFIXES + GET_LEMMA_NOTE, "_LEMMA_", lemma);
        return note.equals(NO_ENTRY_FOUND) ? "" : note;
    }

    private ArrayList<Word> getLemmaSublemma(String lemma) {
        return getEntryAttributeWordList(QUERY_PREFIXES + GET_LEMMA_SUBLEMMA, "_LEMMA_", lemma);
    }

    private ArrayList<Word> getLemmaCollocation(String lemma) {
        return getEntryAttributeWordList(QUERY_PREFIXES + GET_LEMMA_COLLOCATION, "_LEMMA_", lemma);
    }

    private ArrayList<Word> getLemmaReference(String lemma) {
        return getEntryAttributeWordList(QUERY_PREFIXES + GET_LEMMA_REFERENCE, "_LEMMA_", lemma);
    }

    private ArrayList<Word> getLemmaDocIn(String lemma) {
        return getEntryAttributeWordList(QUERY_PREFIXES + GET_LEMMA_DOCIN, "_LEMMA_", lemma);
    }

    private ArrayList<ReferenceData> getLemmaBiblio(String lemma) {
        return getEntryAttributeReferenceDataList(QUERY_PREFIXES + GET_LEMMA_BIBLIO, "_LEMMA_", lemma);
    }

    // invoked in order to retrieve the data of the lemma involved in sublemma or collocation relation
    public Word getLemma(String lemma, String lang) {
        ArrayList<String> word = getList(processQuery(QUERY_PREFIXES + GET_LEXICAL_RELATION_WORD.replace("_LANG_", lang).replace("_LEMMA_", lemma)));
        Word w = new Word();
        w.setOWLName(word.get(0));
        w.setLanguage(lang);
        w.setWrittenRep(lemma);
        return w;
    }

    // invoked in order to retrieve the lemma related to the linguistic variant
    public Word getLemmaOfLinguisticVariant(String form, boolean linguistic) {
        Word w = new Word();
        if (linguistic) {
            List<Map<String, String>> word = processQuery(QUERY_PREFIXES + GET_LEMMA_OF_LINGUISTIC_VARIANT.replace("_FORM_", form));
            for (Map<String, String> m : word) {
                w.setOWLName(m.get("individual"));
                w.setLanguage(m.get("lang"));
                w.setWrittenRep(m.get("writtenRep"));
            }

        }
        return w;
    }

    private String getLemmaType(String lemma) {
        return getEntryAttribute(QUERY_PREFIXES + GET_LEMMA_TYPE, "_LEMMA_", lemma);
    }

    public String getComponentAtPosition(String entry, String position) {
        String constituentAtPositionQuery = QUERY_PREFIXES + GET_CONSTITUENT_AT_POSITION.replace("_ENTRY_", entry).replace("_POSITION_", position);
        List<Map<String, String>> comp = processQuery(constituentAtPositionQuery);
        return comp.get(0).get("constituent");
    }

    private ArrayList<Word> getLemmaMultiword(String lemma, String wr) {
        ArrayList<Word> alw = new ArrayList();
        String entry = lemma.replace("_lemma", "_entry");
        String constituentsQuery = QUERY_PREFIXES + GET_CONSTITUENTS.replace("_ENTRY_", entry);
        List<Map<String, String>> constituents = processQuery(constituentsQuery);
        Collections.sort(constituents, new LexiconComparator("position"));
        for (Map<String, String> m : constituents) {
            String wordQuery = QUERY_PREFIXES + GET_WORDS_OF_MULTIWORD.replace("_CONST_", m.get("constituent"));
            List<Map<String, String>> word = processQuery(wordQuery);
            alw.add(getWord(word, m.get("constituent"), m.get("position"), wr));
        }
        return alw;
    }

    private Word getWord(List<Map<String, String>> word, String OWLComponentIndividual, String position, String wr) {
        Word _w = new Word();
        if (word.isEmpty()) {
            // the word does not exist as lexical entry
            _w.setViewButtonDisabled(true);
            _w.setWrittenRep(wr.split(" ")[Integer.parseInt(position)] + " not found");
            _w.setOWLComp(OWLComponentIndividual);
            return _w;
        } else {
            // the word exists as lexical entry
            for (Map<String, String> w : word) {
                _w.setWrittenRep(w.get("writtenRep"));
                _w.setOWLName(w.get("individual"));
                _w.setLanguage(w.get("lang"));
                _w.setOWLComp(OWLComponentIndividual);
                _w.setLabel(_w.getWrittenRep() + "@" + _w.getLanguage());
            }
        }
        return _w;
    }

    private ArrayList<Word> getEntryAttributeWordList(String q, String t, String e) {
        ArrayList<Word> alw = new ArrayList();
        List<Map<String, String>> wl = processQuery(q.replace(t, e));
        for (Map<String, String> m : wl) {
            alw.add(getWord(m));
        }
        return alw;
    }

    private ArrayList<ReferenceData> getEntryAttributeReferenceDataList(String q, String t, String e) {
        ArrayList<ReferenceData> rfd = new ArrayList();
        List<Map<String, String>> rfdl = processQuery(q.replace(t, e));
        for (Map<String, String> m : rfdl) {
            rfd.add(getReferenceData(m));
        }
        return rfd;
    }

    private ReferenceData getReferenceData(Map<String, String> m) {
        ReferenceData rfd = new ReferenceData();
        String doc = m.get("writtenRep");
        rfd.setType(doc.split("@")[0]);
        rfd.setLocation(doc.split("@")[1]);
        return rfd;
    }

    private Word getWord(Map<String, String> m) {
        Word w = new Word();
        w.setWrittenRep(m.get("writtenRep"));
        w.setOWLName(m.get("individual"));
        if (m.get("lang") != null) {
            w.setLanguage(m.get("lang"));
        } else {
            Matcher matcher = pattern.matcher((m.get("individual") != null) ? m.get("individual") : "");
            if (matcher.find()) {
                w.setLanguage(matcher.group(1).split("_lemma")[0]);
            } else {
                w.setLanguage(m.get("lang"));
            }
        }
        return w;
    }

    private void setFormAttestation(FormData fd, DocumentationManager documentationManager) {
        fd.setInternalAttestation(getInternalAttestationList("http://ditmao.ilc.cnr.it/ditmao#" + fd.getIndividual(), documentationManager));
    }

    public ArrayList<FormData> getFormsOfLemma(String lemma, String lang, DocumentationManager documentationManager) {
        ArrayList<FormData> fdList = new ArrayList<>();
        ArrayList<String> results = getList(processQuery(QUERY_PREFIXES + GET_FORM_INSTANCES_OF_LEMMA.replace("_LEMMA_", lemma)));
        Collections.sort(results);
        for (String form : results) {
            if (!results.get(0).equals(NO_ENTRY_FOUND)) {
                FormData fd = new FormData();
                setFormData(form, lang, fd);
                setFormAttestation(fd, documentationManager);
                fdList.add(fd);
            }
        }
        return fdList;
    }

    private void setFormData(String form, String lang, FormData fd) {
        long startTime = System.currentTimeMillis();
        fd.setIndividual(form);
        fd.setLanguage(lang);
        fd.setFormWrittenRepr(getFormWrittenRep(form));
        fd.setPoS(getFormPoS(form));
        fd.setGender(getFormGender(form));
        fd.setPerson(getFormPerson(form));
        fd.setMood(getFormMood(form));
        fd.setVoice(getFormVoice(form));
        fd.setNumber(getFormNumber(form));
        fd.setAlphabet(getFormAlphabet(form));
        fd.setAlphabeticalType(getFormType(form, "alphabetical"));
        fd.setGraphicType(getFormType(form, "graphic"));
        fd.setMorphologicalType(getFormType(form, "morphological"));
        fd.setGraphophoneticType(getFormType(form, "graphophonetic"));
//        fd.setLinguisticType(getFormType(form, "linguistic"));
        fd.setNote(getFormNote(form));
        fd.setUsedIn(getFormUsedIn(form).replace("ditmao_", ""));
        fd.setUnspecifiedType(getFormType(form, "unspecified"));
        fd.setMorphosyntacticType(getFormType(form, "morphosyntactic"));
        fd.setDocumentedIn(getFormDocIn(form));
//        fd.setLinguisticTypeEntry(getLemmaOfLinguisticVariant(form, fd.isLinguisticType()));
        setTransliteration(fd, form);
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        LOG.info("DURATA QUERY FORMA: " + duration);
        log(org.apache.log4j.Level.INFO, null, "DURATA QUERY FORMA: " + duration);
    }

    private String getFormNote(String form) {
        String note = getEntryAttribute(QUERY_PREFIXES + GET_FORM_NOTE, "_FORM_", form);
        return note.equals(NO_ENTRY_FOUND) ? "" : note;
    }

    private String getFormUsedIn(String form) {
        return getEntryAttribute(QUERY_PREFIXES + GET_FORM_USEDIN, "_FORM_", form);
    }

    private void setTransliteration(FormData fd, String form) {
        String transliteration = getFormTransliteration(form, "hebrewTransliteration");
        if (!transliteration.equals(NO_ENTRY_FOUND) && (!transliteration.isEmpty())) {
            fd.setTransliteration(transliteration);
            fd.setTransilterationType("hebrew");
        } else {
            transliteration = getFormTransliteration(form, "arabicTransliteration");
            if (!transliteration.equals(NO_ENTRY_FOUND) && (!transliteration.isEmpty())) {
                fd.setTransliteration(transliteration);
                fd.setTransilterationType("arabic");
            } else {
                fd.setTransliteration("");
                fd.setTransilterationType(NO_TRANSLITERATION_FOUND);
            }
        }
    }

    public ArrayList<SenseData> getSensesOfLemma(String lemma) {
        ArrayList<SenseData> sdList = new ArrayList<>();
        ArrayList<String> results = getList(processQuery(QUERY_PREFIXES + GET_SENSES_OF_LEMMA.replace("_LEMMA_", lemma)));
        Collections.sort(results);
        for (String sense : results) {
            if (!results.get(0).equals(NO_ENTRY_FOUND)) {
                SenseData sd = new SenseData();
                senseData(sense, sd);
                sdList.add(sd);
            }
        }
        return sdList;
    }

    public ArrayList<SenseData> getSensesOfForm(String form) {
        ArrayList<SenseData> sdList = new ArrayList<>();
        ArrayList<String> results = getList(processQuery(QUERY_PREFIXES + GET_SENSES_OF_FORM.replace("_FORM_", form)));
        Collections.sort(results);
        for (String sense : results) {
            if (!results.get(0).equals(NO_ENTRY_FOUND)) {
                SenseData sd = new SenseData();
                senseData(sense, sd);
                sdList.add(sd);
            }
        }
        return sdList;
    }

    private String getSenseNote(String sense) {
        String note = getEntryAttribute(QUERY_PREFIXES + GET_SENSE_NOTE, "_SENSE_", sense);
        return note.equals(NO_ENTRY_FOUND) ? "" : note;
    }

//    private String getScientificName(String sense) {
//        String sn = getEntryAttribute(QUERY_PREFIXES + GET_SENSE_SCIENTIFICNAME, "_SENSE_", sense);
//        return sn;
//    }
    public ArrayList<Openable> getScientificName(String sense) {
        ArrayList<Openable> sdoList = new ArrayList();
        ArrayList<String> sns = getEntryAttributeList(QUERY_PREFIXES + GET_SENSE_SCIENTIFICNAME, "_SENSE_", sense);
        for (String sn : sns) {
            SenseData.Openable sdo = new SenseData.Openable();
            if (!sn.equals(NO_ENTRY_FOUND)) {
                sdo.setName(sn);
                sdoList.add(sdo);
            }
        }
        return sdoList;
    }

    private void senseData(String sense, SenseData sd) {
        long startTime = System.currentTimeMillis();
        sd.setName(sense);
        sd.setNote(getSenseNote(sense));
        sd.setScientificName(getScientificName(sense));
        sd.setTranslation(getTranslation(sense));
        sd.setEnglishTranslation(getEnglishTranslation(sense));
        sd.setTranslationOf(getTranslationOf(sense));
        sd.setEnglishTranslationOf(getEnglishTranslationOf(sense));
        sd.setSynonym(getSynonym(sense));
        sd.setCorrespondence(getCorrespondence(sense));
        sd.setOWLClass(getOntoClass(sense));
        setFieldMaxLenght(sd.getScientificName(), sd);
        setFieldMaxLenght(sd.getSynonym(), sd);
        setFieldMaxLenght(sd.getCorrespondence(), sd);
        setFieldMaxLenght(sd.getEnglishTranslation(), sd);
        setFieldMaxLenght(sd.getEnglishTranslationOf(), sd);
        setFieldMaxLenght(sd.getTranslation(), sd);
        setFieldMaxLenght(sd.getTranslationOf(), sd);
        setFieldMaxLenght(sd.getOWLClass(), sd);
//        sd.setFiledMaxLenght(sd.getFiledMaxLenght() - 5);
        sd.setFiledMaxLenght((sd.getFiledMaxLenght() > FIELD_MAX_LENGHT) ? FIELD_MAX_LENGHT : sd.getFiledMaxLenght());
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        LOG.info("DURATA QUERY SENSO: " + duration);
        log(org.apache.log4j.Level.INFO, null, "DURATA QUERY SENSO: " + duration);
    }

    private void setFieldMaxLenght(List<Openable> lo, SenseData sd) {
        for (Openable op : lo) {
            if (op.getName().length() > sd.getFiledMaxLenght()) {
                sd.setFiledMaxLenght(op.getName().length());
            }
        }
    }

    private void setFieldMaxLenght(Openable op, SenseData sd) {
        if (op.getName().length() > sd.getFiledMaxLenght()) {
            sd.setFiledMaxLenght(op.getName().length());
        }
    }

    public ArrayList<SenseData> getOtherSenses(String sense) {
        ArrayList<SenseData> sdList = new ArrayList<>();
        ArrayList<String> results = getList(processQuery(QUERY_PREFIXES + GET_OTHER_INSTANCES_OF_SENSES.replace("_SENSE_", sense)));
        Collections.sort(results);
        for (String s : results) {
            if (!results.get(0).equals(NO_ENTRY_FOUND)) {
                SenseData sd = new SenseData();
                senseData(s, sd);
                sdList.add(sd);
            }
        }
        return sdList;
    }

    public List<Map<String, String>> ontologyClassList() {
        List<Map<String, String>> ontologyClassList = new ArrayList<>();
        ontologyClassList.add(getMapOntoClass("L_UNIVERS"));
        ontologyClassList.add(getMapOntoClass("LE_CIEL"));
        ontologyClassList.add(getMapOntoClass("LA_TERRE"));
        ontologyClassList.add(getMapOntoClass("LES_PLANTES"));
        ontologyClassList.add(getMapOntoClass("LES_ANIMAUX"));
        ontologyClassList.add(getMapOntoClass("L_HOMME"));
        ontologyClassList.add(getMapOntoClass("L_ÊTRE_PHISIQUE"));
        ontologyClassList.add(getMapOntoClass("LE_CORPS_ET_SES_PARTIES"));
        ontologyClassList.add(getMapOntoClass("LE_SQUELETTE_HUMAIN"));
        ontologyClassList.add(getMapOntoClass("L_ORGANISME_HUMAIN_ET_SES_FONCTIONS"));
        ontologyClassList.add(getMapOntoClass("LA_SANTÉ_ET_LES_MALADIES"));
        ontologyClassList.add(getMapOntoClass("SANTÉ"));
        ontologyClassList.add(getMapOntoClass("MALADIE"));
        ontologyClassList.add(getMapOntoClass("LA_VIE_HUMAINE"));
        ontologyClassList.add(getMapOntoClass("L_ÂME"));
        ontologyClassList.add(getMapOntoClass("L_ÊTRE_SOCIAL"));
        ontologyClassList.add(getMapOntoClass("L_ORGANISATION_SOCIALE"));
        ontologyClassList.add(getMapOntoClass("L_HOMME_ET_L_UNIVERS"));
        ontologyClassList.add(getMapOntoClass("L_A_PRIORI"));
        ontologyClassList.add(getMapOntoClass("LA_SCIENCE_ET_LA_TECHNIQUE"));
        return ontologyClassList;
    }

    private Map<String, String> getMapOntoClass(String clazz) {
        Map<String, String> myMap = new HashMap<String, String>();
        myMap.put("writtenRep", clazz);
        return myMap;
    }

    private String getLemmaWrittenRep(String lemma) {
        return getEntryAttribute(QUERY_PREFIXES + GET_LEMMA_REPRESENTATION, "_LEMMA_", lemma);
    }

    private String getLemmaPoS(String lemma) {
        return getEntryAttribute(QUERY_PREFIXES + GET_LEMMA_POS, "_LEMMA_", lemma);
    }

    private String getEntryVerified(String lemma) {
        return getEntryAttribute(QUERY_PREFIXES + GET_ENTRY_VERIFIED, "_ENTRY_", lemma.replace("_lemma", "_entry"));
    }

    private String isCorrespondance(String lemma) {
        return getEntryAttribute(QUERY_PREFIXES + GET_ISCORR, "_LEMMA_", lemma);
    }

    private String getLemmaAlphabet(String lemma) {
        return getEntryAttribute(QUERY_PREFIXES + GET_LEMMA_ALPHABET, "_LEMMA_", lemma);
    }

    private String getLemmaTransliteration(String lemma, String transliterationType) {
        String translit = getEntryAttribute(QUERY_PREFIXES + GET_LEMMA_TRANSLITERATION.replace("_RELTYPE_", transliterationType), "_LEMMA_", lemma);
        return translit.equals(NO_ENTRY_FOUND) ? "" : translit;
    }

    private String getFormWrittenRep(String form) {
        return getEntryAttribute(QUERY_PREFIXES + GET_FORM_REPRESENTATION, "_FORM_", form);
    }

    private String getFormPoS(String form) {
        return getEntryAttribute(QUERY_PREFIXES + GET_FORM_POS, "_FORM_", form);
    }

    private String getFormGender(String form) {
        return getEntryAttribute(QUERY_PREFIXES + GET_FORM_GENDER, "_FORM_", form);
    }

    private String getFormPerson(String form) {
        return getEntryAttribute(QUERY_PREFIXES + GET_FORM_PERSON, "_FORM_", form);
    }

    private String getFormMood(String form) {
        return getEntryAttribute(QUERY_PREFIXES + GET_FORM_MOOD, "_FORM_", form);
    }

    private String getFormVoice(String form) {
        return getEntryAttribute(QUERY_PREFIXES + GET_FORM_VOICE, "_FORM_", form);
    }

    private String getFormNumber(String form) {
        return getEntryAttribute(QUERY_PREFIXES + GET_FORM_NUMBER, "_FORM_", form);
    }

    private String getFormAlphabet(String form) {
        return getEntryAttribute(QUERY_PREFIXES + GET_FORM_ALPHABET, "_FORM_", form);
    }

    private String getFormTransliteration(String form, String transliterationType) {
        return getEntryAttribute(QUERY_PREFIXES + GET_FORM_TRANSLITERATION.replace("_RELTYPE_", transliterationType), "_FORM_", form);
    }

    private boolean getFormType(String form, String t) {
        ArrayList<String> types = getEntryAttributeList(QUERY_PREFIXES + GET_FORM_TYPE, "_FORM_", form);
        if (types.contains(t + "Variant")) {
            return true;
        } else {
            return false;
        }
    }

    private ArrayList<Word> getFormDocIn(String form) {
        return getEntryAttributeWordList(QUERY_PREFIXES + GET_FORM_DOCIN, "_FORM_", form);
    }

    private ArrayList<Openable> vecchia_getSynonym(String sense) {
        ArrayList<Openable> sdoList = new ArrayList();
        ArrayList<String> syns = getEntryAttributeList(QUERY_PREFIXES + GET_SENSE_SYNONYM, "_SENSE_", sense);
        for (String syn : syns) {
            SenseData.Openable sdo = new SenseData.Openable();
            sdo.setName(syn);
            if (syn.equals(NO_ENTRY_FOUND)) {
                sdo.setViewButtonDisabled(true);
                sdo.setDeleteButtonDisabled(true);
            }
            sdoList.add(sdo);
        }
        return sdoList;
    }

    public ArrayList<Openable> getSynonym(String sense) {
        ArrayList<Openable> sdoList = new ArrayList();
        ArrayList<String> syns = getEntryAttributeList(QUERY_PREFIXES + GET_SENSE_SYNONYM, "_SENSE_", sense);
        for (String syn : syns) {
            SenseData.Openable sdo = new SenseData.Openable();
            if (!syn.equals(NO_ENTRY_FOUND)) {
                sdo.setName(syn);
                sdo.setViewButtonDisabled(false);
                sdo.setDeleteButtonDisabled(false);
                sdoList.add(sdo);
            }
        }
        return sdoList;
    }

    public ArrayList<Openable> getCorrespondence(String sense) {
        ArrayList<Openable> sdoList = new ArrayList();
        ArrayList<String> corrs = getEntryAttributeList(QUERY_PREFIXES + GET_SENSE_CORRESPONDENCE, "_SENSE_", sense);
        for (String corr : corrs) {
            SenseData.Openable sdo = new SenseData.Openable();
            if (!corr.equals(NO_ENTRY_FOUND)) {
                sdo.setName(corr);
                sdo.setViewButtonDisabled(false);
                sdo.setDeleteButtonDisabled(false);
                sdoList.add(sdo);
            }
        }
        return sdoList;
    }

    public ArrayList<Openable> getTranslation(String sense) {
        ArrayList<Openable> sdoList = new ArrayList();
        ArrayList<String> trans = getEntryAttributeList(QUERY_PREFIXES + GET_SENSE_TRANSLATION, "_SENSE_", sense);
        for (String tr : trans) {
            SenseData.Openable sdo = new SenseData.Openable();
            if (!tr.equals(NO_ENTRY_FOUND)) {
                sdo.setName(tr);
                sdo.setViewButtonDisabled(false);
                sdo.setDeleteButtonDisabled(false);
                sdoList.add(sdo);
            }
        }
        return sdoList;
    }

    public ArrayList<Openable> getTranslationOf(String sense) {
        ArrayList<Openable> sdoList = new ArrayList();
        ArrayList<String> trans = getEntryAttributeList(QUERY_PREFIXES + GET_SENSE_TRANSLATION_OF, "_SENSE_", sense);
        for (String tr : trans) {
            SenseData.Openable sdo = new SenseData.Openable();
            if (!tr.equals(NO_ENTRY_FOUND)) {
                sdo.setName(tr);
                sdo.setViewButtonDisabled(false);
                sdo.setDeleteButtonDisabled(false);
                sdoList.add(sdo);
            }
        }
        return sdoList;
    }

    public ArrayList<Openable> getEnglishTranslation(String sense) {
        ArrayList<Openable> sdoList = new ArrayList();
        ArrayList<String> trans = getEntryAttributeList(QUERY_PREFIXES + GET_SENSE_ENGLISH_TRANSLATION, "_SENSE_", sense);
        for (String tr : trans) {
            SenseData.Openable sdo = new SenseData.Openable();
            if (!tr.equals(NO_ENTRY_FOUND)) {
                sdo.setName(tr);
                sdo.setViewButtonDisabled(false);
                sdo.setDeleteButtonDisabled(false);
                sdoList.add(sdo);
            }
        }
        return sdoList;
    }

    public ArrayList<Openable> getEnglishTranslationOf(String sense) {
        ArrayList<Openable> sdoList = new ArrayList();
        ArrayList<String> trans = getEntryAttributeList(QUERY_PREFIXES + GET_SENSE_ENGLISH_TRANSLATION_OF, "_SENSE_", sense);
        for (String tr : trans) {
            SenseData.Openable sdo = new SenseData.Openable();
            if (!tr.equals(NO_ENTRY_FOUND)) {
                sdo.setName(tr);
                sdo.setViewButtonDisabled(false);
                sdo.setDeleteButtonDisabled(false);
                sdoList.add(sdo);
            }
        }
        return sdoList;
    }

    private Openable getOntoClass(String sense) {
        Openable ref = new Openable();
        String ontoClass = getEntryAttribute(QUERY_PREFIXES + GET_SENSE_REFERENCE, "_SENSE_", sense);
        ref.setName(ontoClass.equals(NO_ENTRY_FOUND) ? "" : ontoClass);
        ref.setViewButtonDisabled(!ontoClass.equals(NO_ENTRY_FOUND));
        return ref;
    }

    private ArrayList<Openable> vecchia_getTranslation(String sense) {
        ArrayList<Openable> sdoList = new ArrayList();
        ArrayList<String> trans = getEntryAttributeList(QUERY_PREFIXES + GET_SENSE_TRANSLATION, "_SENSE_", sense);
        for (String tr : trans) {
            SenseData.Openable sdo = new SenseData.Openable();
            sdo.setName(tr);
            if (tr.equals(NO_ENTRY_FOUND)) {
                sdo.setViewButtonDisabled(true);
                sdo.setDeleteButtonDisabled(true);
            }
            sdoList.add(sdo);
        }
        return sdoList;
    }

    public String getLemmaInfo(String lemma) {
        return getEntryAttribute(QUERY_PREFIXES + GET_LEMMAINFO, "_LEMMA_", lemma);
    }

    private String getLemmaGender(String lemma) {
        return getEntryAttribute(QUERY_PREFIXES + GET_LEMMA_GENDER, "_LEMMA_", lemma);
    }

    private String getLemmaPerson(String lemma) {
        return getEntryAttribute(QUERY_PREFIXES + GET_LEMMA_PERSON, "_LEMMA_", lemma);
    }

    private String getLemmaMood(String lemma) {
        return getEntryAttribute(QUERY_PREFIXES + GET_LEMMA_MOOD, "_LEMMA_", lemma);
    }

    private String getLemmaVoice(String lemma) {
        return getEntryAttribute(QUERY_PREFIXES + GET_LEMMA_VOICE, "_LEMMA_", lemma);
    }

    private String getLemmaNumber(String lemma) {
        return getEntryAttribute(QUERY_PREFIXES + GET_LEMMA_NUMBER, "_LEMMA_", lemma);
    }

    private String getLemmaUsedIn(String lemma) {
        return getEntryAttribute(QUERY_PREFIXES + GET_LEMMA_USEDIN, "_LEMMA_", lemma);
    }

    private String getEntryAttribute(String q, String t, String e) {
        ArrayList<String> r = getList(processQuery(q.replace(t, e)));
        return r.get(0);
    }

    private ArrayList<String> getEntryAttributeList(String q, String t, String e) {
        return getList(processQuery(q.replace(t, e)));
    }

    public List<Map<String, String>> processQuery(String q) {
        QueryResult result = null;
//        reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_ASSERTIONS);
//        reasoner.flush();
        QueryEngine engine = QueryEngine.create(ontologyManager, reasoner);
        Query query;
        try {
            query = Query.create(q);
            result = engine.execute(query);

        } catch (QueryEngineException ex) {
            LOG.fatal(ex);
        } catch (QueryParserException ex2) {
            LOG.error("Error parsing {0}", ex2);
        }
//        reasoner.dispose();
//        InferredPropertyAssertionGenerator generator = new InferredPropertyAssertionGenerator();
//        Set<OWLPropertyAssertionAxiom<?, ?>> axioms = generator.createAxioms(owlDataFactory, reasoner);
        return getQueryResults(result);
    }

    private ArrayList<String> vecchia_getQueryResults(QueryResult qr) {
        ArrayList<String> resultsList = new ArrayList<>();
        if (qr != null) {
            Iterator<QueryBinding> itr = qr.iterator();
            if (itr.hasNext()) {
                while (itr.hasNext()) {
                    QueryBinding qb = itr.next();
                    Set<QueryArgument> keys = qb.getBoundArgs();
                    for (QueryArgument key : keys) {
                        if (key.isVar()) {
                            String name = key.getValueAsVar().getName();
                        }
                        switch (qb.get(key).getType().name()) {
                            case "LITERAL":
                                resultsList.add(qb.get(key).getValueAsLiteral().getLiteral());
                                break;
                            case "URI":
                                resultsList.add(qb.get(key).getValueAsIRI().getShortForm());
                                break;
                            default:
                        }
                    }
                }
            } else {
                resultsList.add(NO_ENTRY_FOUND);
            }
        } else {
            resultsList.add(NO_ENTRY_FOUND);
        }
        return resultsList;
    }

    private List<Map<String, String>> getQueryResults(QueryResult qr) {

        List<Map<String, String>> resultsList = new ArrayList<>();
        if (qr != null) {
            Iterator<QueryBinding> itr = qr.iterator();
            if (itr.hasNext()) {
                while (itr.hasNext()) {
                    QueryBinding qb = itr.next();
                    Set<QueryArgument> keys = qb.getBoundArgs();
                    Map<String, String> map = new HashMap<>();
                    for (QueryArgument key : keys) {
                        if (key.isVar()) {
                            String name = key.getValueAsVar().getName();
                            if (name.equals("lemmaInfo")) {
                                int i = 0;
                            }
                        }
                        switch (qb.get(key).getType().name()) {
                            case "LITERAL":
                                map.put(key.getValueAsVar().getName(), qb.get(key).getValueAsLiteral().getLiteral());
                                break;
                            case "URI":
                                map.put(key.getValueAsVar().getName(), qb.get(key).getValueAsIRI().getShortForm());
                                if (qb.get(key).getValueAsIRI().getShortForm().contains("aranhons")) {
                                    int i = 0;
                                }
                                break;
                            default:
                        }
                    }
                    resultsList.add(map);
                }
            } else {
//                resultsList.add(NO_ENTRY_FOUND);
            }
        } else {
//            resultsList.add(NO_ENTRY_FOUND);
        }
        return resultsList;
    }

    // TEST MAIN PER CONSUMO MEMORIA
    public static void _main(String[] args) throws QueryEngineException {

        String LEMMAS = "SELECT ?pos WHERE {"
                + " PropertyValue(?cf, lexinfo:PartOfSpeech, ?pos) }";

        try {
            File file = new File("/home/andrea/.LexO/ditmao.owl");

            LexiconQuery lq = new LexiconQuery();

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
            OWLDataFactory factory = manager.getOWLDataFactory();
            lq.setManager(manager);
            lq.setFactory(factory);
            lq.setOntology(ontology);
            StructuralReasonerFactory _factory = new StructuralReasonerFactory();
            OWLReasoner reasoner = _factory.createReasoner(ontology);
            reasoner.flush();
            int i = 0;
            while (i < 1) {
                LOG.info(i + " Max: " + Runtime.getRuntime().maxMemory() / (1024 * 1024)
                        + " Tot: " + Runtime.getRuntime().totalMemory() / (1024 * 1024));
                i++;
                reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_ASSERTIONS);
                QueryEngine engine = QueryEngine.create(manager, reasoner);
                Query query;
                try {
                    query = Query.create(LexiconQuery.QUERY_PREFIXES + LEMMAS);
                    QueryResult result = engine.execute(query);
                    LOG.info(result.toString() + result.size());
                    LOG.info(LexiconQuery.QUERY_PREFIXES + LEMMAS);
//                    getRes(result);

                } catch (QueryParserException ex) {
                    LOG.fatal(ex);
                }
                factory.purge();
                reasoner.dispose();

            }
        } catch (OWLOntologyCreationException ex) {
            LOG.fatal(ex);
        }

    }

    public static void main(String[] args) throws QueryEngineException {
        String ADVANCED_FILTER_LEMMA = "SELECT ?le ?individual ?writtenRep ?sense ?verified ?type ?sn ?pos ?a ?lemmaInfo WHERE {"
                + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
                + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
                + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
                + " DirectType(?le, ?type), "
                + " PropertyValue(?le, lemon:sense, ?sense) }"
                + " OR WHERE { "
                + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
                + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
                + " PropertyValue(?individual, lexinfo:partOfSpeech, ?pos), "
                + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
                + " DirectType(?le, ?type), "
                + " PropertyValue(?le, lemon:sense, ?sense) }"
                + " OR WHERE { "
                + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
                + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
                + " PropertyValue(?individual, ditmaolemon:hasAlphabet, ?a), "
                + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
                + " DirectType(?le, ?type), "
                + " PropertyValue(?le, lemon:sense, ?sense) }"
                + " OR WHERE { "
                + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
                + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
                + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
                + " DirectType(?le, ?type), "
                + " PropertyValue(?le, lemon:sense, ?sense), "
                + " PropertyValue(?sense, ditmaolemon:scientificName, ?sn) }"
                + " OR WHERE { "
                + " PropertyValue(?le, lemon:canonicalForm, ?individual), "
                + " PropertyValue(?le, ditmaolemon:verified, ?verified), "
                + " PropertyValue(?individual, lemon:writtenRep, ?writtenRep), "
                + " PropertyValue(?individual, ditmaolemon:lemmaInfo, ?lemmaInfo), "
                + " DirectType(?le, ?type), "
                + " PropertyValue(?le, lemon:sense, ?sense) }";

        try {
            File file = new File("/home/andrea/.LexO/ditmao.owl");

            LexiconQuery lq = new LexiconQuery();

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
            OWLDataFactory factory = manager.getOWLDataFactory();
            lq.setManager(manager);
            lq.setFactory(factory);
            lq.setOntology(ontology);
            StructuralReasonerFactory _factory = new StructuralReasonerFactory();
            OWLReasoner reasoner = _factory.createReasoner(ontology);
            QueryEngine engine = QueryEngine.create(manager, reasoner);
            Query query;
            try {
                long startTime = System.currentTimeMillis();
                query = Query.create(LexiconQuery.QUERY_PREFIXES + ADVANCED_FILTER_LEMMA);
                QueryResult result = engine.execute(query);
                long endTime = System.currentTimeMillis();
                System.out.println("DURATA QUERY LEMMA: " + (float) (endTime - startTime) / (float) 1000 + " seconds.");
                System.out.println(result.size());
//                System.out.println("result: " + result.toString());
//                System.out.println(LexiconQuery.QUERY_PREFIXES + LEMMAS);
                getRes(result);

            } catch (QueryParserException ex) {
                LOG.fatal(ex);
            }
            factory.purge();

        } catch (OWLOntologyCreationException ex) {
            LOG.fatal(ex);
        }

    }

    private static void getRes(QueryResult qr) {
        Iterator<QueryBinding> itr = qr.iterator();
        if (itr.hasNext()) {
            while (itr.hasNext()) {
                QueryBinding qb = itr.next();
                Set<QueryArgument> keys = qb.getBoundArgs();
                for (QueryArgument key : keys) {
                    if (key.isVar()) {
                        String name = key.getValueAsVar().getName();
                        System.out.println("*** " + name);
                    }
                    switch (qb.get(key).getType().name()) {
                        case "LITERAL":
                            System.out.println("LITE " + qb.get(key).getValueAsLiteral().getLiteral());
                            break;
                        case "URI":
                            System.out.println("URI  " + qb.get(key).getValueAsIRI().getShortForm());
                            break;
                        case "VAR":
                            System.out.println("NAME " + key.getValueAsVar().getName());
                            break;
                        default:
                    }
                }
            }
        }
    }
    
    public ArrayList<LinguisticReference> getReferencingByOntology(String clazz, LinguisticReference.ReferenceType type) {
        ArrayList<LinguisticReference> allr = new ArrayList<>();
        getReferencingByClass(clazz, allr, type);
        return allr;
    }
    
    private void getReferencingByClass(String clazz, ArrayList<LinguisticReference> allr, LinguisticReference.ReferenceType type) {
        List<Map<String, String>> results = processQuery(LexiconQuery.QUERY_PREFIXES + LexiconQuery.LING_REF_BY_CONCEPT.replace("_CLAZZ_", clazz));
        for (Map<String, String> m : results) {
            LinguisticReference lr = new LinguisticReference();
            lr.setName(m.get("wr"));
            lr.setOntologyEntityName(clazz);
            lr.setType(type);
            lr.setDefinition(getEntryAttribute(LexiconQuery.QUERY_PREFIXES + LexiconQuery.SENSE_DEFINITION, "_SENSE_", m.get("s")));
            allr.add(lr);
        }
    }


}
