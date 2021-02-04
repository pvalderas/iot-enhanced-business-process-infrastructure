package es.upv.pros.pvalderas.microservice.asynchronous;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AsynchronousMicroservice {
	
	public Class serviceAPIClass();

}
