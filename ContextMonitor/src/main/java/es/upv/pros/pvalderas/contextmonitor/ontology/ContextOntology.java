package es.upv.pros.pvalderas.contextmonitor.ontology;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

	public abstract class ContextOntology {
	 	 
	public abstract  Map<String, String> SWRLRules();
	public abstract  Map<String, String> highLevelEvents();
	 
	 
	private static String ontologyName;
	private static String ontologyPath;
	private static String ontologyPrefix;

	public static String getOntologyName() {
		if(ontologyName==null){
			ontologyName=getProperties().getProperty("contextOntology.name")+".owl";
		}
		return ontologyName;
	}


	public static String getOntologyPath() {
		if(ontologyPath==null){
			ontologyPath=System.getProperty("user.dir")+ File.separator + getProperties().getProperty("contextOntology.path")+ File.separator;
		}
		return ontologyPath;
	}
	
	public static String getPrefixUri() {
		if(ontologyPrefix==null){
			ontologyPrefix=getProperties().getProperty("contextOntology.prefix");
		}
		return ontologyPrefix;
	}
	 
	private static Properties props;
	private static Properties getProperties(){
		if(props==null){
			YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
	        yamlFactory.setResources(new ClassPathResource("application.yml"));
	        props=yamlFactory.getObject();
		}
        return props;
	 }
}
