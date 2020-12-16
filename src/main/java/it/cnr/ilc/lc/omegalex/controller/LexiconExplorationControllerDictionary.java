/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.manager.DictionaryData;
import it.cnr.ilc.lc.omegalex.manager.ExternalAttestationData;
import it.cnr.ilc.lc.omegalex.manager.FormData;
import it.cnr.ilc.lc.omegalex.manager.InternalAttestationData;
import it.cnr.ilc.lc.omegalex.manager.LemmaData;
import it.cnr.ilc.lc.omegalex.manager.SenseData;
import static j2html.TagCreator.attrs;
import static j2html.TagCreator.div;
import static j2html.TagCreator.span;
import static j2html.TagCreator.li;
import static j2html.TagCreator.ul;
import static j2html.TagCreator.a;
import static j2html.TagCreator.i;
import static j2html.TagCreator.join;
import static j2html.TagCreator.sup;
import j2html.tags.ContainerTag;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrea
 */
@ViewScoped
@Named
public class LexiconExplorationControllerDictionary extends BaseController implements Serializable {

    private static final Logger LOG = LogManager.getLogger(LexiconExplorationControllerDictionary.class);

    @Inject
    private LexiconCreationControllerFormDetail lexiconCreationControllerFormDetail;
    @Inject
    private LexiconCreationControllerSenseDetail lexiconCreationControllerSenseDetail;

    private DictionaryData dict = new DictionaryData();
    private String htmlEntry = "";
    private String newline = "<br/>";

    //Per catturare eventuali numeri in fondo al lemma
    Pattern patternLemma = Pattern.compile("(.+?)(\\d+\\*?)");

    Pattern patternSense = Pattern.compile("(.+)_([a-zA-Z]+)_(sense\\d+)");

    private boolean correspondence = false;

    public DictionaryData getDict() {
        return dict;
    }

    public void setDict(DictionaryData dict) {
        this.dict = dict;
    }

    public String getHtmlEntry() {
        return htmlEntry;
    }

    public void setHtmlEntry(String htmlEntry) {
        this.htmlEntry = htmlEntry;
    }

    public void createDictionaryEntry(String lemma) {
        dict.clear();
        // TODO: retrieve data and update dict
        htmlEntry = lemma;
    }

    public void clearDictionary() {
        dict.clear();
        htmlEntry = "";
    }

    private String getMorphoTraits(String pos, String gender, String number, String person, String mood, String voice) {
        StringBuilder morpho = new StringBuilder();
        morpho.append(pos.isEmpty() ? "" : pos.charAt(0) + ". ");
        morpho.append(gender.isEmpty() ? "" : gender.charAt(0) + ". ");
        morpho.append(number.isEmpty() ? "" : number.charAt(0) + ".");
        // traits only for verbs
        morpho.append(person.isEmpty() ? "" : person.charAt(0) + ".");
        morpho.append(mood.isEmpty() ? "" : mood.charAt(0) + ".");
        morpho.append(voice.isEmpty() ? "" : voice.charAt(0) + ".");

        return morpho.toString();
    }

    private String getLemma() {
        String ret;
        if (null != lexiconCreationControllerFormDetail) {
            if (null != lexiconCreationControllerFormDetail.getLemma()) {
                if ("reconstructedForm".equals(lexiconCreationControllerFormDetail.getLemma().getInfo())) {
                    ret = "*" + lexiconCreationControllerFormDetail.getLemma().getFormWrittenRepr();
                } else if ("corpusExternalLemma".equals(lexiconCreationControllerFormDetail.getLemma().getInfo())) {
                    ret = "#" + lexiconCreationControllerFormDetail.getLemma().getFormWrittenRepr();
                } else {
                    ret = lexiconCreationControllerFormDetail.getLemma().getFormWrittenRepr();
                }
                return ret;
            }
        }
        LOG.error("Lemma not found!");  //sollevare eccezione??

        return "unknown";
    }

    private String getPos() {

        return lexiconCreationControllerFormDetail.getLemma().getPoS().equals("No entry specified") ? "" : lexiconCreationControllerFormDetail.getLemma().getPoS();
    }

    private String getGender() {
        return lexiconCreationControllerFormDetail.getLemma().getGender().equals("No entry specified") ? "" : lexiconCreationControllerFormDetail.getLemma().getGender();

    }

    private String getNumber() {
        return lexiconCreationControllerFormDetail.getLemma().getNumber().equals("No entry specified") ? "" : lexiconCreationControllerFormDetail.getLemma().getNumber();

    }

