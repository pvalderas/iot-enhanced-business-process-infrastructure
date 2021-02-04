package es.upv.pros.pvalderas.bpcontroller.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.jaxen.JaxenException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.upv.pros.pvalderas.bpcontroller.bpmn.domain.BPMNProcess;
import es.upv.pros.pvalderas.bpcontroller.bpmn.utils.XMLQuery;
import es.upv.pros.pvalderas.bpcontroller.server.dao.DAO;
import es.upv.pros.pvalderas.http.HTTPClient;

@RestController
@CrossOrigin
public class SeverHTTPController {
	
	@Autowired
	private DAO dao;
	
	@Autowired
	private SpringProcessEngineConfiguration config;
	
	@Autowired
	private ResourcePatternResolver resourceLoader;
	
	@Autowired
	private ProcessEngine processEngine;
	
	@Autowired
	private DiscoveryClient discoveryClient;
	
	
	@RequestMapping(
			  value = "/processes", 
			  method = RequestMethod.POST,
			  consumes = "application/json")
	public void saveBPMNPiece(@RequestBody BPMNProcess process) throws IOException, TimeoutException, JaxenException, DocumentException {
			
			String xml=this.addXMLAtts(process.getXml());
			//String xml=process.getXml();
			String fileName=this.createFile(process.getName(), xml);
			this.deployBPMNProcess();
			dao.saveProcess(process, fileName);
	}
	
	
	private String addXMLAtts(String xml) throws DocumentException, JaxenException{
		Document bpmn = DocumentHelper.parseText(xml);
		XMLQuery query=new XMLQuery(bpmn);
		String processQuery = "//bpmn:process";	
		Node process=query.selectSingleNode(processQuery);
		
		List<Node> tasks=query.selectNodes("bpmn:userTask",process);
		for(Node t:tasks){
			this.addListener((Element)t, "user");
		}
		tasks=query.selectNodes("bpmn:manualTask",process);
		for(Node t:tasks){
			this.addListener((Element)t, "manual");
		}

		((Element)process).addAttribute("isExecutable","true");
		
		return bpmn.asXML();
	}
	
	private void addListener(Element task, String type){
		Element extension=(Element)task.selectSingleNode("bpmn:extensionElements");
		if(extension==null){
			extension= DocumentHelper.createElement("bpmn:extensionElements");
			task.elements().add(0, extension);
		}
		Element listener=(Element)extension.selectSingleNode("camunda:executionListener");
		if(listener==null){
			listener=extension.addElement("camunda:executionListener");
		}
		
		String delegate=type=="user"?"${userTask}":"${manualTask}";

		listener.addAttribute("delegateExpression",delegate);	
		listener.addAttribute("event","start");	
	}
	
	private String createFile(String name, String xml) throws FileNotFoundException, UnsupportedEncodingException{
		 String fileName="processes/"+name+".bpmn";
		 File fichero=new File(fileName);
		 PrintWriter writer = new PrintWriter(fichero, "UTF-8");
		 writer.print(xml);
		 writer.close();
		 return fileName;
	}
	
	private void deployBPMNProcess() throws IOException{
		 final Resource[] resources = this.resourceLoader.getResources("file:" + System.getProperty("user.dir") + "/processes/*.bpmn");
	     System.out.println("Loaded Processes: "+resources.length);
	     config.setDeploymentResources(resources);
	     config.buildProcessEngine();
	}
	
	@RequestMapping(
			  value = "/process/{name}", 
			  method = RequestMethod.GET,
			  produces = "application/json")
	 @Transactional
	 public String getFragmentBPMN(@PathVariable(value="name") String name) throws IOException {
		 final Resource[] resources = this.resourceLoader.getResources("file:" + System.getProperty("user.dir") + "/processes/"+name+".bpmn");
		 if(resources.length==1){
			 return new String(Files.readAllBytes(Paths.get(resources[0].getURI())));
		 }
		 else return "";
	 }
	
	
	 @RequestMapping(
			  value = "/processes", 
			  method = RequestMethod.GET,
			  produces = "application/json")
	 @Transactional
	 public List<Map<String, Object>> getProcesses() {
		 return dao.getProcesses();
	 }
	 
	 
	 @RequestMapping(
			  value = "/{process}/start", 
			  method = RequestMethod.GET)
	 public String startProcessByID(@PathVariable(value="process") String processID) {
		 processEngine.getRuntimeService().startProcessInstanceByKey(processID);
		 return "Process "+processID+" started";
	 }
	 
