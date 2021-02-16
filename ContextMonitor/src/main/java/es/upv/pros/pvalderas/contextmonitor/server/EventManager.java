package es.upv.pros.pvalderas.contextmonitor.server;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
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

import es.upv.pros.pvalderas.contextmonitor.ontology.ContextModelManager;
import es.upv.pros.pvalderas.contextmonitor.ontology.HighLevelEventInjector;
import es.upv.pros.pvalderas.contextmonitor.ontology.SWRLEngine;

@Component
public class EventManager {
	
	@Autowired
	private ContextModelManager contextModel;
	
	@Autowired
	private SWRLEngine swrlEngine;
	
	@Autowired
	private HighLevelEventInjector highLevelEventInjector;
	
	
	public void registerContextChangeListener(String queue) throws IOException, TimeoutException{
		
		Properties props=getProperties();
		
		switch(props.getProperty("eventBus.type")){
			case "rabbitmq": rabbitmqEventListener(queue); break;
		}
	
	}
	
	private void rabbitmqEventListener(String queue) throws IOException, TimeoutException{
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
					try {
						
						System.out.println("Received Context Property");
						System.out.println(message);
						
						
						JSONObject  contextData = new JSONObject(message);
						String id=contextData.getString("id");
						contextModel.addInstance("Device", id);
						contextModel.setContextProp(id, "type", contextData.getString("device"));
						JSONArray props=contextData.getJSONArray("properties");
						for(int i=0;i<props.length();i++){
							contextModel.setContextProp(id, props.getJSONObject(i).getString("name"), props.getJSONObject(i).getString("value"));
						}
						
						
						
				
						swrlEngine.createEngine();
						swrlEngine.infer(contextModel.getOntologyPath());
						
						highLevelEventInjector.injectHighLevelEvents();
						
					} catch (Exception e) {
						e.printStackTrace();
					}
			 }
		 };
		channel.basicConsume(queue, true, consumer);
		
		System.out.println("Registered listener in: "+ queue);
	}


	 private  Properties getProperties(){
		YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(new ClassPathResource("application.yml"));
        Properties props=yamlFactory.getObject();
        return props;
	 }
	
	
	
}
