package com.common.util;



import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlUtil {
	
	private static Logger logger = LoggerFactory.getLogger(XmlUtil.class);
	
	
	/**
	 * xml格式文件转JSON
	 * @param xmlStr xml格式的字符串 <a>hello world</a>
	 */
	public static String xmlToJson(String xmlStr) {
		String jsonStr = "";
		try {
			if(!xmlStr.trim().equals("")) {
				XMLSerializer xmlSerializer = new XMLSerializer();
				JSON dataJSON = xmlSerializer.read(xmlStr);
				jsonStr = dataJSON.toString();
			} else {
				logger.info("传入的xml数据为空!!");
			}
			
		} catch (Exception e) {
			logger.info("xml数据转换出错: " + e);
		}
		return jsonStr;
	}
}
