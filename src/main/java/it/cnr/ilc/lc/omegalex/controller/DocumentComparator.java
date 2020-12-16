/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.domain.Document;
import it.cnr.ilc.lc.omegalex.manager.DocumentData;
import java.text.Normalizer;
import java.util.Comparator;

/**
 *
 * @author andrea
 */
public class DocumentComparator implements Comparator<DocumentData> {

    @Override
    public int compare(DocumentData d1, DocumentData d2) {
        String abbreviation1 = Normalizer.normalize(d1.getAbbreviation(), Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}+]", "");
        String abbreviation2 = Normalizer.normalize(d2.getAbbreviation(), Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}+]", "");
        return abbreviation1.compareTo(abbreviation2);
    }

}
