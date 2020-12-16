/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.manager.LexiconManager;
import it.cnr.ilc.lc.omegalex.manager.LexiconQuery;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author andrea
 */
@ViewScoped
@Named
public class LexiconCreationControllerAdvancedFilter extends BaseController implements Serializable {

    @Inject
    private LexiconCreationControllerTabViewList lexiconCreationControllerTabViewList;
    @Inject
    private LexiconManager lexiconManager;
    @Inject
    private LoginController loginController;

    // for query optimization
    private static Pattern entryLangPattern = Pattern.compile("([a-z]+_entry)");

    private boolean allEntries = true;
    private boolean word = false;
    private boolean multiword = false;
    private boolean collocation = false;
    private boolean sublemma = false;
    private boolean verified = false;
    private boolean unverified = false;
    private boolean polysemic = false;
    private boolean monosemic = false;

    private boolean typesBlockDisabled = true;
    private boolean verifiyBlockDisabled = false;
    private boolean semicBlockDisabled = false;

    private String language = "All languages";
    private String scientificName = "any";
    private String alphabet = "any";
    private String pos = "any";

    public String getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public boolean isTypesBlockDisabled() {
        return typesBlockDisabled;
    }

    public void setTypesBlockDisabled(boolean typesBlockDisabled) {
        this.typesBlockDisabled = typesBlockDisabled;
    }

    public boolean isVerifiyBlockDisabled() {
        return verifiyBlockDisabled;
    }

    public void setVerifiyBlockDisabled(boolean verifiyBlockDisabled) {
        this.verifiyBlockDisabled = verifiyBlockDisabled;
    }

    public boolean isSemicBlockDisabled() {
        return semicBlockDisabled;
    }

    public void setSemicBlockDisabled(boolean semicBlockDisabled) {
        this.semicBlockDisabled = semicBlockDisabled;
    }

    public boolean isUnverified() {
        return unverified;
    }

    public void setUnverified(boolean unverified) {
        this.unverified = unverified;
    }

    public boolean isMonosemic() {
        return monosemic;
    }

    public void setMonosemic(boolean monosemic) {
        this.monosemic = monosemic;
    }

    public boolean isAllEntries() {
        return allEntries;
    }

    public void setAllEntries(boolean allEntries) {
        this.allEntries = allEntries;
    }

    public boolean isWord() {
        return word;
    }

    public void setWord(boolean word) {
        this.word = word;
    }

    public boolean isMultiword() {
        return multiword;
    }

    public void setMultiword(boolean multiword) {
        this.multiword = multiword;
    }

    public boolean isCollocation() {
        return collocation;
    }

    public void setCollocation(boolean collocation) {
        this.collocation = collocation;
    }

    public boolean isSublemma() {
        return sublemma;
    }