    private String getPerson() {
        return lexiconCreationControllerFormDetail.getLemma().getPerson().equals("No entry specified") ? "" : lexiconCreationControllerFormDetail.getLemma().getPerson();

    }

    private String getMood() {
        return lexiconCreationControllerFormDetail.getLemma().getMood().equals("No entry specified") ? "" : lexiconCreationControllerFormDetail.getLemma().getMood();

    }

    private String getVoice() {
        return lexiconCreationControllerFormDetail.getLemma().getVoice().equals("No entry specified") ? "" : lexiconCreationControllerFormDetail.getLemma().getVoice();

    }

    private String getTraits() {
        return getMorphoTraits(getPos(), getGender(), getNumber(), getPerson(), getMood(), getVoice());
    }

    private boolean isCorrespondence() {
        if (null != lexiconCreationControllerFormDetail) {
            if (null != lexiconCreationControllerFormDetail.getLemma()) {
                return lexiconCreationControllerFormDetail.getLemma().getCorrespondence().equals("true");
            }
        }
        LOG.error("Lemma not found!");  //sollevare eccezione??
        return false;
    }

    private String getLang() {
        String ret;
        if (null != lexiconCreationControllerFormDetail) {
            if (null != lexiconCreationControllerFormDetail.getLemma()) {
                ret = lexiconCreationControllerFormDetail.getLemma().getLanguage();
                if (!lexiconCreationControllerFormDetail.getLemma().getUsedIn().isEmpty()
                        && !lexiconCreationControllerFormDetail.getLemma().getUsedIn().equals("No entry specified")) {
                    ret = ret + " utilisé en " + lexiconCreationControllerFormDetail.getLemma().getUsedIn();
                }
                return ret;
            }
        }
        return "unknown";
    }
    

    private String getSeeAoc() {
        String ret = "";
        if (lexiconCreationControllerFormDetail.getLemma().getLinguisticTypeEntry() != null) {
                        if (!lexiconCreationControllerFormDetail.getLemma().getLinguisticTypeEntry().getWrittenRep().isEmpty()) {
                            ret = "voir " + lexiconCreationControllerFormDetail.getLemma().getLinguisticTypeEntry().getLanguage() + " " +
                                    lexiconCreationControllerFormDetail.getLemma().getLinguisticTypeEntry().getWrittenRep();
                        }
                    }
        return ret;
    }
    
    public String getLemmaGramGrpInfo(String lemmaId, String lemmaClassName, String expClassName, String gramGrpClassName) {

        String lemma = getLemma();
        correspondence = isCorrespondence();
        String esponente = null;
        if (null != lemma) {
            Matcher matcher = patternLemma.matcher(lemma);
            if (matcher.find()) {
                lemma = matcher.group(1);
                esponente = matcher.group(2);
                LOG.info("lemma " + lemma + ", esponente " + esponente);
            }
            LOG.info("lemma " + lemma + ", NO esponente");

        }
        ContainerTag div = div(attrs("#" + lemmaId));
        ContainerTag spanLemma = span(lemma).withClass(lemmaClassName);

        if (esponente != null) {
            spanLemma.with(sup(esponente).withClass(expClassName));
        }
        div.with(spanLemma);
        div.with(span(getTraits()).withClass(gramGrpClassName));

        LOG.debug("div.renderFormatted() " + div.renderFormatted());
        return div.renderFormatted();
    }

    public String getLangInfo(String langId, String langClass) {

        ContainerTag _div = div(attrs("#" + langId + "_main"));
        
        String lang = getLang();
        ContainerTag div1 = div(attrs("#" + langId));
        ContainerTag spanLemma = span(lang).withClass(langClass);
        div1.with(spanLemma);
        _div.with(div1);
        String seeAoc = getSeeAoc();
        if (!seeAoc.isEmpty()) {
            ContainerTag div2 = div(attrs("#" + langId + "_seeAoc"));
            ContainerTag spanLemma2 = span(seeAoc).withClass(langClass);
            div2.with(spanLemma2);
            _div.with(div2);
        }
        
        LOG.debug("div.renderFormatted() " + _div.renderFormatted());
        return _div.renderFormatted();
    }

    public boolean isRendable() {
        return lexiconCreationControllerFormDetail.getLemma().getFormWrittenRepr() != null;
    }

    private List<String> getInternalAttestationsList() {
        //abbreviation sigla chapter_number paragraph_number list_entry
        return getInternalAttestation(lexiconCreationControllerFormDetail.getLemma().getInternalAttestation());
    }

