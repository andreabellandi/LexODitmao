/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.controller.LexiconCreationControllerTabViewList.AttTreeNode;
import it.cnr.ilc.lc.omegalex.domain.Attestation;
import java.text.Normalizer;
import java.util.Comparator;

/**
 *
 * @author andrea
 */
public class AttestationComparator implements Comparator<LexiconCreationControllerTabViewList.AttTreeNode> {

    public AttestationComparator() {
    }

    @Override
    public int compare(AttTreeNode a1, AttTreeNode a2) {
//        String abbreviation1 = Normalizer.normalize(a1.getDocument().getAbbreviation(), Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}+]", "");
//        String abbreviation2 = Normalizer.normalize(a2.getDocument().getAbbreviation(), Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}+]", "");
//        return abbreviation1.compareTo(abbreviation2);
        String abbreviation1 = Normalizer.normalize(a1.getAttestationUriLemma().toLowerCase(), Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}+]", "");
        String abbreviation2 = Normalizer.normalize(a2.getAttestationUriLemma().toLowerCase(), Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}+]", "");
        return abbreviation1.compareTo(abbreviation2);
    }
}
