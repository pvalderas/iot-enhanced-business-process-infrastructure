package es.upv.pros.pvalderas.actionperformer.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ClientStarter implements ApplicationRunner {
	 
	 
	@Autowired
	private ApplicationContext context;
	

	 
    @Override
    public void run(ApplicationArguments args) {
    	
		Class mainClass=context.getBeansWithAnnotation(ActionPerformer.class).values().iterator().next().getClass().getSuperclass();
		 
		if(mainClass!=null){
			System.out.print("Setting up ActionPerformer......");
				        
				      
			System.out.println("OK");
			
		}
         
    }
    
    
}
