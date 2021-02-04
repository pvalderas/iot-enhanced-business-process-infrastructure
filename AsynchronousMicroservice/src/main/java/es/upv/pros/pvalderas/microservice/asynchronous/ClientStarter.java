package es.upv.pros.pvalderas.microservice.asynchronous;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class ClientStarter implements ApplicationRunner {
	 
	 
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private EventManager eventManager;
	 
    @Override
    public void run(ApplicationArguments args) throws IOException, TimeoutException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, JSONException, InstantiationException {
    	
		Class mainClass=context.getBeansWithAnnotation(AsynchronousMicroservice.class).values().iterator().next().getClass().getSuperclass();
		 
		if(mainClass!=null){
			Properties props=getProperties();
			String microservice=props.getProperty("spring.application.name").toUpperCase();
			microservice=microservice==null?mainClass.getSimpleName():microservice;
			
			System.out.println("Setting up Asynchronous Microservice "+microservice);
				        
			eventManager.registerOperationListener(microservice, "operations");
			registerOperationsAPI(mainClass);
			
			System.out.println("Setting up OK");
			
		}
         
    }
    
    private  Properties getProperties(){
		YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ClassPathResource("application.yml"));
        Properties props=yamlFactory.getObject();
        return props;
	 }
    
 private void registerOperationsAPI(Class mainClass) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, JSONException, InstantiationException {
        
        Annotation classAnnotation= mainClass.getDeclaredAnnotation(AsynchronousMicroservice.class);
        Class classAPI=(Class)classAnnotation.annotationType().getMethod("serviceAPIClass").invoke(classAnnotation);
        
        JSONArray list=new JSONArray();
        Map<String, Class> classes= new Hashtable<String, Class>();
    
        for(Method m:classAPI.getMethods()){
	       	Annotation bpmnOperation = m.getDeclaredAnnotation(BPMNOperation.class);
	       	
	       	if( bpmnOperation!=null){
	       		String operationName=(String)bpmnOperation.annotationType().getMethod("name").invoke(bpmnOperation);
	       		String operationId=(String)bpmnOperation.annotationType().getMethod("id").invoke(bpmnOperation);
	            	
	        
	        	JSONObject op=new JSONObject();
	        	
	        	op.put("id", operationId);
	       		op.put("name", operationName);
	       		op.put("path", "none");
	       		op.put("method", "none");
	       		op.put("type", "asynchronous");
	       		
	       		list.put(op);
	       		
	       		Class asyncOpClass=(Class)m.invoke(classAPI.newInstance());
	       		classes.put(operationId, asyncOpClass);
	       		
	        }
	    
        }
        
        OperationList.set(list);
        OperationList.setOperationClasses(classes);
        
   }
    
}
