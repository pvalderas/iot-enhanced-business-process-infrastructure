package es.upv.pros.pvalderas.bpcontroller.server.bpmn;

import java.util.Properties;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import es.upv.pros.pvalderas.bpcontroller.server.EventManager;
import es.upv.pros.pvalderas.http.HTTPClient;

@Component
public class AsynchronousTask implements ExecutionListener {
	
	
	@Autowired 
	EventManager eventManager;
	

	public void notify(DelegateExecution execution) throws Exception {
		
		YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ClassPathResource("application.yml"));
        Properties props=yamlFactory.getObject();
        String actionPerformerURL=props.getProperty("actionPerformer.serviceUrl");
   
        String operationString[] = ((String)execution.getVariable("operation")).split("\\.");
        String data=execution.getVariable("results")!=null?(String)execution.getVariable("results"):"";
        
        JSONObject operation=new JSONObject();
		operation.put("microservice",operationString[0]);
		operation.put("operationID", operationString[1]);
		operation.put("operationPath", "none");
		operation.put("executionID", execution.getId());
		operation.put("data", data);
		operation.put("method", "none");
		
		eventManager.registerOperationListener(operationString[0]+"."+operationString[1]);
		
		HTTPClient.post(actionPerformerURL, operation.toString(), false, "application/json");
    	

	}

	
 }