package es.upv.pros.pvalderas.bpcontroller.server.bpmn;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class UserTask implements ExecutionListener {

    public void notify(DelegateExecution execution) throws Exception {
     
	        	    	
	    	System.out.println("***********************************************************");
	    	System.out.println("USER TASK EXECUTION: "+execution.getActivityInstanceId()+":"+execution.getCurrentActivityName());
	        System.out.println("***********************************************************");

	      
	        
	    }
}
