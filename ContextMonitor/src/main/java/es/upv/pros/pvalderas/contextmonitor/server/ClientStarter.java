package es.upv.pros.pvalderas.contextmonitor.server;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import es.upv.pros.pvalderas.contextmonitor.ontology.ContextModelManager;
import es.upv.pros.pvalderas.contextmonitor.ontology.ContextOntology;
import es.upv.pros.pvalderas.contextmonitor.ontology.HighLevelEventInjector;
import es.upv.pros.pvalderas.contextmonitor.ontology.SWRLEngine;

@Component
public class ClientStarter implements ApplicationRunner {
	 
	 
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private EventManager eventManager;
	
	@Autowired
	private ContextModelManager contextModelManager;
	 
    @Override
    public void run(ApplicationArguments args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException, TimeoutException, InstantiationException {
    	
		Class mainClass=context.getBeansWithAnnotation(ContextMonitor.class).values().iterator().next().getClass().getSuperclass();
		 
		if(mainClass!=null){
			System.out.print("Setting up Context Monitor......");
			
			eventManager.registerContextChangeListener("context");
			this.setOntology(mainClass);
			
			contextModelManager.loadOntology();
	      
			System.out.println("OK");
			
		}
         
    }
    
    private void setOntology(Class mainClass) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException{
    	Annotation classAnnotation= mainClass.getDeclaredAnnotation(ContextMonitor.class);
        Class contextOntology=(Class)classAnnotation.annotationType().getMethod("contextOntology").invoke(classAnnotation);
        
        ContextOntology ontology=(ContextOntology)contextOntology.newInstance();
        
        SWRLEngine.setRules(ontology.SWRLRules());
        HighLevelEventInjector.setEvents(ontology.highLevelEvents());
        
    }
    
    
}
