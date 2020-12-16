/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author andrea
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(indexes = {
    @Index(columnList = "attestationUri")})
public abstract class Attestation extends SuperEntity {

    private Document document;
    private String attestationUri;
    private String attestationUriLemma;

    @ManyToOne(fetch = FetchType.LAZY)
    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
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
