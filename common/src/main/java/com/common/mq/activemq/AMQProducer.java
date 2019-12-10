package com.common.mq.activemq;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.IllegalStateException;
import javax.jms.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AMQProducer {
	
	
	public static final int  TOPIC = 1;
	public static final int  QUEUE = 2;
	
	public static final int  PERSISTENT = DeliveryMode.PERSISTENT;
	public static final int  NON_PERSISTENT = DeliveryMode.NON_PERSISTENT;
	
	private String amqUrl;
	private String user;
	private String password;
	private int chanelMode;
	private String topicOrQueue;
	private int deliveryMode;	
	private ScheduledExecutorService scheduledService;
	private static AMQProducer amqProducer=null;
	
	private MessageProducer producer = null;
	private Session session = null;
	
	//private static List<Map> listJsonData = new ArrayList<Map>();
	@SuppressWarnings("rawtypes")
	private static List<Map> listJsonData = Collections.synchronizedList(new ArrayList<Map>());
	
	private static Map<String, Object> mapJsonData = Collections.synchronizedMap(new HashMap<String, Object>());
	
	//默认10秒
	private int delay=2;
	
	private static Logger logger = LoggerFactory.getLogger(AMQProducer.class);
	
	/**获取MQ发送对象
	 * 默认用户名，密码，默认为Queue的方式，默认为非持久化
	 * @param amqUrl MQ URL
	 * @param topicOrQueue Topic或者Queue名称
	 * @return 
	 * @throws Exception 
	 */
	public static AMQProducer getInstance(String amqUrl,String topicOrQueue) throws Exception{
		if(amqProducer==null){
			amqProducer = new AMQProducer(amqUrl,topicOrQueue);
		}		
		return amqProducer;
	}
	
	/**获取MQ发送对象
	 * 默认用户名，密码，默认为Queue的方式，默认为非持久化
	 * @param amqUrl MQ URL
	 * @param topicOrQueue Topic或者Queue名称
	 * @param delay 最晚提交时间秒
	 * @return 
	 * @throws Exception 
	 */
	public static AMQProducer getInstance(String amqUrl,String topicOrQueue,int delay) throws Exception{
		if(amqProducer==null){
			amqProducer = new AMQProducer(amqUrl,topicOrQueue,delay);
		}		
		return amqProducer;
	}

	/** 获取MQ发送对象
	 *  默认用户名，密码，默认为非持久化
	 * @param amqUrl MQ URL
	 * @param chanelMode 发送渠道是Topic还是Queue
	 * @param topicOrQueue Topic或者Queue名称
	 * @param delay 最晚提交时间秒
	 * @return
	 * @throws Exception 
	 */
	public static AMQProducer getInstance(String amqUrl,int chanelMode,String topicOrQueue,int delay) throws Exception{
		if(amqProducer==null){
			amqProducer = new AMQProducer(amqUrl,chanelMode,topicOrQueue,delay);
		}		
		return amqProducer;
	}
	
	/**获取MQ发送对象
	 * 默认用户名，密码
	 * @param amqUrl MQ URL
	 * @param chanelMode 发送渠道是Topic还是Queue
	 * @param topicOrQueue Topic或者Queue名称
	 * @param deliveryMode  非持久化或持久化
	 * @param delay 最晚提交时间秒
	 * @return
	 * @throws Exception 
	 */
	public static AMQProducer getInstance(String amqUrl,int chanelMode,String topicOrQueue,int deliveryMode,int delay) throws Exception{
		if(amqProducer==null){
			amqProducer = new AMQProducer(amqUrl,chanelMode,topicOrQueue,deliveryMode,delay);
		}		
		return amqProducer;
	}
	
	/**获取MQ发送对象
	 * @param amqUrl MQ URL
	 * @param user 用户名
	 * @param password 密码
	 * @param chanelMode 发送渠道是Topic还是Queue
	 * @param topicOrQueue Topic或者Queue名称
	 * @param deliveryMode 非持久化或持久化
	 * @param delay 最晚提交时间秒
	 * @return
	 * @throws Exception 
	 */
	public static AMQProducer getInstance(String amqUrl,String user,String password,
			int chanelMode, String topicOrQueue,int deliveryMode,int delay) throws Exception{
		if(amqProducer==null){
			amqProducer = new AMQProducer(amqUrl,user,password,chanelMode,topicOrQueue,deliveryMode,delay);
		}		
		return amqProducer;
	}
	
//	private AMQProducer(){
//		
//	}
	
	public AMQProducer(String amqUrl,String topicOrQueue) throws Exception {
		this(amqUrl, ActiveMQConnection.DEFAULT_USER,
				ActiveMQConnection.DEFAULT_PASSWORD,
				AMQProducer.QUEUE,
				topicOrQueue, DeliveryMode.NON_PERSISTENT,0);
		
	}
	
	public AMQProducer(String amqUrl,String topicOrQueue,int delay) throws Exception{
		this(amqUrl, ActiveMQConnection.DEFAULT_USER,
				ActiveMQConnection.DEFAULT_PASSWORD,
				AMQProducer.QUEUE,
				topicOrQueue, DeliveryMode.NON_PERSISTENT,delay);
	}
	
	public AMQProducer(String amqUrl,int chanelMode, String topicOrQueue,int delay) throws Exception{
		this(amqUrl, ActiveMQConnection.DEFAULT_USER,
				ActiveMQConnection.DEFAULT_PASSWORD,
				chanelMode,
				topicOrQueue, DeliveryMode.NON_PERSISTENT,delay);
	}
	
	public AMQProducer(String amqUrl,String topicOrQueue,String user,String password,int delay) throws Exception{
		this(amqUrl,user,password,AMQProducer.QUEUE,
				topicOrQueue, DeliveryMode.NON_PERSISTENT,delay);
	}
	

	
	public AMQProducer(String amqUrl,int chanelMode, String topicOrQueue,int deliveryMode,int delay) throws Exception{
		this(amqUrl, ActiveMQConnection.DEFAULT_USER,
				ActiveMQConnection.DEFAULT_PASSWORD,
				chanelMode,topicOrQueue,deliveryMode,delay);	
	}
	
	public AMQProducer(String amqUrl,String user,String password,
			int chanelMode, String topicOrQueue,int deliveryMode,int delay) throws Exception{
		
		if(delay>0){
			this.delay = delay;
		}		
		init(amqUrl,user,password,chanelMode,topicOrQueue,deliveryMode);
	}
	
	private void init(String amqUrl,String user,String password,
			int chanelMode, String topicOrQueue,int deliveryMode) throws Exception{
		
		this.amqUrl = amqUrl;
		this.user = user;
		this.password = password;
		this.chanelMode = chanelMode;
		this.topicOrQueue = topicOrQueue;
		this.deliveryMode = deliveryMode;
		
		Connection connection = null;
		
		Destination destination = null;
		
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(user, password, amqUrl);
		connection = factory.createConnection();
		session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
		
		if(chanelMode==TOPIC){
			destination = session.createTopic(topicOrQueue);
		} else{
			destination = session.createQueue(topicOrQueue);
		}
		
		producer = session.createProducer(destination);
		// 设置不持久化，此处学习，实际根据项目决定
		producer.setDeliveryMode(deliveryMode);	
		
		connection.start();	
		
	    Runnable runnable = new Runnable() {
	        public void run() {
	        	try {
					sendCommit();
				} catch (Exception exp) {
					logger.error("scheduled sendCommit error", exp);
				}
	        }
	    };
	    
	    
	    scheduledService = Executors.newSingleThreadScheduledExecutor();
	    // 参数：1、任务体 2、首次执行的延时时间 
	    //      3、任务执行间隔 4、间隔时间单位
	    scheduledService.scheduleAtFixedRate(runnable, 0, this.delay, TimeUnit.SECONDS);
	}
	
	private void reInit() throws Exception {
		scheduledService.shutdown();
		scheduledService = null;
		init(this.amqUrl,this.user,this.password,
				this.chanelMode, this.topicOrQueue,this.deliveryMode);
	}
	
	/**分批次往Activi MQ发送数据
	 * @param mapData 待发送数据，Map数据中不支持对象
	 * @param batch 每批次发送的数据量
	 */
	public void send(Map<String,Object> mapData,int batch){
		synchronized(listJsonData){
			listJsonData.add(mapData);
			if(listJsonData.size()>=batch && listJsonData.size()>0){
				doSend();
			}			
		}
	}
	
	/**分批次往Activi MQ发送数据
	 * @param listData 待发送数据，Map数据中不支持对象
	 * @param batch 每批次发送的数据量
	 */
	public void sendList(List<Map<String,Object>> listData,int batch){
		synchronized(listJsonData){
			listJsonData.addAll(listData);
			if(listJsonData.size()>=batch && listJsonData.size()>0){
				doSend();
			}			
		}
	}
	
	/**
	 * 以map格式发送数据
	 * 
	 * @param mapData 待发送数据，不支持对象
	 */
	public void sendMap (Map<String, Object> mapData) {
		synchronized(mapJsonData){
			mapJsonData.putAll(mapData);
			if(mapJsonData.size()>0){
				doSendSingle();
			}			
		}
	}
	
	private void doSendSingle() {
		JsonWriter writer = null;
		try {
			synchronized (mapJsonData) {
				if (mapJsonData.size() > 0) {
					DslJson<Object> json = new DslJson<Object>();
					writer = json.newWriter();
					json.serialize(writer, mapJsonData);
					String msg = writer.toString();
					logger.debug(">>>>>>>"+msg);
					TextMessage message = session.createTextMessage(msg);
					producer.send(message);
					mapJsonData.clear();
				}
			}
		} catch (Exception e) {
			logger.error("doSend map data error", e);
		}finally {
			if(writer!=null){
				writer.reset();
			}			
		}
	}
	
	private void doSend(){
		JsonWriter writer = null;
		try{
			synchronized(listJsonData){
				if(listJsonData.size()>0){
					DslJson<Object> json = new DslJson<Object>();
					writer = json.newWriter();
					json.serialize(writer, listJsonData);
					String msg = writer.toString();
					logger.debug(">>>>>>>"+msg);
					TextMessage message = session.createTextMessage(msg);
					producer.send(message);
					
					listJsonData.clear();
				}				
			}

		} catch (IllegalStateException legalExp){
			try {
				//如果连接断开，尝试重新连接
				reInit();
				this.doSend();
			} catch (Exception reInitExp) {
				logger.error("doSend reInit error", reInitExp);
			}
		} catch (Exception exp){
			logger.error("doSend list error", exp);
		} finally {
			if(writer!=null){
				writer.reset();
			}			
		}
	}
	
	/**显示提交缓存中的数据
	 * @throws Exception
	 */
	public void sendCommit() throws Exception{
		doSend();
		
		//session.commit();
	}
}
