/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.manager;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author andrea
 */
public class DictionaryData implements Serializable {
 
    private LemmaData lemma;
    private ArrayList<FormData> variants;
    private ArrayList<SenseData> senses;

    public LemmaData getLemma() {
        return lemma;
    }

    public void setLemma(LemmaData lemma) {
        this.lemma = lemma;
    }

    public ArrayList<FormData> getVariants() {
        return variants;
    }

    public void setVariants(ArrayList<FormData> variants) {
        this.variants = variants;
    }

    public ArrayList<SenseData> getSenses() {
        return senses;
    }

    public void setSenses(ArrayList<SenseData> senses) {
        this.senses = senses;
    }
        
    public DictionaryData() {
        this.lemma = new LemmaData();
        this.variants = new ArrayList();
        this.senses = new ArrayList();
    }
    
    public void clear() {
        this.lemma.clear();
        this.variants.clear();
        this.senses.clear();
    }
    
}
