/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.domain;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 *
 * @author andrea
 */
@Entity
public class Manuscript extends SuperEntity {

    private String siglum;
    private String title;

    @Column(unique = true)
    public String getSiglum() {
        return siglum;
    }

    public void setSiglum(String siglum) {
        this.siglum = siglum;
    }

    @Column(length = 4000)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
