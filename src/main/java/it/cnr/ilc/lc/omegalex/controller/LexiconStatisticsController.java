/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.domain.InternalAttestation;
import it.cnr.ilc.lc.omegalex.manager.AccountManager;
import it.cnr.ilc.lc.omegalex.manager.DocumentationManager;
import it.cnr.ilc.lc.omegalex.manager.ExternalAttestationData;
import it.cnr.ilc.lc.omegalex.manager.FormData;
import it.cnr.ilc.lc.omegalex.manager.InternalAttestationData;
import it.cnr.ilc.lc.omegalex.manager.LemmaData;
import it.cnr.ilc.lc.omegalex.manager.LemmaData.Word;
import it.cnr.ilc.lc.omegalex.manager.LexiconManager;
import it.cnr.ilc.lc.omegalex.manager.LexiconQuery;
import it.cnr.ilc.lc.omegalex.manager.PropertyValue;
import it.cnr.ilc.lc.omegalex.manager.ReferenceData;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.log4j.Level;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.chart.PieChartModel;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 *
 * @author andrea
 */
@ViewScoped
@Named
public class LexiconStatisticsController extends BaseController implements Serializable {

    @Inject
    private PropertyValue propertyValue;
    @Inject
    private LexiconCreationControllerTabViewList lexiconCreationControllerTabViewList;
    @Inject
    private LexiconCreationControllerSenseDetail lexiconCreationViewSenseDetail;
    @Inject
    private LexiconCreationControllerRelationDetail lexiconCreationControllerRelationDetail;
    @Inject
    private LexiconExplorationControllerDictionary lexiconExplorationControllerDictionary;
    @Inject
    private ExternalAttestationCreationDialogController externalAttestationCreationDialogController;
    @Inject
    private InternalAttestationCreationDialogController internalAttestationCreationDialogController;
    @Inject
    private DocumentationManager documentationManager;
    @Inject
    private LexiconManager lexiconManager;
    @Inject
    private LoginController loginController;

    private PieChartModel pieModel1;

    public PieChartModel getPieModel1() {
        createPieModel1();
        return pieModel1;
    }

    private void createPieModel1() {
        pieModel1 = new PieChartModel();
        pieModel1.set("Ancient Occitan", 1833);
        pieModel1.set("Ancient Catalan", 18);

        pieModel1.set("French", 1012);
        pieModel1.set("English", 741);

        pieModel1.set("Hebrew", 361);
        pieModel1.set("Aramaic", 37);
        pieModel1.set("Latin", 160);
        pieModel1.set("Arabic", 805);

//        pieModel1.setTitle("Multilingual lexicon languages");
        pieModel1.setLegendPosition("w");
        pieModel1.setShowDataLabels(true);
    }

    public void itemSelect(ItemSelectEvent event) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Item selected",
                "Item Index: " + event.getItemIndex() + ", Series Index:" + event.getSeriesIndex());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        System.out.println("AAAAAAAHHHHHHHH");
    }
    
    public ArrayList<User> getUsers() {
        ArrayList<User> alu = new ArrayList();
        alu.add(new User("?????????", "******", "users", "23", "-", "3,4%"));
        alu.add(new User("?????????", "******", "users", "23", "-", "3,4%"));
        alu.add(new User("?????????", "******", "users", "23", "-", "3,4%"));
        alu.add(new User("?????????", "******", "users", "23", "-", "3,4%"));
        alu.add(new User("?????????", "******", "users", "23", "-", "3,4%"));
        alu.add(new User("?????????", "******", "users", "23", "-", "3,4%"));
        alu.add(new User("?????????", "******", "users", "23", "-", "3,4%"));
        alu.add(new User("?????????", "******", "users", "23", "-", "3,4%"));
        return alu;
    }
    
    
    public static class User {
        
        private String username;
        private String password;
        private String role;
        private String editedEntries;
        private String validatedEntries;
        private String percent;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getEditedEntries() {
            return editedEntries;
        }

        public void setEditedEntries(String editedEntries) {
            this.editedEntries = editedEntries;
        }

        public String getValidatedEntries() {
            return validatedEntries;
        }

        public void setValidatedEntries(String validatedEntries) {
            this.validatedEntries = validatedEntries;
        }

        public String getPercent() {
            return percent;
        }

        public void setPercent(String percent) {
            this.percent = percent;
        }
        
        
        
        public User(String us, String pwd, String role, String ee, String ve, String perc) {
            this.editedEntries = ee;
            this.password = pwd;
            this.percent = perc;
            this.role = role;
            this.username = us;
            this.validatedEntries = ve;
        }
        
    }

}
