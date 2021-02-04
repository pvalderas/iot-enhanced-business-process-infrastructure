package es.upv.pros.pvalderas.contextmonitor.ontology;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import es.upv.pros.pvalderas.http.HTTPClient;

@Component
public class HighLevelEventInjector {
	
	private static Map<String, String> events;
	public static void setEvents(Map<String, String> evs) {
		events = evs;
	}
	
	ContextModelManager contextManager;
	
	
	public void injectHighLevelEvents() throws IOException{
		Properties props=getProperties();
		
		String BPController=props.getProperty("bpController.serviceUrl")+props.getProperty("bpController.messagePath");
		
		for(String ruleKey: events.keySet()){
			if(contextManager.checkCondition(events.get(ruleKey))){
				HTTPClient.post(BPController, ruleKey, false, "text/plain");
			}
		}
	}
	
	private  Properties getProperties(){
		YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ClassPathResource("application.yml"));
        Properties props=yamlFactory.getObject();
        return props;
	 }
	
}
