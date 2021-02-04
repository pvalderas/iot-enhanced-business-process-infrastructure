package es.upv.pros.pvalderas.bpcontroller.server.bpmn;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import es.upv.pros.pvalderas.bpcontroller.server.EventManager;
import es.upv.pros.pvalderas.http.HTTPClient;

@Component
public class ServiceClass implements JavaDelegate
{
	FixedValue operationID;
	FixedValue operationPath;
	FixedValue microservice;
	
	@Autowired 
	EventManager eventManager;
    
    public void execute(final DelegateExecution execution) throws IOException, JSONException, TimeoutException {
    	
    	YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ClassPathResource("application.yml"));
        Properties props=yamlFactory.getObject();
        
        String data=execution.getVariable("results")!=null?(String)execution.getVariable("results"):"";
        
        JSONObject operation=new JSONObject();
		operation.put("microservice",microservice.getExpressionText());
		operation.put("operationPath", operationPath.getExpressionText());
		operation.put("operationID", operationID.getExpressionText());
		operation.put("executionID", execution.getId());
		operation.put("data", data);
		operation.put("method", "GET");
        
		String actionPerformerURL=props.getProperty("actionPerformer.serviceUrl");
		String results=HTTPClient.post(actionPerformerURL, operation.toString(), true, "application/json");
		
		execution.setVariable("results", results);
		
		JSONObject resultsJSON=new JSONObject(results);

		System.out.println("***********************************************************");
    	System.out.println("REST TASK EXECUTION: "+execution.getCurrentActivityName()+": "+microservice.getExpressionText()+"."+operationPath.getExpressionText());
    	System.out.println("RESULTS: "+resultsJSON.getJSONObject("results").toString());
        System.out.println("***********************************************************");

        
    }
}