package com.common.mq.receiver;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class KafkaReceiver extends AbstractReceiver{

	private static Logger logger = LoggerFactory.getLogger(KafkaReceiver.class);
	
	private String kafkaHosts;
	private String topic;
	private String groupID;
	private String keyDeserializer;
	private String valueDeserializer;
	private long timeoutMS;
	
	public KafkaReceiver(String kafkaHosts, String topic, String groupID, 			
			String keyDeserializer, String valueDeserializer,
			long timeoutMS){
		
		  this.kafkaHosts = kafkaHosts;
		  this.topic = topic;
		  this.groupID = groupID;
		  this.keyDeserializer = keyDeserializer;
		  this.valueDeserializer = valueDeserializer;
		  this.timeoutMS = timeoutMS;
	}
	
	
	@Override
	public void run() {
		System.out.println(">>> KafkaConsumer run");	
		KafkaConsumer<Object, Object> consumer = null;
		try{
			Properties props = new Properties();
			props.put("bootstrap.servers", kafkaHosts);
			props.put("auto.offset.reset", "latest");
			
			props.put("group.id", groupID);
			props.put("enable.auto.commit", "true");
			props.put("auto.commit.interval.ms", "1000");
//			props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//			props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
			props.put("key.deserializer", keyDeserializer);
			props.put("value.deserializer", valueDeserializer);
			
			consumer = new KafkaConsumer<>(props);
//			KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
			consumer.subscribe(Arrays.asList(topic));
			logger.info(">>>KafkaConsumer start");
			System.out.println(">>>KafkaConsumer start");
			while (true) {
				try{
					
					ConsumerRecords<Object, Object> records = consumer.poll(Duration.ofMillis(timeoutMS));
					logger.debug(">>>ConsumerRecords count:"+records.count());
					System.out.println(">>>ConsumerRecords count:"+records.count());
					for (ConsumerRecord<Object, Object> record : records){					
						this.putData(record.value());
					}					
				} catch(Exception exp){
					logger.error("", exp);
				} finally{
					
				}
			}			
		}catch(Exception exp){
			logger.error("", exp);
		}finally{
			if(consumer!=null){
				consumer.close();
			}
		}

	}
	
	public static void main(String[] args) {
		
		String kafkaHosts="192.168.1.183:9092,192.168.1.184:9092,192.168.1.185:9092";
		String keyDeserializer="org.apache.kafka.common.serialization.StringDeserializer";
		String valueDeserializer="org.apache.kafka.common.serialization.StringDeserializer";
		
		KafkaReceiver kafkaReceiver = new KafkaReceiver(kafkaHosts, "testtopic1", "Group01", keyDeserializer, valueDeserializer, 200);
		
		Thread t = new Thread(kafkaReceiver);
		t.start();
		
		while(true){
			Object data = kafkaReceiver.getData();
			System.out.println("getdata>>>"+data);			
		}
	}
	

}
