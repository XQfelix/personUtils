package com.common.mq.receiver;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.TimeoutException;

public class RabbitMqReceiver extends AbstractReceiver{
	
	private static Logger logger = LoggerFactory.getLogger(RabbitMqReceiver.class);
    private static Connection connection = null; //连接
    private static Channel channel = null; //通道
    
    private static RabbitMqReceiver rabbitMq = null;
    private static boolean ackFlag = true;
    
	private static String rabbitHost;  //rabbitMQ地址
	private static String userName; //用户名
	private static String passWord; //密码
	private static String queueName; //队列名称
	private static String exchangeName; //交换机名称
    private static String routingKey; //路由键
    private static String routingMode; //路由模式  fanout  direct  topic  
    
    
    /**简单模式
     * @param rabbitHost rabbitMQ地址
     * @param userName 用户名
     * @param passWord 密码
     * @param queueName 队列名称
     * */
    public static RabbitMqReceiver getInstance(String rabbitHost, String userName, String passWord, String queueName) {
    	if(rabbitMq == null) {
    		rabbitMq = new RabbitMqReceiver(rabbitHost, userName, passWord, queueName);
    	}
    	getConnection();
    	return rabbitMq;
    }
    
    
    /**订阅模式
     * @param rabbitHost rabbitMQ地址
     * @param userName 用户名
     * @param passWord 密码
     * @param queueName 队列名称
     * @param exchangeName 交换机名称
     * */
    public static RabbitMqReceiver getInstance(String rabbitHost, String userName, String passWord, String queueName, String exchangeName) {
    	if(rabbitMq == null) {
    		rabbitMq = new RabbitMqReceiver(rabbitHost, userName, passWord, queueName, exchangeName);
    	}
    	getConnection();
    	return rabbitMq;
    }
    
    
    /**路由模式
     * @param rabbitHost rabbitMQ地址
     * @param userName 用户名
     * @param passWord 密码
     * @param queueName 队列名称
     * @param exchangeName 交换机名称
     * @param routingKey 路由键(在topic模式下, #：表示匹配一个或多个词；（lazy.a.b.c）  *：表示匹配一个词；（a.orange.b）)
     * @param routingMode 路由模式 支持 direct直接路由,  topic通配符 两种模式
     * */
    public static RabbitMqReceiver getInstance(String rabbitHost, String userName, String passWord, String queueName, String exchangeName, String routingKey, String routingMode) {
    	if(rabbitMq == null) {
    		rabbitMq = new RabbitMqReceiver(rabbitHost, userName, passWord, queueName, exchangeName, routingKey, routingMode);
    	}
    	getConnection();
    	return rabbitMq;
    }
    
    
    /**简单模式
     * @param rabbitHost rabbitMQ地址
     * @param userName 用户名
     * @param passWord 密码
     * @param queueName 队列名称
     * */
    @SuppressWarnings("static-access")
    public RabbitMqReceiver(String rabbitHost, String userName, String passWord, String queueName) {
		this.rabbitHost = rabbitHost;
		this.userName = userName;
		this.passWord = passWord;
		this.queueName = queueName;
		
	}
    
    /**订阅模式
     * @param rabbitHost rabbitMQ地址
     * @param userName 用户名
     * @param passWord 密码
     * @param queueName 队列名称
     * @param exchangeName 交换机名称
     * */
    @SuppressWarnings("static-access")
	public RabbitMqReceiver(String rabbitHost, String userName, String passWord, String queueName, String exchangeName) {
		this.rabbitHost = rabbitHost;
		this.userName = userName;
		this.passWord = passWord;
		this.queueName = queueName;
		this.exchangeName = exchangeName;
	}
    
    
    
    /**路由模式
     * @param rabbitHost rabbitMQ地址
     * @param userName 用户名
     * @param passWord 密码
     * @param queueName 队列名称
     * @param exchangeName 交换机名称
     * @param routingKey 路由键(在topic模式下, #：表示匹配一个或多个词；（lazy.a.b.c）  *：表示匹配一个词；（a.orange.b）)
     * @param routingMode 路由模式 支持 direct直接路由,  topic通配符 两种模式
     * */
    @SuppressWarnings("static-access")
    public RabbitMqReceiver(String rabbitHost, String userName, String passWord, String queueName, String exchangeName, String routingKey, String routingMode) {
		this.rabbitHost = rabbitHost;
		this.userName = userName;
		this.passWord = passWord;
		this.queueName = queueName;
		this.exchangeName = exchangeName;
		this.routingKey = routingKey;
		this.routingMode = routingMode;
	}
    
    public RabbitMqReceiver() {}
	
    @Override
	public void run() {
		try {
			//单队列模式
			channel.basicQos(1);//能者多劳
			channel.queueDeclare(queueName,false,false,false,null);  //声明队列
			if(exchangeName != null) {
				if(routingMode!=null && routingKey!=null) {
					//路由模式 或者 Topics模式  #：表示匹配一个或多个词；（lazy.a.b.c）  *：表示匹配一个词；（a.orange.b）
					channel.exchangeDeclare(exchangeName, routingMode);
					channel.queueBind(queueName, exchangeName, routingKey);
				} else {
					//订阅模式
					channel.queueBind(queueName,exchangeName,"");
					ackFlag = false;
				}
			}
	        DefaultConsumer consumer = new DefaultConsumer(channel) {
	            @Override
	            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
	                super.handleDelivery(consumerTag, envelope, properties, body);
	                logger.debug("RabbitMqReceiver------->>>" + RabbitMqReceiver.toObject(body));
	                rabbitMq.putData(new String(body,"UTF-8"));
	                if(ackFlag == false) {
	                	channel.basicAck(envelope.getDeliveryTag(),false);
	                }
	            }
	        };
			channel.basicConsume(queueName,ackFlag,consumer);
			logger.info("RabbitMqReceiver BootUp Success!!");
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("RabbitMqReceiver Consumer: ",e);
		}
	}
	
	
	/*
	 * 获取连接
	 * */
	private static void getConnection() {
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
        * 数组转对象  
     * @param bytes  
     * @return  
     */  
    public static Object toObject (byte[] bytes) {      
        Object obj = null;      
        try {        
            ByteArrayInputStream bis = new ByteArrayInputStream (bytes);        
            ObjectInputStream ois = new ObjectInputStream (bis);        
            obj = ois.readObject();      
            ois.close();   
            bis.close();   
        } catch (IOException ex) {        
            ex.printStackTrace();   
        } catch (ClassNotFoundException ex) {        
            ex.printStackTrace();   
        }      
        return obj;    
    }

	
   public static void main(String[] args) throws IOException {
//	   RabbitMqReceiver RabbitMqReceiver = getInstance("192.168.1.162", "test", "test", "haha");
//	   Thread t = new Thread(RabbitMqReceiver);
//	   t.start();
//		
//		while(true){
//			Object data = RabbitMqReceiver.getData();
//			System.out.println("getdata>>>"+((List<Map<String, Object>>) data).size());			
//		}
	}

}
