package com.common.mq.receiver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Enumeration;

public class ActivemqReceiver extends AbstractReceiver implements MessageListener {

	private static Logger logger = LoggerFactory.getLogger(ActivemqReceiver.class);
	
	private String url;
	private String queue;
	private String username;
	private String password;
	private Boolean queueOrTopic;
	
	private Connection connection = null;
	private Session session = null;
	private MessageConsumer consumer = null;
	
	public ActivemqReceiver(String url, String queue, String username, String password, Boolean queueOrTopic) {
		this.url = url;
		this.queue = queue;
		this.username = username;
		this.password = password;
		this.queueOrTopic = queueOrTopic;
	}

	@Override
	public void run() {
		try{
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(username, password, url);
			connection = connectionFactory.createConnection();
			session = connection.createSession(false , Session.AUTO_ACKNOWLEDGE);
			Destination destination = queueOrTopic ? session.createQueue(queue) : session.createTopic(queue);
			consumer = session.createConsumer(destination);
			connection.start();
			consumer.setMessageListener(this);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void onMessage(Message message) {
		logger.debug("The message type" + message.getClass().getName());
		try{
			if (message instanceof TextMessage) {
				TextMessage txtMsg = (TextMessage) message;
				String msg = txtMsg.getText();
				this.putData(msg);
			} else if (message instanceof BytesMessage) {
	            BytesMessage bytesMsg = (BytesMessage) message;
	            long bodyLength = bytesMsg.getBodyLength();
	            byte[] b = new byte[(int) bodyLength];
	            bytesMsg.readBytes(b);
	            this.putData(new String(b,"utf-8"));
			} else if (message instanceof ObjectMessage) {
				ObjectMessage om = (ObjectMessage) message;
				this.putData(JSON.toJSON(om.getObject()));
			} else if(message instanceof MapMessage) {
				MapMessage map = (MapMessage) message;
				Enumeration mapNames = map.getMapNames();
				JSONObject data = new JSONObject();
				while(mapNames.hasMoreElements()) {
					String name = mapNames.nextElement().toString();
					data.put(name, map.getObject(name));
				}
				this.putData(data.toJSONString());
			} else if(message instanceof StreamMessage){
				StreamMessage stream = (StreamMessage) message;
				Object readObject = null;
				try {
					while((readObject = stream.readObject()) != null) {
						this.putData(readObject);
					}
				} catch (Exception e) {
				}
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}
}
