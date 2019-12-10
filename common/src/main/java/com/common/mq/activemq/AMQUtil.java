package com.common.mq.activemq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AMQUtil {
	
	private static final Map<String, AMQProducer> AMP_PRODUCER_MAP = new ConcurrentHashMap<String, AMQProducer>();
	
	/**获取MQ发送对象
	 * 默认用户名，密码，默认为Queue的方式，默认为非持久化
	 * @param amqUrl MQ URL
	 * @param topicOrQueue Topic或者Queue名称
	 * @return 
	 * @throws Exception 
	 */
	public static AMQProducer getAMQProducer(String amqUrl,String topicOrQueue, String name) throws Exception{
		AMQProducer amqProducer = AMP_PRODUCER_MAP.get(name);
		if(amqProducer==null){
			amqProducer = new AMQProducer(amqUrl,topicOrQueue);
			if (amqProducer != null) {
				AMP_PRODUCER_MAP.put(name, amqProducer);
			}
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
	public static AMQProducer getAMQProducer(String amqUrl,String topicOrQueue,int delay, String name) throws Exception{
		AMQProducer amqProducer = AMP_PRODUCER_MAP.get(name);
		if(amqProducer==null){
			amqProducer = new AMQProducer(amqUrl,topicOrQueue,delay);
			if (amqProducer != null) {
				AMP_PRODUCER_MAP.put(name, amqProducer);
			}
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
	public static AMQProducer getAMQProducer(String amqUrl,int chanelMode,String topicOrQueue,int delay, String name) throws Exception{
		AMQProducer amqProducer = AMP_PRODUCER_MAP.get(name);
		if(amqProducer==null){
			amqProducer = new AMQProducer(amqUrl,chanelMode,topicOrQueue,delay);
			if (amqProducer != null) {
				AMP_PRODUCER_MAP.put(name, amqProducer);
			}
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
	public static AMQProducer getAMQProducer(String amqUrl,int chanelMode,String topicOrQueue,int deliveryMode,int delay, String name) throws Exception{
		AMQProducer amqProducer = AMP_PRODUCER_MAP.get(name);
		if(amqProducer==null){
			amqProducer = new AMQProducer(amqUrl,chanelMode,topicOrQueue,deliveryMode,delay);
			if (amqProducer != null) {
				AMP_PRODUCER_MAP.put(name, amqProducer);
			}
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
	public static AMQProducer getAMQProducer(String amqUrl,String user,String password,
			int chanelMode, String topicOrQueue,int deliveryMode,int delay, String name) throws Exception{
		AMQProducer amqProducer = AMP_PRODUCER_MAP.get(name);
		if(amqProducer==null){
			amqProducer = new AMQProducer(amqUrl,user,password,chanelMode,topicOrQueue,deliveryMode,delay);
			if (amqProducer != null) {
				AMP_PRODUCER_MAP.put(name, amqProducer);
			}
		}		
		return amqProducer;
	}

	
	public static void main(String[] args) {
		try {
			AMQProducer amqProducer = AMQUtil.getAMQProducer("tcp://192.168.1.195:61616", "test11", "a");
			AMQProducer amqProducer1 = AMQUtil.getAMQProducer("tcp://192.168.1.195:61616", "test21", "b");
			
			AMQProducer a = AMQProducer.getInstance("tcp://192.168.1.195:61616", "test111");
			AMQProducer b = AMQProducer.getInstance("tcp://192.168.1.195:61616", "test112");
			
			
			for (int j = 0; j < 100; j++) {
				List<Map<String, Object>> list1 = new ArrayList<>();
				List<Map<String, Object>> list2 = new ArrayList<>();
				for (int i = 0; i < 10; i++) {
					Map<String, Object> m = new HashMap<>();
					m.put("a", "a" + i);
					m.put("b", "b" + i);
					m.put("c", "c" + i);
					m.put("d", "d" + i);
					m.put("e", "e" + i);
					m.put("f", "f" + i);
					m.put("g", "g" + i);
//					amqProducer.sendMap(m);
					list1.add(m);
					Map<String, Object> m2 = new HashMap<>();
					m2.put("A", "a" + i);
					m2.put("B", "b" + i);
					m2.put("C", "c" + i);
					m2.put("D", "d" + i);
					m2.put("E", "e" + i);
					m2.put("F", "f" + i);
					m2.put("G", "g" + i);
//					amqProducer1.sendMap(m2);
					list2.add(m2);
				}
				a.sendList(list1, 10);
				b.sendList(list2, 10);
				amqProducer.sendList(list1, 1);
				amqProducer1.sendList(list2, 1);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
