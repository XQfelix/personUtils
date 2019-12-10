package com.common.mq.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractReceiver implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(AbstractReceiver.class);
	
	private int MAX_QUEUE_SIZE = 100000;
	
	protected BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>(MAX_QUEUE_SIZE);
	
	public Object getData() {
		try {
			Object data = queue.take();
			logger.debug("take que, queue size=" + queue.size());
			return data;
		} catch (Exception exp) {
			logger.error("getData error", exp);
			return null;
		}
	}
	
	protected void putData(Object data) {
		try {
			queue.put(data); 
			logger.debug("put que, queue size=" + queue.size());
		} catch (Exception exp) {
			logger.error("putData error", exp);
		}
	}
	
	public int getDataSize(){
		int size = queue.size();
		return size;
	}
}
