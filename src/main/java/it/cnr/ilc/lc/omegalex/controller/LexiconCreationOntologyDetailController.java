/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.manager.OntologyData;
import it.cnr.ilc.lc.omegalex.manager.OntologyManager;
import it.cnr.ilc.lc.omegalex.manager.LexiconManager;
import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author andrea
 */
@ViewScoped
@Named
public class LexiconCreationOntologyDetailController extends BaseController implements Serializable {

    @Inject
    private OntologyManager ontologyManager;
    
    @Inject
    private LexiconManager lexiconManager;

    private boolean ontologyClassRendered = false;
    private OntologyData ontologyData;
//    private ArrayList<String> individualTabList;

//    public ArrayList<String> getIndividualTabList() {
//        return individualTabList;
//    }

    public boolean isOntologyClassRendered() {
        return ontologyClassRendered;
    }

    public void setOntologyClassRendered(boolean ontologyClassRendered) {
        this.ontologyClassRendered = ontologyClassRendered;
    }

    public OntologyData getOntologyClassDetail() {
        return ontologyData;
    }

    public void addOntologyClassDetails(String clazz) {
//        domainOntologyManager.getIndividualsByClass(clazz);
        ontologyData = new OntologyData();
        ontologyData.setMetadata(ontologyManager.getMetadataByClass(clazz));
//        individualTabList = domainOntologyManager.getIndividualsByClass(clazz);
        ontologyData.setIndividuals(ontologyManager.getIndividualsByClass(clazz));
        ontologyData.addLinguistiReferences(lexiconManager.getReferencingByOntology(clazz, OntologyData.LinguisticReference.ReferenceType.CLASS));
        for (String cl : ontologyManager.subClassesListOf(clazz)) {
            ontologyData.addLinguistiReferences(lexiconManager.getReferencingByOntology(cl, OntologyData.LinguisticReference.ReferenceType.SUBCLASS));
        }
    }

}

