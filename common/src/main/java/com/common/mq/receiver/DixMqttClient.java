package com.common.mq.receiver;

import com.alibaba.fastjson.JSONObject;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class DixMqttClient extends AbstractReceiver{

	private MqttClient mqttClient = null;
	private MemoryPersistence memoryPersistence = null;
	private MqttConnectOptions mqttConnectOptions = null;
	
	private String serverURL = null;
	private String clientID = null;
	private String username = null;
	private String password = null;
	private String topic = "";
	private int qos =2;	
	private String recCharSet = "utf-8";
	
	private static Logger logger = LoggerFactory.getLogger(DixMqttClient.class);
		
	private static DixMqttClient dixMqttClient=null;
			
	/**mqtt消息订阅客户端
	 * @param serverURL mqttBroker URL 例:tcp://192.168.1.1:1883
	 * @param clientID 客户端ID
	 * @param username 连接用户名
	 * @param password 连接密码
	 * @param topic 消息订阅主题
	 * @param qos 消息订阅质量
	 * 			QoS=0，至多一次可能会出现丢包的现象;
	 * 			QoS=1，至少一次至少一次，保证包会到达目的地，但是可能出现重包;
	 * 			QoS=2，正好一次，保证包会到达目的地，且不会出现重包的现象;
	 * @param charSet 字符串编码格式,默认：utf-8
	 * @return
	 * @throws MqttException
	 */
	public static DixMqttClient getInstance(String serverURL,String clientID,String username,String password,String topic ,int qos, String charSet) throws MqttException {
		if(dixMqttClient == null) {
			dixMqttClient = new DixMqttClient(serverURL,clientID, username, password,topic,qos,charSet);
		}
		return dixMqttClient;
	}
	
	
	/**mqtt消息发送客户端
	 * @param serverURL mqttBroker URL 例:tcp://192.168.1.1:1883
	 * @param clientID 客户端ID
	 * @param username 连接用户名
	 * @param password 连接密码
	 * @return
	 * @throws MqttException
	 */
	public static DixMqttClient getInstance(String serverURL,String clientID,String username,String password) throws MqttException {
		if(dixMqttClient == null) {
			dixMqttClient = new DixMqttClient(serverURL,clientID, username, password);
		}
		return dixMqttClient;
	}
	
	private  DixMqttClient(String serverURL,String clientID,String username,String password) throws MqttException{
		this.serverURL = serverURL;
		this.clientID = clientID;
		this.username = username;
		this.password = password;
		initConnect();
	}
	
	private  DixMqttClient(String serverURL,String clientID,String username,String password,String topic ,int qos, String charSet) throws MqttException{
		this.serverURL = serverURL;
		this.clientID = clientID;
		this.username = username;
		this.password = password;
		this.topic = topic;
		this.qos = qos;
		this.recCharSet = charSet;
		initConnect();
	}
	
	@Override
	public void run() {
		try {
			this.subscribeMessage(this.topic, this.qos, this.recCharSet);
		} catch (MqttException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**初始化连接
	 * @throws MqttException 
	 */
	private void initConnect() throws MqttException {
		
		//初始化连接设置对象
		mqttConnectOptions = new MqttConnectOptions();		
		mqttConnectOptions.setUserName(username);
		mqttConnectOptions.setPassword(password.toCharArray());
		
		//true可以安全地使用内存持久性作为客户端断开连接时清除的所有状态
		mqttConnectOptions.setCleanSession(true);
		
		//设置连接超时,默认也是30秒
		mqttConnectOptions.setConnectionTimeout(30);
		
		//设置持久化方式
		memoryPersistence = new MemoryPersistence();
		
		//初始化MqttClient
		mqttClient = new MqttClient(serverURL, clientID,memoryPersistence);
		
		//
		if(!mqttClient.isConnected()) {
			//创建回调函数对象
			MqttSendCallback mqttSendCallback = new MqttSendCallback();
			//客户端添加回调函数
			mqttClient.setCallback(mqttSendCallback);
			//创建连接
			mqttClient.connect(mqttConnectOptions);
		}
	}
	
	/**发送消息
	 * @param topic
	 * @param message
	 * @param qos
	 * 			QoS=0，至多一次可能会出现丢包的现象;
	 * 			QoS=1，至少一次至少一次，保证包会到达目的地，但是可能出现重包;
	 * 			QoS=2，正好一次，保证包会到达目的地，且不会出现重包的现象;
	 * @throws MqttException 
	 */
	public boolean publishMessage(String topic,String message,int qos,String charSet) throws MqttException{
		if(null == mqttClient ) {
			initConnect();	
		}
		
		if(!mqttClient.isConnected()) {
			reConnect();
		}
		
		MqttMessage mqttMessage = new MqttMessage();
		mqttMessage.setQos(qos);
		mqttMessage.setPayload(message.getBytes(Charset.forName(charSet)));
		
		MqttTopic mqttTopic = mqttClient.getTopic(topic);		
		if(null != mqttTopic) {
			MqttDeliveryToken publish = mqttTopic.publish(mqttMessage);
			if(publish.getException() == null) {
					return true;
			} else {
				//打印错误日志
				logger.error("",publish.getException());
				return false;
			}							
		}
		
		return false;
	}
	

	/**订阅消息
	 * @param topic
	 * @param qos
	 * @param charSet 编码格式，默认UTF-8
	 * @return
	 * @throws MqttException
	 */
	public boolean subscribeMessage(String topic ,int qos, String charSet) throws MqttException {
		
		if(charSet!=null && !charSet.trim().equals("")) {
			this.recCharSet = charSet;
		}		
		
		if(null == mqttClient ) {
			initConnect();	
		}
		
		if(!mqttClient.isConnected()) {
			reConnect();
		}
		
		mqttClient.subscribe(topic, qos);
		
		return true;
	}
	
	/**关闭连接
	 * @throws MqttException 
	 * 
	 */
	public void closeConnect() throws MqttException {
//		//关闭存储方式
		if(null != memoryPersistence) {
			memoryPersistence.close();
		}
//		
		//关闭连接
		if(null != mqttClient) {
			if(mqttClient.isConnected()) {
				mqttClient.disconnect();
				mqttClient.disconnectForcibly();
//				mqttClient.close();
			}
		}
	}
	
	/**重连接
	 * @throws MqttException 
	 * 
	 */
	public void reConnect() throws MqttException {
		if(null != mqttClient) {
			if(!mqttClient.isConnected()) {
				//创建回调函数对象
				MqttSendCallback mqttSendCallback = new MqttSendCallback();
				//客户端添加回调函数
				mqttClient.setCallback(mqttSendCallback);
				//创建连接
//				mqttClient.reconnect();
				mqttClient.connect(mqttConnectOptions);
			}
		} else {
			initConnect();
		}
	}
		
	
	/**清空主题
	 * @param topic
	 * @throws MqttException 
	 */
	public void cleanTopic(String topic) throws MqttException {
		if(null != mqttClient && !mqttClient.isConnected()) {
			mqttClient.unsubscribe(topic);
		}
	}
	
	private class MqttSendCallback implements MqttCallback {

		@Override
		public void connectionLost(Throwable cause) {
			//重连接
			try {
				dixMqttClient.reConnect();
			} catch (MqttException e) {
				//TODO: 错误日志
			}
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) {
			try {
				String fromTopic = topic;
				int qos = message.getQos();
				String recMessage = new String(message.getPayload(),recCharSet);
								
				JSONObject receivedData = new JSONObject();
				receivedData.put("fromTopic", fromTopic);
				receivedData.put("qos", qos);
				receivedData.put("recData", recMessage);
				
				dixMqttClient.putData(receivedData.toJSONString());				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
