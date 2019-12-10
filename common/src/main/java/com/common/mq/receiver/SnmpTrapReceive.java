package com.common.mq.receiver;

import com.alibaba.fastjson.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import java.io.IOException;
import java.util.Vector;

public class SnmpTrapReceive extends AbstractReceiver implements CommandResponder {
	
	private static Logger logger = LoggerFactory.getLogger(SnmpTrapReceive.class);
	private MultiThreadedMessageDispatcher dispatcher;
	private Snmp snmp = null;
	private Address listenAddress;
	private ThreadPool threadPool;
	private String ip;
	private String port;
	private String protocol;
	
	public SnmpTrapReceive(String ip, String port, String protocol) {
		this.ip = ip;
		this.port = port;
		this.protocol = protocol;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void init(){
		threadPool = ThreadPool.create("Trap", 2);
		dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
		listenAddress = GenericAddress.parse(System.getProperty("snmp4j.listenAddress", protocol + ":" + ip + "/" + port)); // 本地IP与监听端口
		TransportMapping transport = null;
		// 对TCP与UDP协议进行处理
		if (listenAddress instanceof UdpAddress) {
			try {
				transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			try {
				transport = new DefaultTcpTransportMapping((TcpAddress) listenAddress);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		snmp = new Snmp(dispatcher, transport);
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		try {
			snmp.listen();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public void processPdu(CommandResponderEvent event) {
		Vector<VariableBinding> recVBs = (Vector<VariableBinding>) event.getPDU().getVariableBindings();
		logger.debug("获取Trap原始数据: " + recVBs);
		JSONArray ret = new JSONArray();
		for(int i=0; i<recVBs.size(); i++) {
			JSONObject obj = new JSONObject();
			VariableBinding recVB = recVBs.elementAt(i);
			obj.put(recVB.getOid(), recVB.getVariable());
			ret.add(obj);
		}
		this.putData(ret);
	}
	
	@Override
	public void run() {
		try {
			init();
			snmp.addCommandResponder(this);
			logger.info("--------------->start Trap module!<---------------");
		} catch (Exception ex) {
			logger.error("start Trap module fail :" , ex);
		}
	}

}
