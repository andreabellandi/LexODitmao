/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omegalex.manager;

import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

/**
 *
 * @author andrea
 */
public class OntologyQuery {

    private final String sparqlPSCService = "http://lari-datasets.ilc.cnr.it/psc-m-adj-phon/sparql";
    private String default_query = "PREFIX psc: <http://lari-datasets.ilc.cnr.it/psc-m-adj-phon#>\n"
            + "PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#>\n"
            + "PREFIX lemon: <https://www.w3.org/ns/lemon/ontolex#>\n"
            + "SELECT ?p ?infl\n"
            + "WHERE {\n"
            + "  ?le lemon:writtenRep \"bello\" .\n"
            + "  ?le ?p ?infl .\n"
            + "}";

    public static void main(String[] args) throws QueryEngineException {
        OntologyQuery oq = new OntologyQuery();
        oq.ontologySearch(null);
    }
    
    public OntologyQuery() {
    }

    public void ontologySearch(SenseData sd) {
        QueryExecution q = QueryExecutionFactory.sparqlService(sparqlPSCService, default_query);
        ResultSet results = q.execSelect();
        while (results.hasNext()) {
            QuerySolution binding = results.nextSolution();
            System.out.println("property: " + binding.get("p").toString());
            System.out.println("value:    " + binding.get("infl").toString());
        }
    }
    
}