    public void setSublemma(boolean sublemma) {
        this.sublemma = sublemma;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isPolysemic() {
        return polysemic;
    }

    public void setPolysemic(boolean polysemic) {
        this.polysemic = polysemic;
    }

    public void allEntriesChanged() {
        if (allEntries) {
            word = false;
            multiword = false;
            typesBlockDisabled = true;
            sublemma = false;
            collocation = false;
        } else {
            allEntries = true;
            typesBlockDisabled = true;
            multiword = false;
            sublemma = false;
            collocation = false;
        }
    }

    public void wordChanged() {
        if (word) {
            typesBlockDisabled = true;
            multiword = false;
            allEntries = false;
            sublemma = false;
            collocation = false;
        } else {
            typesBlockDisabled = true;
            sublemma = false;
            collocation = false;
            multiword = false;
            allEntries = true;
        }
    }

    public void multiwordChanged() {
        if (multiword) {
            allEntries = false;
            word = false;
            typesBlockDisabled = false;
        } else {
            allEntries = true;
            word = false;
            typesBlockDisabled = true;
            sublemma = false;
            collocation = false;
        }
    }

    public void collocationChanged() {
        if (collocation) {
            sublemma = false;
        }
    }

    public void sublemmaChanged() {
        if (sublemma) {
            collocation = false;
        }
    }

    public void verifiedChanged() {
        if (verified) {
            unverified = false;
        }
    }

    public void unverifiedChanged() {
        if (unverified) {
            verified = false;
        }
    }

    public void polysemicChanged() {
        if (polysemic) {
            monosemic = false;
        }
    }

    public void monosemicChanged() {
        if (monosemic) {
            polysemic = false;
        }
    }

    private boolean isFilterable(Map<String, String> m, String keyFilter, String keyName) {
        boolean filterable = false;
        if (keyFilter.equals("any")) {
            filterable = true;
        } else {
            if (m.get(keyName) != null) {
                if (keyFilter.equals(m.get(keyName))) {
                    filterable = true;
                }
            } else {
                filterable = false;
            }
        }
        return filterable;
    }

    private void filterEntry(Map<String, String> m, Map<String, String> _mL, Map<String, String> _mS, List<Map<String, String>> l, List<Map<String, String>> s, String check) {
        if ((isFilterable(m, alphabet, "a")) && (isFilterable(m, pos, "pos")) && (isFilterable(m, scientificName, "sn"))) {
            String lang = getLang(m.get("le"));
            if (language.equals(lang) || language.equals("All languages")) {
                String lemmaInfo = lexiconManager.getLemmaInfo(m.get("individual"));
                if (!lemmaInfo.equals(LexiconQuery.NO_ENTRY_FOUND)) {
                    _mL.put("lemmaInfo", lemmaInfo);
                }
                if (check.equals("all")) {
                    _mL.put("writtenRep", m.get("writtenRep"));
                    _mL.put("individual", m.get("individual"));
                    _mL.put("lang", lang);
                    _mL.put("type", m.get("type"));
                    _mL.put("verified", m.get("verified"));
                    l.add(_mL);
                    _mS.put("writtenRep", m.get("sense"));
                    _mS.put("individual", m.get("sense"));
                    _mS.put("lang", lang);
                    _mS.put("verified", m.get("verified"));
                    s.add(_mS);

                } else {
                    if (m.get("verified").equals(check)) {
                        _mL.put("writtenRep", m.get("writtenRep"));
                        _mL.put("individual", m.get("individual"));
                        _mL.put("lang", lang);
                        _mL.put("type", m.get("type"));
                        _mL.put("verified", m.get("verified"));
                        l.add(_mL);
                        _mS.put("writtenRep", m.get("sense"));
                        _mS.put("individual", m.get("sense"));
                        _mS.put("lang", lang);
                        _mS.put("verified", m.get("verified"));
                        s.add(_mS);
                    }
                }
            }
        }
    }

    private void filterEntry(Map<String, String> m, Map<String, String> _mF, List<Map<String, String>> f, String check) {
        if ((isFilterable(m, alphabet, "a")) && (isFilterable(m, pos, "pos")) && (isFilterable(m, scientificName, "sn"))) {
            String lang = getLang(m.get("le"));
            if (language.equals(lang) || language.equals("All languages")) {
                if (check.equals("all")) {
                    _mF.put("lang", lang);
                    _mF.put("writtenRep", m.get("writtenRep"));
                    _mF.put("individual", m.get("individual"));
                    _mF.put("lang", getLang(m.get("le")));
                    _mF.put("type", m.get("type"));
                    _mF.put("verified", m.get("verified"));
                    f.add(_mF);
                } else {
                    if (m.get("verified").equals(check)) {
                        _mF.put("lang", lang);
                        _mF.put("writtenRep", m.get("writtenRep"));
                        _mF.put("individual", m.get("individual"));
                        _mF.put("lang", getLang(m.get("le")));
                        _mF.put("type", m.get("type"));
                        _mF.put("verified", m.get("verified"));
                        f.add(_mF);
                    }
                }
            }
        }
    }

    private boolean contains(String individual, List<Map<String, String>> l) {
        for (Map<String, String> _m : l) {
            if (_m.get("individual").equals(individual)) {
                return true;
            }
        }
        return false;
    }

    private List<Map<String, String>> getDistinctList(List<Map<String, String>> l) {
        List<Map<String, String>> _l = new ArrayList<Map<String, String>>();
        for (Map<String, String> m : l) {
            if (!contains(m.get("individual"), _l)) {
                _l.add(m);
            }
        }
        return _l;
    }

    public void filter() {
        long startTime = System.currentTimeMillis();
        String type = allEntries ? "all" : (word ? "Word" : (multiword ? "Multiword" : ""));
        if (collocation) {
            type = "Collocation";
        }
        if (sublemma) {
            type = "Sublemma";
        }
        String check = "all";
        if (verified) {
            check = "true";
        }
        if (unverified) {
            check = "false";
        }
        List<Map<String, String>> l = new ArrayList<Map<String, String>>();
        List<Map<String, String>> f = new ArrayList<Map<String, String>>();
        List<Map<String, String>> s = new ArrayList<Map<String, String>>();
        for (Map<String, String> m : lexiconManager.advancedFilter_lemmas()) {
            Map<String, String> _mL = new HashMap<String, String>();
            Map<String, String> _mS = new HashMap<String, String>();
            switch (type) {
                case "all":
                    filterEntry(m, _mL, _mS, l, s, check);
                    break;
                case "Word":
                    if (m.get("type").equals("Word")) {
                        filterEntry(m, _mL, _mS, l, s, check);
                    }
                    break;
                case "Multiword":
                    if (m.get("type").equals("Collocation") || m.get("type").equals("Sublemma")) {
                        filterEntry(m, _mL, _mS, l, s, check);
                    }
                    break;
                case "Collocation":
                    if (m.get("type").equals("Collocation")) {
                        filterEntry(m, _mL, _mS, l, s, check);
                    }
                    break;
                case "Sublemma":
                    if (m.get("type").equals("Sublemma")) {
                        filterEntry(m, _mL, _mS, l, s, check);
                    }
                    break;
            }
        }
        for (Map<String, String> m : lexiconManager.advancedFilter_forms()) {
            Map<String, String> _mF = new HashMap<String, String>();
            switch (type) {
                case "all":
                    filterEntry(m, _mF, f, check);
                    break;
                case "Word":
                    if (m.get("type").equals("Word")) {
                        filterEntry(m, _mF, f, check);
                    }
                    break;
                case "Multiword":
                    if (m.get("type").equals("Collocation") || m.get("type").equals("Sublemma")) {
                        filterEntry(m, _mF, f, check);
                    }
                    break;
                case "Collocation":
                    if (m.get("type").equals("Collocation")) {
                        filterEntry(m, _mF, f, check);
                    }
                    break;
                case "Sublemma":
                    if (m.get("type").equals("Sublemma")) {
                        filterEntry(m, _mF, f, check);
                    }
                    break;
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("durata: " + (endTime - startTime));
        lexiconCreationControllerTabViewList.cnlqFilterLemmaTabView(getDistinctList(l));
        lexiconCreationControllerTabViewList.cnlqFilterSenseTabView(getDistinctList(s));
        lexiconCreationControllerTabViewList.cnlqFilterFormTabView(getDistinctList(f));

    }

    public void resetFilter() {
        allEntries = true;
        word = false;
        multiword = false;
        collocation = false;
        sublemma = false;
        verified = false;
        unverified = false;
        polysemic = false;
        monosemic = false;
        typesBlockDisabled = true;
        verifiyBlockDisabled = false;
        semicBlockDisabled = false;
        language = "All languages";
        pos = "any";
        alphabet = "any";
        scientificName = "any";
        String recoveryLang = lexiconCreationControllerTabViewList.getLexiconLanguage();
        lexiconCreationControllerTabViewList.initFormTabView(recoveryLang);
        lexiconCreationControllerTabViewList.initLemmaTabView(recoveryLang);

    }

    private String getLang(String individual) {
        Matcher matcher = entryLangPattern.matcher(individual);
        if (matcher.find()) {
            return matcher.group(1).split("_entry")[0];
        } else {
            return null;
        }
    }
}
