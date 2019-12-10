package com.common.mq.kafka;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class KafkaUtil {
	private static Logger logger = LoggerFactory.getLogger(KafkaUtil.class);
	
	private static KafkaUtil kafkaUtil;
	
	private static KafkaProducer<String, String> producer;
 
	//默认10秒
	private int delay=2;
	
	private String topic;
	
	@SuppressWarnings("rawtypes")
	private static List<Map> listJsonData = Collections.synchronizedList(new ArrayList<Map>());
    
	
	/**
	 * 
	 * @param hosts 服务器ip:端口号，集群用逗号分隔
	 * @param topic 消息主题
	 * @param delay 最晚提交时间秒
	 * @return
	 * @throws Exception
	 */
    public static KafkaUtil getInstance(String hosts,String topic,int delay) throws Exception{
		if(kafkaUtil==null){
			kafkaUtil = new KafkaUtil(hosts,topic,delay);
		}		
		return kafkaUtil;
	}
    
	
    private KafkaUtil(String hosts, String topic, int delay) {
	   createProducer(hosts,topic,delay);
    }


  /**
     * kafka生产者不能够从代码上生成主题，只有在服务器上用命令生成
     * 创建Kafka生产者
     * 
     * @return KafkaProducer
     */
    private void createProducer(String hosts,String topic,int delay) {
       Properties properties = new Properties();
       //服务器ip:端口号，集群用逗号分隔，配置多个地址，防止down机  9092
       properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
       properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
       properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
       producer = new KafkaProducer<String, String>((properties));
       
       if(delay>0){
			this.delay = delay;
	   }		
       
       this.topic = topic;
       
       Runnable runnable = new Runnable() {
	        public void run() {
	        	try {
					sendCommit();
				} catch (Exception exp) {
					logger.error("scheduled sendCommit error", exp);
				}
	        }
	    };
	    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	    // 参数：1、任务体 2、首次执行的延时时间 
	    //      3、任务执行间隔 4、间隔时间单位
	    service.scheduleAtFixedRate(runnable, 0, this.delay, TimeUnit.SECONDS);
    }

    
    /**
     * 显示提交缓存中的数据
	 * @throws Exception
	 */
	public void sendCommit() throws Exception{
		 doSend();
	}
	
	
	private void doSend(){
		JsonWriter writer = null;
		try{
			if(listJsonData.size()>0){
				synchronized(listJsonData){
					if(listJsonData.size()>0){
						DslJson<Object> json = new DslJson<Object>();
						writer = json.newWriter();
						json.serialize(writer, listJsonData);
						String msg = writer.toString();
						logger.debug(">>>>>>>"+msg);
						producer.send(new ProducerRecord<String, String>(topic, msg));
						
						listJsonData.clear();
					}
				}
			}
		} catch (Exception exp){
			logger.error("doSend error", exp);
		} finally {
			if(writer!=null){
				writer.reset();
			}			
		}
	}
	
	
	/**分批次往kafka MQ发送数据
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
	
	
	/**分批次往kafka MQ发送数据
	 * @param mapData 待发送数据，Map数据中不支持对象
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
     * 
     * 传入kafka约定的topicName,json格式字符串，发送给kafka集群
     * 
     * @param topicName
     * @param jsonMessage
     */
    public void sendMessage( String jsonMessage) {
        producer.send(new ProducerRecord<String, String>(topic, jsonMessage));
//        producer.close();
    }
    
  /**
     * 
     * 传入kafka约定的topicName,json格式字符串数组，发送给kafka集群
     * 用于批量发送消息，性能较高。
     * 
     * @param topicName
     * @param jsonMessages
     * @throws InterruptedException
     */
	 public void sendMessage( String... jsonMessages) throws InterruptedException {
		if(jsonMessages!=null&&jsonMessages.length>0){
	        for (String jsonMessage : jsonMessages) {
	           producer.send(new ProducerRecord<String, String>(topic, jsonMessage));
	        }
		}else{
			logger.error("发送消息数据为空:"+jsonMessages);
		}
//	        producer.close();
    }


	 
	 public static void main(String[] args) throws InterruptedException {
//	       String[] s = new String[] { "{\"userName\":\"赵四31\",\"pwd\":\"lisi\",\"age\":13}",
//	 "{\"userName\":\"赵四41\",\"pwd\":\"lisi\",\"age\":14}",
//	 "{\"userName\":\"赵四51\",\"pwd\":\"lisi\",\"age\":15}" };
	        try {
				KafkaUtil kafkaUtil = KafkaUtil.getInstance("192.168.222.135:9092", "jiangjun", 2);
				Map<String,Object> mapData = new HashMap<>();
				mapData.put("key1", "value1");
				kafkaUtil.send(mapData, 1);
				
//				kafkaUtil.sendMessage("测试kafka数据！！！");
//				kafkaUtil.sendMessage( s);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
    }
	 
	 

}