    private List<String> getInternalAttestation(List<InternalAttestationData> internalAttestations) {

        ArrayList results = new ArrayList();

        for (InternalAttestationData iad : internalAttestations) {
            StringBuilder sb = new StringBuilder();
            sb.append(iad.getDocAbbreviation()).append(" ")
                    .append(iad.getManSigla())
                    .append(iad.getChapterNumber() != null ? (" " + iad.getChapterNumber()) : "")
                    .append(iad.getParagraphNumber() != null ? (" " + iad.getParagraphNumber()) : "")
                    .append(iad.getListEntry() != null ? (" " + iad.getListEntry()) : "");
            results.add(sb.toString());
        }
        LOG.info("getInternalAttestation(): size of results: " + results.size());

        return results;

    }

    public String getInternalAttestations(String id, String className) {

        StringBuilder sb = new StringBuilder();

        for (Iterator<String> iterator = getInternalAttestationsList().iterator(); iterator.hasNext();) {

            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append("; ");
            }
        }

        return sb.toString();
    }

    public String getInternalAttestations(String att, String id, String className) {
//        ContainerTag div = div(attrs("#" + id),
//                span(attrs("." + className), att)
//        );
//
//        return div.renderFormatted();
        ContainerTag li = li(attrs("#" + id), span(attrs("." + className), att));
        return li.renderFormatted();
    }

    public boolean isRendableLemmaComment() {
        boolean ret = !lexiconCreationControllerFormDetail.getLemma().getNote().isEmpty();
        return ret;
    }

    public String getLemmaComment(String id, String className) {

        String ret = "";
        if (isRendableLemmaComment()) {
//            ContainerTag li = li(attrs("#" + id),
//                    span(attrs("." + className), lexiconCreationControllerFormDetail.getLemma().getNote())
//            );
//            ret = li.renderFormatted();
            ret = lexiconCreationControllerFormDetail.getLemma().getNote();
        }
        LOG.debug("getLemmaComment() (" + ret + ")");
        return ret;
    }

    public String getVariants(String variantFormFrameClass, String variantClass, String variantFormClass, String variantAttestationClass, String variantNoteClass) {

        ContainerTag divVariants = div().withClass(variantFormFrameClass);

        for (FormData fd : lexiconCreationControllerFormDetail.getForms()) {
            ContainerTag div = div();
            div.with(span(fd.getFormWrittenRepr()).withClass(variantFormClass)).withClass(variantClass);
            if (!"none".equals(fd.getTransilterationType())) {//1
                //c'e' la traslitterazione
                div.with(span("[" + fd.getTransliteration() + "]"));
            }
            for (Iterator<String> it = getInternalAttestation(fd.getInternalAttestation()).iterator(); it.hasNext();) {
                String attestation = it.next();
                div.with(span(attestation + ((it.hasNext() ? "; " : ""))).withClass(variantAttestationClass));
            }

            if (!fd.getNote().isEmpty()) {
                div.with(div(span(fd.getNote())).withClass(variantNoteClass));
            }
            divVariants.with(div);
        }

        LOG.debug(divVariants.renderFormatted());
        return divVariants.renderFormatted();

    }

    public List<List<String>> __getVariantsList() {

        ArrayList results = new ArrayList();
        //abbreviation sigla chapter_number paragraph_number list_entry
        for (FormData fd : lexiconCreationControllerFormDetail.getForms()) {
            ArrayList row = new ArrayList();
            row.add(fd.getFormWrittenRepr());//0
            if (!"none".equals(fd.getTransilterationType())) {//1
                //c'e' la traslitterazione
                row.add(fd.getTransliteration());
            } else {
                row.add(null);
            }
            row.add((!fd.getNote().isEmpty() ? fd.getNote() : null));//2
            row.addAll(getInternalAttestation(fd.getInternalAttestation()));//3
            results.add(row);
        }
        return results;
    }

    public String __getVariantForm(List<String> variant) {
        StringBuilder sb = new StringBuilder();

        sb.append(variant.get(0));
        if (null != variant.get(1)) {
            sb.append(" ").append("[").append(variant.get(1)).append("]");
        }

        return sb.toString();
    }

    public String __getVariantNote(List<String> variant) {

        return variant.get(1); //note
    }

    public String getVariantAttributes(List<String> variant) {
        LOG.info("variant: " + variant);
        //String form = variant.get(0);
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i < variant.size(); i++) {
            sb.append(variant.get(i));
            sb.append("; ");
        }

        String attestations;
        if (sb.length() > 2) {
            attestations = sb.substring(0, sb.length() - 1);
        } else {
            attestations = sb.toString();
        }

        return attestations;
    }

    public List<List<String>> getSensesList() {

        ArrayList results = new ArrayList();
        //abbreviation sigla chapter_number paragraph_number list_entry
        for (SenseData sd : lexiconCreationControllerSenseDetail.getSenses()) {
            ArrayList row = new ArrayList();

            StringBuilder sb = new StringBuilder();
//
//            Matcher frMatcher = patternSense.matcher(french);
//            if (frMatcher.fiLineBreakpoint LexiconExplorationControllerDictionary.java : 322 successfully submitted.nd()) {
//                french = frMatcher.group(1).replaceAll("_", " ");
//            } else {
//                LOG.error("getSense(): no match found ! french " + french);
//
//            }
//            Matcher synMatcher = patternSense.matcher(synonym);
//            if (synMatcher.find()) {
//                synonym = synMatcher.group(1).replaceAll("_", " ");
//            } else {
//                LOG.error("getSense(): no match found ! synonym " + synonym);
//
//            }
//
//            Matcher corrMatcher = patternSense.matcher(correspondence);
//            if (corrMatcher.find()) {
//                LOG.info("corresp. " + corrMatcher.group(1));
//                correspondence = corrMatcher.group(1).replaceAll("_", " ");
//            } else {
//                LOG.error("getSense(): no match found ! correspondence " + correspondence);
//
//            }

            if (!sd.getScientificName().isEmpty()) { //Scientific Name 0
                for (int i = 0; i < sd.getScientificName().size() - 1; i++) {
                    sb.append(sd.getScientificName().get(i).getName()).append("; ");
                }
                sb.append(sd.getScientificName().get(sd.getScientificName().size() - 1).getName());
                row.add(sb.toString());
            } else {
                row.add(null);
            }

            if (!sd.getEnglishTranslation().isEmpty()) { //Eng. 1
                String english;
                sb = new StringBuilder();
                for (Iterator<SenseData.Openable> it = sd.getEnglishTranslation().iterator(); it.hasNext();) {
                    SenseData.Openable next = it.next();
                    english = next.getName();
                    Matcher engMatcher = patternSense.matcher(english);
                    if (engMatcher.find()) {
                        String grp = engMatcher.group(1);
                        //String aposReplace = grp.replaceAll("_APOS_", "'");
                        String aposReplace = grp.replaceAll("_APOS_", "’");
                        english = aposReplace.replaceAll("_", " ");
                    } else {
                        LOG.error("getSense(): no match found ! english " + english);

                    }
                    sb.append(english).append(it.hasNext() ? "; " : "");
                }
                row.add(sb.toString());
            } else {
                row.add(null);
            }

            if (!sd.getTranslation().isEmpty()) { //Eng. 1
                String french;
                sb = new StringBuilder();
                for (Iterator<SenseData.Openable> it = sd.getTranslation().iterator(); it.hasNext();) {
                    SenseData.Openable next = it.next();
                    french = next.getName();
                    Matcher m = patternSense.matcher(french);
                    if (m.find()) {
                        String grp = m.group(1);
                        //String aposReplace = grp.replaceAll("_APOS_", "'");
                        String aposReplace = grp.replaceAll("_APOS_", "’");
                        french = aposReplace.replaceAll("_", " ");
                    } else {
                        LOG.error("getSense(): no match found ! french " + french);

                    }
                    sb.append(french).append(it.hasNext() ? "; " : "");
                }
                row.add(sb.toString());
            } else {
                row.add(null);
            }

            if (!sd.getSynonym().isEmpty()) { //Eng. 1
                String str;
                sb = new StringBuilder();
                for (Iterator<SenseData.Openable> it = sd.getSynonym().iterator(); it.hasNext();) {
                    SenseData.Openable next = it.next();
                    str = next.getName();
                    str = str.replaceAll("_SHARP_", "");
                    str = str.replaceAll("_STAR_", "");
                    str = str.replaceAll("OB_", "");
                    str = str.replaceAll("_CB", "");
                    str = str.replaceAll("OSB_", "");
                    str = str.replaceAll("_CSB", "");
                    str = str.replaceAll("OSB_", "");
                    str = str.replaceAll("_CSB", "");
                    str = str.replaceAll("_QUEST_", "");
                    Matcher m = patternSense.matcher(str);
                    if (m.find()) {
                        str = m.group(1).replaceAll("_", " ");
                    } else {
                        LOG.error("getSense(): no match found ! sysnonym " + str);

                    }
                    sb.append(str).append(it.hasNext() ? "; " : "");
                }
                row.add(sb.toString());
            } else {
                row.add(null);
            }

            if (!sd.getCorrespondence().isEmpty()) { //Eng. 1
                String str;
                sb = new StringBuilder();
                for (Iterator<SenseData.Openable> it = sd.getCorrespondence().iterator(); it.hasNext();) {
                    SenseData.Openable next = it.next();
                    str = next.getName();
                    Matcher m = patternSense.matcher(str);
                    if (m.find()) {
                        str = m.group(1).replaceAll("_", " ") + "@" + m.group(2);
                    } else {
                        LOG.error("getSense(): no match found ! correspondence " + str);
                    }
                    sb.append(str).append(it.hasNext() ? "; " : "");
                }
                row.add(sb.toString());
            } else {
                row.add(null);
            }

            row.add(sd.getOWLClass().getName()); //ONTO 5

            results.add(row);
        }

        return results;
    }

    public String getSense(List<String> sense, String id, String className, String smallCapsClass) {

        String name = sense.get(0);
        String english = sense.get(1);
        String french = sense.get(2);
        String synonym = sense.get(3);
        String correspondence = sense.get(4);
        String onto = sense.get(5);
        ContainerTag mainDiv = div().withClass(className);
        LOG.debug("getSense(): name " + name);
        LOG.debug("getSense(): english " + english);
        LOG.info("getSense(): correspondence " + correspondence);
        LOG.debug("getSense(): onto (" + onto + ")");

        if (null != name) {
            mainDiv.with(div(span(name)));
        }
        if (null != english) {
            mainDiv.with(div(span(join(i("Engl. "), english))));
        }
        if (null != french) {
            mainDiv.with(div(span(join(i("Fr. "), french))));
        }
        if (onto.length() > 0) {
            mainDiv.with(div(span("Onto: " + onto)).withClass(smallCapsClass));
        }
        if (null != synonym) {
            mainDiv.with(div(span(join(i("Syn. "), synonym))));
        }
        if (null != correspondence) {
            if (isCorrespondence()) {
                mainDiv.with(div(span(join(i("Corr. of "), correspondence))));
            } else {
                mainDiv.with(div(span(join(i("Corr. "), correspondence))));
            }
        }

        return mainDiv.renderFormatted();
    }

    public String getSenseUL(List<String> sense, String id, String className, String smallCapsClass) {

        String name = sense.get(0);
        String english = sense.get(1);
        String french = sense.get(2);
        String synonym = sense.get(3);
        String correspondence = sense.get(4);
        String onto = sense.get(5);

        ContainerTag ul = ul(attrs("." + className));
        if (name != null) {
            ul.with(li(span(attrs("." + smallCapsClass), name)));
        }
        if (english != null) {
            ul.with(li(span(attrs("." + className), join(i("Engl. "), english))));
        }
        if (french != null) {
            ul.with(li(span(attrs("." + className), join(i("Fr. "), french))));
        }

        if (onto != null) {
            ul.with(li(span(attrs("." + smallCapsClass), "Onto " + onto)));
        }
        if (synonym != null) {
            ul.with(li(span(attrs("." + className), join(i("Syn. "), synonym))));
        }
        if (correspondence != null) {
            ul.with(li(span(attrs("." + className), correspondence)));
        }

        return ul.renderFormatted();
    }

    public String getSubLemmata(String id, String subLemmataClassName) {

        StringBuilder sb = new StringBuilder();
        for (LemmaData.Word word : lexiconCreationControllerFormDetail.getLemma().getSublemma()) {
            sb.append(word.getWrittenRep()).append("; ");

        }
        return sb.substring(0, sb.length() - 1);

    }

    public String getCollocations(String id, String collocationClassName) {

        StringBuilder sb = new StringBuilder();
        for (LemmaData.Word word : lexiconCreationControllerFormDetail.getLemma().getCollocation()) {
            sb.append(word.getWrittenRep()).append("; ");

        }
        return sb.substring(0, sb.length() - 1);

    }

    public String getSeeAlso(String id, String seeAlsoClassName) {

        ContainerTag div = div().withClass(seeAlsoClassName);
        if (lexiconCreationControllerFormDetail.getLemma().getSeeAlso().size() > 0) {
            div.with(span("See also: "));
        }
        for (Iterator<LemmaData.Word> it = lexiconCreationControllerFormDetail.getLemma().getSeeAlso().iterator(); it.hasNext();) {
            LemmaData.Word word = it.next();
            div.with(span(word.getWrittenRep() + ((it.hasNext() ? "; " : ""))));
        }

        return div.renderFormatted();

    }

    public List<Map.Entry<String, String>> getSeeAlsoList(String id, String seeAlsoClassName) {

        ArrayList<Map.Entry<String, String>> ret = new ArrayList<>();

        for (Iterator<LemmaData.Word> it = lexiconCreationControllerFormDetail.getLemma().getSeeAlso().iterator(); it.hasNext();) {
            LemmaData.Word word = it.next();
            ret.add(new AbstractMap.SimpleEntry<>(word.getOWLName(), word.getWrittenRep()));
        }

        return ret;

    }

    public boolean isRendableExternalAttestations() {

        boolean ret = !lexiconCreationControllerFormDetail.getLemma().getExternalAttestation().isEmpty();

        return ret;
    }

    public List<String> getExternalAttestations(String id, String className) {

        ArrayList<String> row = new ArrayList<>();
        StringBuilder sb;
        for (ExternalAttestationData ead : lexiconCreationControllerFormDetail.getLemma().getExternalAttestation()) {

            if (!ead.getAbbreviationId().isEmpty()) {
                if (!(ead.getForm().isEmpty() || ead.getUrl().isEmpty())) {
                    //Caso DOM o FEW
                    sb = new StringBuilder();
                    sb.append(ead.getAbbreviationId()).append(" ").append((a(ead.getForm()).withHref(ead.getUrl()).withTarget("_blank")).renderFormatted()).append(" ");
                    getExtrnalAttestationData(ead, sb).append("; ");
                    row.add(sb.toString());
                } else {
                    sb = new StringBuilder();
                    sb.append(ead.getAbbreviationId()).append(" ");
                    sb.append(!ead.getForm().isEmpty() ? ead.getForm() + " " : "");
                    getExtrnalAttestationData(ead, sb).append("; ");
                    //sb.append(ead.getAbbreviationId()).append(" ").append(ead.getVolume()).append(" ").append(ead.getPageNumber());
                    row.add(sb.toString());
                }
            } else {
                row.add(null);
            }

        }
        return row;
    }

    private StringBuilder getExtrnalAttestationData(ExternalAttestationData ead, StringBuilder sb) {
        sb.append(ead.getVolume().isEmpty() ? "" : (ead.getSubvolume().isEmpty() ? ead.getVolume() + ": " : ead.getVolume() + " "))
                .append(ead.getSubvolume().isEmpty() ? "" : ead.getSubvolume() + ": ")
                .append(ead.getPageNumber().isEmpty() ? "" : ead.getPageNumber() + " ")
                .append(ead.getColumnNumber().isEmpty() ? "" : ead.getColumnNumber() + " ")
                .append(ead.getArticleNumber().isEmpty() ? "" : ead.getArticleNumber() + " ")
                .append(ead.getLineNumber().isEmpty() ? "" : ead.getLineNumber() + " ")
                .append(ead.getGlossaryNumber().isEmpty() ? "" : ead.getGlossaryNumber() + " ")
                .append(ead.getNumberOfMap().isEmpty() ? "" : ead.getNumberOfMap() + " ")
                .append(ead.getFootnoteNumber().isEmpty() ? "" : ead.getFootnoteNumber() + " ")
                .append(ead.getFolioNumber().isEmpty() ? "" : ead.getFolioNumber() + " ")
                .append(ead.getNumberOfGeographicPoint().isEmpty() ? "" : ead.getNumberOfGeographicPoint() + " ")
                .append(ead.getEntryNumber().isEmpty() ? "" : ead.getEntryNumber() + " ")
                .append(ead.getChapterNumberRoman().isEmpty() ? "" : ead.getChapterNumberRoman() + " ")
                .append(ead.getFascicle().isEmpty() ? "" : ead.getFascicle() + " ")
                .append(ead.getPart().isEmpty() ? "" : ead.getPart() + " ")
                .append(ead.getParagraphNumber().isEmpty() ? "" : ead.getParagraphNumber() + " ")
                .append(ead.getBook().isEmpty() ? "" : ead.getBook() + " ")
                .append(ead.getSec().isEmpty() ? "" : ead.getSec() + " ")
                .append(ead.getOther().isEmpty() ? "" : ead.getOther() + " ")
                .append(ead.getSvSublemma().isEmpty() ? "" : ead.getSvSublemma() + " ");
        return sb;
    }

}
