package es.upv.pros.pvalderas.microservice.iotdevice;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
public class ClientStarter implements ApplicationRunner {
	 
	 
	@Autowired
	private ApplicationContext context;
	
	 
    @Override
    public void run(ApplicationArguments args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, JSONException {
    	
		Class mainClass=context.getBeansWithAnnotation(IoTDevice.class).values().iterator().next().getClass().getSuperclass();
		 
		if(mainClass!=null){
			String microservice=mainClass.getSimpleName();
			
			System.out.println("Setting up IoT Device "+microservice);
				        
			registerOperations(mainClass);
			
			System.out.println("Setting up OK");
			
		}
         
    }
    
    private void registerOperations(Class mainClass) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, JSONException {
        
        Annotation classAnnotation= mainClass.getDeclaredAnnotation(IoTDevice.class);
        Class classAPI=(Class)classAnnotation.annotationType().getMethod("serviceAPIClass").invoke(classAnnotation);
        
        JSONArray list=new JSONArray();
    
        for(Method m:classAPI.getMethods()){
	       	Annotation request=m.getAnnotation(RequestMapping.class);
	       	Annotation bpmnOperation = m.getDeclaredAnnotation(BPMNOperation.class);
	       	
	       	if(request!=null && bpmnOperation!=null){
	       		String operationName=(String)bpmnOperation.annotationType().getMethod("name").invoke(bpmnOperation);
	       		String operationId=(String)bpmnOperation.annotationType().getMethod("id").invoke(bpmnOperation);
	       	
	        
	        	JSONObject op=new JSONObject();
	       		String[] paths=(String[])request.annotationType().getMethod("value").invoke(request);
	       		RequestMethod[] methods=(RequestMethod[])request.annotationType().getMethod("method").invoke(request);        		
	       	
	       		op.put("id", operationId);
	       		op.put("name", operationName);
	       		op.put("path", paths[0]);
	       		op.put("method", methods[0]);
	       		op.put("type", "synchronous");
	       		
	       		list.put(op);
	        }
	    
        }
        
        OperationList.set(list);
        
   }
    
}
