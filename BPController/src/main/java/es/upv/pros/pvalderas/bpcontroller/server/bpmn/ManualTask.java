package es.upv.pros.pvalderas.bpcontroller.server.bpmn;

import java.io.IOException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONException;
import org.springframework.stereotype.Component;

@Component
public class ManualTask implements JavaDelegate
{
    
    public void execute(final DelegateExecution execution) throws IOException, JSONException {
    	
    	
    	System.out.println("***********************************************************");
    	System.out.println("MANUAL INTERACTION"+execution.getActivityInstanceId()+":"+execution.getCurrentActivityName());
        System.out.println("***********************************************************");
        
    }
}