package es.upv.pros.pvalderas.microservice.iotdevice;

import org.json.JSONArray;

public class OperationList {

	private static JSONArray operationList;
	
	public static void set(JSONArray list){
		operationList=list;
	}
	
	public static JSONArray get(){
		return operationList;
	}
	
	public static String getAsString(){
		return operationList.toString();
	}
}
