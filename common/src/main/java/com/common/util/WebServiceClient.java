package com.common.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @Title 获取webservice数据
 * */
public class WebServiceClient {
	private static Logger logger = LoggerFactory.getLogger(WebServiceClient.class);
	/**
	 * 通过axis方式请求调用webservice
	 * @param wsdl 请求的wsdl地址（不包含?wsdl）
	 * @param tns 请求的的wsdl的命名空间（类似于http://service.ws.demo.ws.uinv.com）
	 * @param method 请求的webservice的具体方法
	 * @param tag 请求wsdl时必须指定的命名空间及方法前的解析标识
	 * @param headMtd 请求头
	 * @param headerparam 请求头参数
	 * @return json String
	 * @throws Exception
	 * */
	public static String sendAxis2(String wsdl, String tns, String tag, String method, String bodyparam, String headMtd, String headerparam){
		String res = null;
		tag = tag + ":";
		JSONObject bodyparams = JSONObject.parseObject(bodyparam);
		//JSONObject bodyObj = null;
		//JSONObject headerObj = null;
		Options option = new Options();
		EndpointReference targetEPR = new EndpointReference(wsdl);
		option.setTo(targetEPR);
		option.setAction(tns + method);
		ServiceClient sender;
		try {
			sender = new ServiceClient();
			sender.setOptions(option);
			OMFactory fac = OMAbstractFactory.getOMFactory();
			OMNamespace omNs = fac.createOMNamespace(tns, "");
			OMElement mtd = fac.createOMElement(method, omNs);
			if(bodyparams!=null&&bodyparams.size()>0){
				for(String p1:bodyparams.keySet()){
					OMElement param = fac.createOMElement(p1, omNs);
					param.setText(bodyparams.get(p1).toString());
					mtd.addChild(param);	
				}
			}
			mtd.build();
		
			//添加header验证	
			if(headerparam!=null&&headerparam.length()>0){
				addValidation(sender, tns, headMtd, headerparam);
			}
			OMElement resu = sender.sendReceive(mtd);
			//XML解析为json
//			if(resu.toString().indexOf("<")==0) {
//				XMLSerializer xmlSerializer = new XMLSerializer();
//				net.sf.json.JSON dataJSON = xmlSerializer.read(resu.toString());
//				res = dataJSON.toString();
//			}else if(resu.toString().indexOf("{")==0){
//				res = resu.toString();
//			}
			res = resu.toString();
		} catch (AxisFault e) {
		}
		return res;	
	}
	
	/**
	 * @Title 添加头部认证
	 * @param serviceClient
	 * @param tns(命名空间)
	 * @param hMethod(认证方法名)
	 * */
	public static void addValidation(ServiceClient serviceClient, String tns, String hMethod,String headerparm) {
		JSONObject headers = JSONObject.parseObject(headerparm);
		OMFactory fac = OMAbstractFactory.getOMFactory();
		//命名空间
		OMNamespace omNs = fac.createOMNamespace(tns, "");
		OMElement header = fac.createOMElement(hMethod, omNs);
		if(headers!=null&&headers.size()>0){
			for(String p1:headers.keySet()){
				OMElement param = fac.createOMElement(p1, omNs);
				param.setText(headers.get(p1).toString());
				header.addChild(param);	
			}
		} 		
		serviceClient.addHeader(header);
	}
	
	
	public static void main(String[] args) {
		
//		String wsdl = "http://127.0.0.1:10015/webservice/path";
//		String tns = "http://diXml.com.cn/";
//		String method = "diWebIntegration";	
//		String tag = "dix";
//		String body = "{'jsArr':'河北'}";
//		String data = sendAxis2(wsdl, tns, tag, method, body, null, null);
//		System.out.println(data);
	}
	
}
