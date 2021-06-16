package es.upv.pros.pvalderas.microservice.iotdevice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BPMNOperation {

	public String name();
	
	public String id();
	
}
