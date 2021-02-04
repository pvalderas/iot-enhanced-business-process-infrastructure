package es.upv.pros.pvalderas.bpcontroller.server;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class EventManager {
	
	@Autowired
	ProcessEngine processEngine;
	
	private Properties props;
	
	public void registerOperationListener(String queue) throws IOException, TimeoutException{
		
		props=getProperties();
		
		switch(props.getProperty("eventBus.type")){
			case "rabbitmq": rabbitmqRegisterEvent(queue); break;
		}
	
	}
	
	private void rabbitmqRegisterEvent(String queue) throws IOException, TimeoutException{
		
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
				 
				
					JSONObject results;
					try {
						
						results = new JSONObject(new String(body));
						
						processEngine.getRuntimeService().setVariable(results.getString("processExecution"), "results", results.toString());
					
						System.out.println("***********************************************************");
				    	System.out.println("ASYNCRONOUS TASK EXECUTION: "+results.getString("taskId"));
				    	System.out.println("RESULTS: "+results.getString("data"));
				    	System.out.println("***********************************************************");

				    	processEngine.getExternalTaskService().complete(results.getString("taskId"), props.getProperty("spring.application.name"));
						
					
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				
			 }
		 };
		channel.basicConsume(queue, true, consumer);
	}


	 private  Properties getProperties(){
		YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ClassPathResource("application.yml"));
        Properties props=yamlFactory.getObject();
        return props;
	 }

	
	
	
}
