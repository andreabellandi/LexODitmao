/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.manager;

import com.google.common.collect.Multimap;
import it.cnr.ilc.lc.omegalex.controller.BaseController;
import it.cnr.ilc.lc.omegalex.controller.LexiconCreationControllerTabViewList;
import it.cnr.ilc.lc.omegalex.controller.LexiconCreationControllerTabViewList.DataTreeNode;
import it.cnr.ilc.lc.omegalex.controller.LoginController;
import it.cnr.ilc.lc.omegalex.domain.Account;
import it.cnr.ilc.lc.omegalex.manager.OntologyData.IndividualAxiom;
import it.cnr.ilc.lc.omegalex.manager.OntologyData.IndividualDetails;
import it.cnr.ilc.lc.omegalex.manager.OntologyData.Metadata;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.log4j.Level;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 *
 * @author andrea
 */
public class OntologyModel extends BaseController {

    @Inject
    private LoginController loginController;

    private OWLOntologyManager manager;
    private OWLOntology domainOntology;
    private OWLDataFactory factory;
    StructuralReasonerFactory reasonerFactory = null;
    OWLReasoner reasoner = null;

    private int individualsNumber = 0;

    private static final String ONTOLOGY_FOLDER = System.getProperty("user.home") + "/.LexO/";
    private static final String DEFAULT_ONTOLOGY = ONTOLOGY_FOLDER + "ditmaoOntology.owl";

    private PrefixManager pm;

    // never used ...
    public OntologyModel(FileUploadEvent f) {
        manager = OWLManager.createOWLOntologyManager();
        try (InputStream inputStream = f.getFile().getInputstream()) {
            domainOntology = manager.loadOntologyFromOntologyDocument(inputStream);
            factory = manager.getOWLDataFactory();
            reasonerFactory = new StructuralReasonerFactory();
            reasoner = reasonerFactory.createReasoner(domainOntology);
            setPrefixes();
        } catch (OWLOntologyCreationException | IOException ex) {
            log(Level.ERROR, ((loginController==null)?new Account(): loginController.getAccount()), "LOADING domain ontology ", ex);
        }
    }

    public OntologyModel() {
        manager = OWLManager.createOWLOntologyManager();
        try (InputStream inputStream = new FileInputStream(DEFAULT_ONTOLOGY)) {
            domainOntology = manager.loadOntologyFromOntologyDocument(inputStream);
            factory = manager.getOWLDataFactory();
            reasonerFactory = new StructuralReasonerFactory();
            reasoner = reasonerFactory.createReasoner(domainOntology);
            setPrefixes();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setPrefixes() {
        String namespace = domainOntology.getOntologyID().getOntologyIRI().get().toURI().toString();
        pm = new DefaultPrefixManager();
        pm.setPrefix("domainOntology", namespace);
    }

    public OWLOntologyManager getManager() {
        return manager;
    }

    public OWLOntology getDomainOntology() {
        return domainOntology;
    }

    public OWLDataFactory getFactory() {
        return factory;
    }

    public int getIndividualsNumber() {
        return individualsNumber;
    }

    private int getIndividualsNumberByClass(OWLClass cls) {
        int n = 0;
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, true);
        for (OWLNamedIndividual i : instances.getFlattened()) {
            n++;
        }
        return n;
    }

    public ArrayList<IndividualDetails> getIndividualsByClass(String clazz) {
        OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();
        ArrayList<IndividualDetails> indDetails = new ArrayList<IndividualDetails>();
        OWLClass cls = factory.getOWLClass(IRI.create(pm.getPrefixName2PrefixMap().get("domainOntology:"), "#" + clazz));
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, true);
        for (OWLNamedIndividual i : instances.getFlattened()) {
            IndividualDetails id = new IndividualDetails();
            id.setIri(i.getIRI().toString());
            id.setShortForm(i.getIRI().getShortForm());
            ArrayList<Metadata> metadata = getMetadataByIndividual(id, i);
            if (metadata != null) {
                id.setMetadata(metadata);
            }
            Multimap<OWLObjectPropertyExpression, OWLIndividual> assertedValues = EntitySearcher.getObjectPropertyValues(i, domainOntology);
            ArrayList<IndividualAxiom> axList = new ArrayList<IndividualAxiom>();
            for (OWLObjectProperty objProp : domainOntology.getObjectPropertiesInSignature(Imports.INCLUDED)) {
                for (OWLNamedIndividual ind : reasoner.getObjectPropertyValues(i, objProp).getFlattened()) {
                    boolean asserted = assertedValues.get(objProp).contains(ind);
                    axList.add(new IndividualAxiom(renderer.render(objProp), renderer.render(ind), IndividualAxiom.AxiomType.OBJECT_TYPE));
                }
            }
            for (OWLDataPropertyAssertionAxiom ax : domainOntology.getDataPropertyAssertionAxioms(i)) {
                axList.add(new IndividualAxiom(ax.getProperty().asOWLDataProperty().getIRI().getShortForm(), ax.getObject().getLiteral(), IndividualAxiom.AxiomType.DATA_TYPE));
            }
            if (axList != null) {
                id.setAxioms(axList);
            }
            indDetails.add(id);
        }
        return indDetails;
    }

