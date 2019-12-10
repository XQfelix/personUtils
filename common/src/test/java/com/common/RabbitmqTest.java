package com.common;

import com.common.mq.rabbitmq.RabbitMQProducer;
import com.common.mq.receiver.RabbitMqReceiver;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/10 13:58
 */
public class RabbitmqTest {
    private static final Logger logger = LoggerFactory.getLogger(RabbitmqTest.class);
    @Test
    public void cusmor(){
        RabbitMqReceiver rqr = RabbitMqReceiver.getInstance("192.168.1.162", "test", "test", "rmq");
	    Thread t1 = new Thread(rqr);
	    t1.start();

        Thread t2 = new Thread(rqr);
        t2.start();

        Thread t3 = new Thread(rqr);
        t3.start();

        Thread t4 = new Thread(rqr);
        t4.start();

        while(true){
			Object data = rqr.getData();
			logger.info("getdata>>>"+(data));
		}
    }

    @Test
    public void producer() throws IOException, TimeoutException, InterruptedException {
        RabbitMQProducer RMQ = new RabbitMQProducer("192.168.1.162", "test", "test");
        List<Map<String, Object>> sendList = new ArrayList<Map<String,Object>>();
        for (int j = 0; j <10 ; j++) {
            for (int i = 0; i < 100000; i++) {
                Map<String, Object> sendMap = new HashMap<String, Object>();
                sendMap.put("hello"+i, "world"+i);
//                sendList.add(sendMap);
                RMQ.sendForSimpleQueue("rmq", sendMap);
            }

//            Thread.sleep(3000);
        }
    }

}
