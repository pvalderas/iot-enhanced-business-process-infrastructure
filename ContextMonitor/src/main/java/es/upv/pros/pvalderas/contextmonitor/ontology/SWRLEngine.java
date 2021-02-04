package es.upv.pros.pvalderas.contextmonitor.ontology;

import java.io.File;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.stereotype.Component;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;


@Component
public class SWRLEngine {
	
	
	private SWRLRuleEngine ruleEngine;
	private static Map<String, String> SWRLrules;
	public static void setRules(Map<String, String> rules){
		SWRLrules=rules;
	}
		
	public void createEngine() throws OWLOntologyCreationException{
		if(ruleEngine==null){
			OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument(new File(ContextOntology.getOntologyPath()+ContextOntology.getOntologyName()));
			ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);
		}
	}
	
	
	public void infer(String filePath) throws OWLOntologyCreationException, SWRLParseException, SWRLBuiltInException{
		for(String ruleKey: SWRLrules.keySet()){
			ruleEngine.createSWRLRule(ruleKey,SWRLrules.get(ruleKey));
		}		 
		ruleEngine.infer();
	}
	
}