    public ArrayList<Metadata> getMetadataByIndividual(IndividualDetails ind, OWLNamedIndividual i) {
        ArrayList<Metadata> mdList = new ArrayList<Metadata>();
        setMetadataByIndividual(mdList, i, OWLRDFVocabulary.RDFS_COMMENT.getIRI());
        setMetadataByIndividual(mdList, i, OWLRDFVocabulary.RDFS_LABEL.getIRI());
        return mdList;
    }

    private void setMetadataByIndividual(ArrayList<Metadata> mdList, OWLNamedIndividual i, IRI metadata) {
        Metadata md = getMetadataByIndividual(i, factory.getOWLAnnotationProperty(metadata));
        if (md != null) {
            mdList.add(md);
        }
    }

    public ArrayList<Metadata> getMetadataByClass(String clazz) {
        ArrayList<Metadata> metadataList = new ArrayList();
        OWLClass cls = factory.getOWLClass(IRI.create(pm.getPrefixName2PrefixMap().get("domainOntology:"), "#" + clazz));
        metadataList.add(new Metadata("Full class name", cls.getIRI().getIRIString()));
        metadataList.add(new Metadata("Short name", clazz));
        metadataList.add(getMetadataByClass(cls, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI())));
        metadataList.add(getMetadataByClass(cls, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI())));
        metadataList.add(new Metadata("Number of instances", Integer.toString(getIndividualsNumberByClass(cls))));
        return metadataList;
    }

    // **********************
    // it supposes that only ONE property of type annotation exists !!! (there could be more ... @it, @en, ...)
    // **********************
    private Metadata getMetadataByClass(OWLClass cls, OWLAnnotationProperty annotation) {
        for (Object obj : EntitySearcher.getAnnotations(cls, domainOntology, annotation).toArray()) {
            OWLAnnotationValue value = ((OWLAnnotation) obj).getValue();
            if (value instanceof OWLLiteral) {
                //System.out.println(cls + " labelled " + ((OWLLiteral) value).getLiteral());
                log(Level.INFO,(loginController==null)?new Account(): loginController.getAccount(), cls + " labelled " + ((OWLLiteral) value).getLiteral());
                return new Metadata(annotation.getIRI().getShortForm(), ((OWLLiteral) value).getLiteral());
            }
        }
        return null;
    }

    private Metadata getMetadataByIndividual(OWLNamedIndividual i, OWLAnnotationProperty annotation) {
        for (Object obj : EntitySearcher.getAnnotations(i, domainOntology, annotation).toArray()) {
            OWLAnnotationValue value = ((OWLAnnotation) obj).getValue();
            if (value instanceof OWLLiteral) {
                // System.out.println(i + " labelled " + ((OWLLiteral) value).getLiteral());
                log(Level.INFO, (loginController==null)?new Account(): loginController.getAccount(), i + " labelled " + ((OWLLiteral) value).getLiteral());

                return new Metadata(annotation.getIRI().getShortForm(), ((OWLLiteral) value).getLiteral());
            }
        }
        return null;
    }

    public int getOntologyHierarchy(TreeNode ontoRoot) {
        int classCounter = 0;
        individualsNumber = 0;
        OWLClass clazz = factory.getOWLThing();
        System.out.println("Class       : " + clazz);
        Set<OWLClass> set = domainOntology.classesInSignature().collect(Collectors.toSet());
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(domainOntology);
        printHierarchy(reasoner, clazz, 0, ontoRoot);
        for (OWLClass cl : set) {
            classCounter++;
            individualsNumber = individualsNumber + getIndividualsNumberByClass(cl);
            if (!reasoner.isSatisfiable(cl)) {
                log(Level.INFO,(loginController==null)?new Account(): loginController.getAccount(), "XXX: " + cl.getIRI().getShortForm());
                // System.out.println("XXX: " + cl.getIRI().getShortForm());
            }
        }
        reasoner.dispose();
        return classCounter;
    }

    public int getOntologyClassesNumber() {
        if (domainOntology != null) {
            return domainOntology.classesInSignature().collect(Collectors.toSet()).size();
        }
        return 0;
    }

    private void printHierarchy(OWLReasoner reasoner, OWLClass clazz, int level, TreeNode tn) {
        if (reasoner.isSatisfiable(clazz)) {
            for (int i = 0; i < level * 3; i++) {
                System.out.print(" ");
            }
            System.out.println(clazz.getIRI().getShortForm());
            TreeNode n = null;
            if (!clazz.getIRI().getShortForm().equals("Thing")) {
                LexiconCreationControllerTabViewList.DataTreeNode dtn = new LexiconCreationControllerTabViewList.DataTreeNode(clazz.getIRI().getShortForm(), clazz.getIRI().toString(), "", "", "false", "false", "", 0);
                n = new DefaultTreeNode(dtn, tn);
                n.setExpanded(true);
            }
            NodeSet<OWLClass> c = reasoner.getSubClasses(clazz, true);
            for (OWLClass child : c.entities().collect(Collectors.toList())) {
                if (!child.equals(clazz)) {
                    printHierarchy(reasoner, child, level + 1, n == null ? tn : n);
                }
            }
        }
    }

    public int getOntologyHierarchy(TreeNode ontoRoot, String nameToSelect) {
        int classCounter = 0;
        OWLClass clazz = manager.getOWLDataFactory().getOWLThing();
        System.out.println("Class       : " + clazz);
        Set<OWLClass> set = domainOntology.classesInSignature().collect(Collectors.toSet());
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(domainOntology);
        printHierarchy(reasoner, clazz, 0, ontoRoot, nameToSelect);
        for (OWLClass cl : set) {
            classCounter++;
            if (!reasoner.isSatisfiable(cl)) {
                System.out.println("XXX: " + cl.getIRI().getShortForm());
            }
        }
        reasoner.dispose();
        return classCounter;
    }

    private void printHierarchy(OWLReasoner reasoner, OWLClass clazz, int level, TreeNode tn, String nameToSelect) {
        if (reasoner.isSatisfiable(clazz)) {
            for (int i = 0; i < level * 3; i++) {
                System.out.print(" ");
            }
            System.out.println(clazz.getIRI().getShortForm());
            TreeNode n = null;
            if (!clazz.getIRI().getShortForm().equals("Thing")) {
                DataTreeNode dtn = new LexiconCreationControllerTabViewList.DataTreeNode(clazz.getIRI().getShortForm(), clazz.getIRI().toString(), "", "", "false", "false", "", 0);
                n = new DefaultTreeNode(dtn, tn);
                if (nameToSelect.equals("collapse")) {
                    n.setExpanded(false);
                } else {
                    n.setExpanded(true);
                }
            }
            NodeSet<OWLClass> c = reasoner.getSubClasses(clazz, true);
            for (OWLClass child : c.entities().collect(Collectors.toList())) {
                if (!child.equals(clazz)) {
                    printHierarchy(reasoner, child, level + 1, n == null ? tn : n, nameToSelect);
                }
            }

        }
    }

    public List<String> getClasses() {
        ArrayList<String> al = new ArrayList();
        OWLClass clazz = manager.getOWLDataFactory().getOWLThing();
        Set<OWLClass> set = domainOntology.classesInSignature().collect(Collectors.toSet());
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(domainOntology);
        printClasses(reasoner, clazz, 0, al);
        reasoner.dispose();
        return al;
    }

    private List<String> printClasses(OWLReasoner reasoner, OWLClass clazz, int level, ArrayList<String> al) {
        if (!clazz.getIRI().getShortForm().equals("Thing") && !clazz.getIRI().getShortForm().equals("Nothing")) {
            al.add(clazz.getIRI().getShortForm());
        }
        NodeSet<OWLClass> c = reasoner.getSubClasses(clazz, true);
        for (OWLClass child : c.entities().collect(Collectors.toList())) {
            if (!child.equals(clazz)) {
                printClasses(reasoner, child, level + 1, al);
            }
        }
        return al;
    }

    public int getOntologyDatatypePropertiesNumber() {
//        return domainOntology.getAxiomCount(AxiomType.DATA_PROPERTY_ASSERTION);
        return (int) domainOntology.dataPropertiesInSignature().count();
    }

    public int getOntologyObjectPropertiesNumber() {
        return (int) domainOntology.objectPropertiesInSignature().count();
//        return domainOntology.getAxiomCount(AxiomType.OBJECT_PROPERTY_ASSERTION);
    }

    public ArrayList<String> getOntologyClasses() {
        ArrayList<String> al = new ArrayList();
        Set<OWLClass> set = domainOntology.classesInSignature().collect(Collectors.toSet());
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(domainOntology);
        for (OWLClass cl : set) {
            if (reasoner.isSatisfiable(cl)) {
                al.add(cl.getIRI().getShortForm());
            }
        }
        reasoner.dispose();
        return al;
    }
    
        public List<String> getSubClassesOf(String clazz) {
        ArrayList<String> al = new ArrayList();
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(domainOntology);
        OWLClass cl = factory.getOWLClass(pm.getPrefixName2PrefixMap().get("domainOntology:"), "#" + clazz);
        printClasses(reasoner, cl, al);
        reasoner.dispose();
        al.remove(clazz);
        return al;
    }

    private List<String> printClasses(OWLReasoner reasoner, OWLClass clazz, ArrayList<String> al) {
        if (!clazz.getIRI().getShortForm().equals("Thing") && !clazz.getIRI().getShortForm().equals("Nothing")) {
            al.add(clazz.getIRI().getShortForm());
        }
        NodeSet<OWLClass> c = reasoner.getSubClasses(clazz, true);
        for (OWLClass child : c.entities().collect(Collectors.toList())) {
            if (!child.equals(clazz)) {
                printClasses(reasoner, child, al);
            }
        }
        return al;
    }

}
