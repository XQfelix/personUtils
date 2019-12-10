package com.common.mq.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitMQProducer {
	private static Logger logger = LoggerFactory.getLogger(RabbitMQProducer.class);
    private Connection connection = null;
    private Channel channel = null;
    
	private String rabbitHost;  //rabbitMQ地址
	private String userName; //用户名
	private String passWord; //密码
    
    
    public RabbitMQProducer(String rabbitHost, String userName, String passWord) {
		this.rabbitHost = rabbitHost;
		this.userName = userName;
		this.passWord = passWord;
		getConnection();
	}

	/*
	 * 获取连接
	 * */
	private void getConnection() {
        if(connection == null) {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(rabbitHost);
            connectionFactory.setUsername(userName);
            connectionFactory.setPassword(passWord);
            connectionFactory.setAutomaticRecoveryEnabled(true);
            try {
                connection = connectionFactory.newConnection();
                channel = connection.createChannel();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
    }
    
	
	/**
	  * 简单模式发送消息
	 * @param queueName 队列名称
	 * @param sendData 发送数据
	 * 
	 * */
    public void sendForSimpleQueue(String queueName, Object sendData) throws IOException, TimeoutException {
        channel.queueDeclare(queueName,false,false,false,null);
        channel.basicPublish("",queueName,null, RabbitMQProducer.toByteArray(sendData));
        logger.debug("send----->>> " + String.valueOf(sendData));
//        channel.close();
//        connection.close();
    }
    
    
    
    /**
	  * 订阅模式发送消息
	 * @param exchangeName 交换机名称
	 * @param sendData 发送数据
	 * 
	 * */
    public void sendForSubScribe(String exchangeName, Object sendData) throws IOException {
	    channel.exchangeDeclare(exchangeName,"fanout", true,true,null);
        channel.basicPublish(exchangeName,"",null,String.valueOf(sendData).getBytes());
        logger.debug("send----->>> " + String.valueOf(sendData));
    }
    
    
    
    /**
	  * 路由模式发送消息
	 * @param exchangeName 交换机名称
	 * @param routingKey 路由键
	 * @param sendData 发送数据
	 * 
	 * */
    public void SendForRouting(String exchangeName, String routingKey, Object sendData) throws IOException {
        channel.exchangeDeclare(exchangeName, "direct");
        channel.basicPublish(exchangeName, routingKey, null, String.valueOf(sendData).getBytes());
        logger.debug("send----->>> " + String.valueOf(sendData));
    }
    
    
    
    
    /**
	  * topics模式模式发送消息
	 * @param exchangeName 交换机名称
	 * @param routingKey 路由键(# 表示匹配一个或多个词；(lazy.a.b.c),  表示匹配一个词；(a.orange.b))
	 * @param sendData 发送数据
	 * 
	 * */
    public void SendForTopics(String exchangeName, String routingKey, Object sendData) throws IOException {
        channel.exchangeDeclare(exchangeName,"topic");
     
        channel.basicPublish(exchangeName,routingKey,false,false,null,String.valueOf(sendData).getBytes());
        logger.debug("send----->>> " + String.valueOf(sendData));
    }
    
    
    
    /**  
        * 对象转数组  
     * @param obj  
     * @return  
     */  
    public static byte[] toByteArray (Object obj) {      
        byte[] bytes = null;      
        ByteArrayOutputStream bos = new ByteArrayOutputStream();      
        try {        
            ObjectOutputStream oos = new ObjectOutputStream(bos);         
            oos.writeObject(obj);        
            oos.flush();         
            bytes = bos.toByteArray ();      
            oos.close();         
            bos.close();        
        } catch (IOException ex) {        
            ex.printStackTrace();   
        }      
        return bytes;    
    }   
    
    
    

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
//    	RabbitMQProducer RMQ = new RabbitMQProducer("192.168.1.162", "test", "test");
//    	List<Map<String, Object>> sendList = new ArrayList<Map<String,Object>>();
//    	Map<String, Object> sendMap = new HashMap<String, Object>();
//    	sendMap.put("hello", "world");
//    	sendList.add(sendMap);
//    	RMQ.sendForSimpleQueue("haha", sendList);
    }

}
