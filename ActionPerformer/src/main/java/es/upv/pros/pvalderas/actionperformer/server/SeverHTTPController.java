package es.upv.pros.pvalderas.actionperformer.server;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.upv.pros.pvalderas.http.HTTPClient;

@RestController
@CrossOrigin
public class SeverHTTPController {
	
	@Autowired
	private DiscoveryClient discoveryClient;
	
	@Autowired
	private AsyncronousOperationManager operationManager;

	@RequestMapping(
			  value = "/iot/microservice", 
			  method = RequestMethod.POST,
			  consumes = "application/json")
	public String executeOperation(@RequestBody String operation) throws JSONException, IOException, TimeoutException {
		
		JSONObject operationJSON= new JSONObject(operation);
		
		String microservice=operationJSON.getString("microservice");
		String path=operationJSON.getString("operationPath");
		String id=operationJSON.getString("operationID");
		String processExecution=operationJSON.getString("executionID");
		String data=operationJSON.getString("data");
		String method=operationJSON.getString("method");
		
		ServiceInstance instance=discoveryClient.getInstances(microservice).get(0);
		String connectionType=instance.getMetadata().get("connectionType");
		
		if (connectionType.equals("synchronous")){
			String results="";
			switch(method){
				case "GET":results = HTTPClient.get("HTTP://"+instance.getHost()+":"+instance.getPort()+path);break;
				case "POST":results = HTTPClient.post("HTTP://"+instance.getHost()+":"+instance.getPort()+path, data, true, "application/json");break;
			}
				
	    	return "{\"asynchronous\":false, \"results\":"+results+"}";
		}else{
			
			JSONObject asycOperation=new JSONObject();
			asycOperation.put("microservice", microservice);
			asycOperation.put("operation",id);
			asycOperation.put("processExecution",processExecution);
			asycOperation.put("data",data);
			operationManager.executeOperation(asycOperation.toString(), microservice+".operations");
			return "{\"asynchronous\":true, \"queueName\":\""+microservice+"."+id+"\"}";
			
		}
	
	}
	
	 
}
