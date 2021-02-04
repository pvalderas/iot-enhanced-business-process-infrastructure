package es.upv.pros.pvalderas.actionperformer.server;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Component
public class AsyncronousOperationManager {
	
	
	public void executeOperation(String operation, String queue) throws IOException, TimeoutException{
		
		Properties props=getProperties();
		
		switch(props.getProperty("eventBus.type")){
			case "rabbitmq": rabbitmqExecuteOperation(operation, queue); break;
		}
	
	}
	
	private void rabbitmqExecuteOperation(String operation, String queue) throws IOException, TimeoutException{
		Properties props=getProperties();
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(props.getProperty("eventBus.host"));
		if(props.getProperty("eventBus.port")!=null) factory.setPort(Integer.parseInt(props.getProperty("eventBus.port")));
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(queue, false, false, false, null);

		channel.basicPublish("", queue, null, operation.getBytes());
		
		System.out.println("Published "+operation+" in "+queue);
		
		channel.close();
		connection.close();

	}


	 private  Properties getProperties(){
		YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ClassPathResource("application.yml"));
        Properties props=yamlFactory.getObject();
        return props;
	 }

	
	
	
}
