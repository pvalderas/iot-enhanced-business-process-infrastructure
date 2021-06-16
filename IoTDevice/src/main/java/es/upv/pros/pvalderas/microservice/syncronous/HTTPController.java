package es.upv.pros.pvalderas.microservice.syncronous;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
public class HTTPController {
	
	
	
	@GetMapping( value = "/operations", produces="application/json")
	 public String operations() {
		
		 return OperationList.getAsString();
	 }

}
