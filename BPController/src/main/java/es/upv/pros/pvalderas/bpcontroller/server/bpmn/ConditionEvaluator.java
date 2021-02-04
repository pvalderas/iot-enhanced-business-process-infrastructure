package es.upv.pros.pvalderas.bpcontroller.server.bpmn;

import java.util.Properties;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import es.upv.pros.pvalderas.http.HTTPClient;

@Component
public class ConditionEvaluator implements ExecutionListener {
	
	public void notify(DelegateExecution execution) throws Exception {
	  
		YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ClassPathResource("application.yml"));
        Properties props=yamlFactory.getObject();
        String contextMonitorURL=props.getProperty("contextMonitor.serviceUrl");
        String conditionPath=props.getProperty("contextMonitor.conditionPath");
        
        JSONObject condition=new JSONObject();
        condition.put("data", execution.getVariable("results"));
        condition.put("query", execution.getCurrentActivityName());
        
        String results=HTTPClient.post(contextMonitorURL+conditionPath, condition.toString(), true, "application/json");
		
		execution.setVariable("conditionResult", Boolean.parseBoolean(results));
		
		System.out.println("***********************************************************");
    	System.out.println("CONDITION EVALUATION: "+condition.getString("query"));
    	System.out.println("DATA: "+condition.getString("data"));
    	System.out.println("RESULTS: "+results);
        System.out.println("***********************************************************");

    }
	
 }