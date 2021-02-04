package es.upv.pros.pvalderas.microservice.asynchronous;

import java.util.Map;

import org.json.JSONArray;

public class OperationList {

	private static JSONArray operationList;
	private static Map<String,Class> operationClasses;
	
	protected static void set(JSONArray list){
		operationList=list;
	}
	
	protected static JSONArray get(){
		return operationList;
	}
	
	protected static String getAsString(){
		return operationList.toString();
	}

	public static Map<String, Class> getOperationClasses() {
		return operationClasses;
	}

	public static void setOperationClasses(Map<String, Class> classes) {
		operationClasses = classes;
	}
	
	
}
