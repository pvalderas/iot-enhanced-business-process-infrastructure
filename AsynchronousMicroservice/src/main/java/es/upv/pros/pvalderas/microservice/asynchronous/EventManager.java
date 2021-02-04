package es.upv.pros.pvalderas.microservice.asynchronous;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import es.upv.pros.pvalderas.http.HTTPClient;

@Component
public class EventManager {
	 
	
	public void registerOperationListener(String microservice, String queue) throws IOException, TimeoutException{
		
		Properties props=getProperties();
		
		switch(props.getProperty("eventBus.type")){
			case "rabbitmq": rabbitmqRegisterEvent(microservice+"."+queue); break;
			default: rabbitmqRegisterEvent(microservice+"."+queue); break;
		}
	
	}
	
	private void rabbitmqRegisterEvent(String queue) throws IOException, TimeoutException{
		Properties props=getProperties();
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(props.getProperty("eventBus.host"));
		if(props.getProperty("eventBus.port")!=null) factory.setPort(Integer.parseInt(props.getProperty("eventBus.port")));
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(queue, false, false, false, null);

		Consumer consumer = new DefaultConsumer(channel) {
			 @Override
			 public void handleDelivery(String consumerTag, Envelope envelope, 
					 					AMQP.BasicProperties properties, byte[] body) throws IOException {
				 
				
					String message=new String(body);
					JSONObject messageJSON;

					try {
						
						messageJSON = new JSONObject(message);
						String microservice=messageJSON.getString("microservice");
						String operation=messageJSON.getString("operation");
						String data=messageJSON.getString("data");
						String processExecution=messageJSON.getString("processExecution");
						
					    String responseQueue=microservice+"."+operation;
						
						Properties props=getProperties();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						String fetchUrl=props.getProperty("bpController.serviceUrl")+props.getProperty("bpController.asyncTaskPath")+microservice+"/"+operation;
						String taskId=HTTPClient.get(fetchUrl);
	
						Connection connection2 = factory.newConnection();
						Channel channel2 = connection.createChannel();
						channel2.queueDeclare(responseQueue, false, false, false, null);
						
						String resultsOp=executeOperation(operation, data);
						
						JSONObject results= new JSONObject();
						results.put("data", resultsOp);
						results.put("taskId", taskId);
						results.put("processExecution",processExecution);
				
						channel2.basicPublish("", responseQueue, null, results.toString().getBytes());
						
						channel2.close();
						connection2.close();
							

					} catch (Exception e) {
						e.printStackTrace();
					}
				
			 }
		 };
		channel.basicConsume(queue, true, consumer);
		
		System.out.println("Registered listener in: "+ queue);
	}
	
	private String executeOperation(String operation, String data) throws JSONException, InstantiationException, IllegalAccessException{
		
		Class asyncOpClass=OperationList.getOperationClasses().get(operation);
		AsynchronousOperation op=(AsynchronousOperation)asyncOpClass.newInstance();
		
		return op.execute(data);
	}


	 private  Properties getProperties(){
		YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ClassPathResource("application.yml"));
        Properties props=yamlFactory.getObject();
        return props;
	 }

	
	
	
}
