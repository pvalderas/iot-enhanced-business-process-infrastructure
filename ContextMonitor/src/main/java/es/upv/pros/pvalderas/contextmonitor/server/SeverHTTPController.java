package es.upv.pros.pvalderas.contextmonitor.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.upv.pros.pvalderas.contextmonitor.ontology.ContextModelManager;
import es.upv.pros.pvalderas.contextmonitor.ontology.SPARQLEngine;

@RestController
@CrossOrigin
public class SeverHTTPController {
	
	@Autowired
	private ContextModelManager contextModelManager;

	@RequestMapping(
			  value = "/context/query", 
			  method = RequestMethod.POST,
			  consumes = "application/json")
	public boolean checkCondition(@RequestBody String condition)  {
		
		return contextModelManager.checkCondition(condition);
		
		
	}
	
	 
}
