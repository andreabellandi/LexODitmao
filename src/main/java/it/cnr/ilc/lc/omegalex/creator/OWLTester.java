/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.creator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 *
 * @author oakgen
 */
public class OWLTester {

    public static void main(String[] args) {
        try {
            OWLOntology ontology;
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            try (InputStream input = new FileInputStream("/Users/oakgen/Desktop/DiTMAO.owl")) {
                ontology = manager.loadOntologyFromOntologyDocument(input);
            }
            try (FileOutputStream output = new FileOutputStream("/Users/oakgen/Desktop/DiTMAO out.owl")) {
                manager.saveOntology(ontology, output);
            }
        } catch (IOException | OWLOntologyCreationException | OWLOntologyStorageException e) {
            System.err.println(e.getStackTrace());
        }
    }
}