	 @RequestMapping(
			  value = "process/start", 
			  method = RequestMethod.GET)
	 public String startDefaultProcess() {
		 Properties props=getProperties();
         
         String processID=props.getProperty("decoder.defaultProcessID");
         
         processEngine.getRuntimeService().startProcessInstanceByKey(processID);
		 return "Process "+processID+" started";
	 }
	 
	 @RequestMapping(
			  value = "/process/message", 
			  method = RequestMethod.POST)
	 public void message(@RequestBody String messageName) {
		 processEngine.getRuntimeService().createMessageCorrelation(messageName).correlate();
	 }
	
	 
	 @RequestMapping(
			  value = "/process/usertask/{id}", 
			  method = RequestMethod.PUT)
	 public void completeUserTask(@PathVariable(value="id") String taskId) {
		 String internalTaskID=processEngine.getTaskService().createTaskQuery().activityInstanceIdIn(taskId).singleResult().getId();
		 processEngine.getTaskService().complete(internalTaskID);
	 }
	 
	 
	 @RequestMapping(
			  value = "/process/asynctask/{microservice}/{operation}", 
			  method = RequestMethod.GET)
	 public String fetchAsyncTask(@PathVariable(value="microservice") String microservice, @PathVariable(value="operation") String operation) {
		 Properties props=getProperties();
		String BPControllerId=props.getProperty("spring.application.name");
		 
		 LockedExternalTask task = processEngine.getExternalTaskService().fetchAndLock(1, BPControllerId)
		  .topic(microservice+"."+operation, 60L * 1000L)
		  .execute().get(0);
		 
		 return task.getId();

	 }
	 	  
	 
	 private static Map<String, JSONObject> microservices=new Hashtable<String, JSONObject>();
	 @RequestMapping(
			  value = "/microservices", 
			  method = RequestMethod.GET)
	 public String getMicroservicesFromEUREKA(){
		Properties props=getProperties();
		String BPControllerId=props.getProperty("spring.application.name");
		String ActionPerformerId=props.getProperty("actionPerformer.name");
		 
		List<String> services = this.discoveryClient.getServices();
	    List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
	   
	    services.forEach(serviceName -> {
	        this.discoveryClient.getInstances(serviceName).forEach(instance ->{
	            instances.add(instance);
	        });
	    });
		
	    JSONArray instanceJSONList=new JSONArray();
	    instances.forEach(instance -> {
	    	try {
	    	
	    		if(!instance.getServiceId().toLowerCase().equals(BPControllerId.toLowerCase()) &&
	    				!instance.getServiceId().toLowerCase().equals(ActionPerformerId.toLowerCase())){
	    		
		    		JSONObject instanceJSON=new JSONObject();
		    		
		    		instanceJSON.put("id",instance.getServiceId());
					instanceJSON.put("host",instance.getHost());
					instanceJSON.put("port",instance.getPort());
					instanceJSON.put("connectionType",instance.getMetadata().get("connectionType"));
				
		    	
					instanceJSONList.put(instanceJSON);
	
					microservices.put((String)instanceJSON.get("id"), instanceJSON);
	    		}
			
	    	} catch (JSONException e) {
				e.printStackTrace();
			}
	    });
	    
	    return instanceJSONList.toString();
	 }
	 
	 @RequestMapping(
			  value = "/operations/{microservice}", 
			  method = RequestMethod.GET,
			  produces = "application/json")
	public String getOperations(@PathVariable(value="microservice") String microserviceID) throws IOException, JSONException  {
			
		 JSONObject microservice=microservices.get(microserviceID);	
		  
		 return HTTPClient.get("HTTP://"+microservice.getString("host")+":"+microservice.get("port")+"/operations");
			
	}

	 
	 private  Properties getProperties(){
		 YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
         yamlFactory.setResources(new ClassPathResource("application.yml"));
         Properties props=yamlFactory.getObject();
         return props;
	 }
}
