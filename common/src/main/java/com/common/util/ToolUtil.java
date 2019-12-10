package com.common.util;


import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Pattern;

public class ToolUtil {
	private static Logger log = LoggerFactory.getLogger(ToolUtil.class);
	private static String os = System.getProperties().getProperty("os.name");
	/**
	 * 读取文件内容
	 * @param filename
	 * @param charset
	 * @return
	 */
	public static String readFileContent(String filename, String charset){
		if (charset == null || charset.length() < 1) {
			charset = "utf-8"; 
		}
		InputStream fis=null;
		try{
			fis=new FileInputStream(new File(filename));
			byte[] buff = new byte[1024*1024];
			List<Byte> list = new ArrayList<Byte>();
			while(true){
				int index = fis.read(buff);
				if(index>=0){
					for(int i=0;i<index;i++){
						list.add(Byte.valueOf(buff[i]));
					}
					buff = new byte[1024*1024];
				}else{
					break;
				}
			}
			byte[] buff_final = new byte[list.size()];
			for(int i=0;i<list.size();i++){
				buff_final[i] = list.get(i).byteValue();
			}
			return new String(buff_final, charset).trim();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			if(fis!=null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			fis = null;
		}
	}
	
	/**
	 * 根据txt文件的绝对路径，读取文件内容
	 * 
	 * @param filePath
	 * @return 文件内容的List对象
	 */
	public static List<String> readTxtFile(String filePath, String charset) {
		
		if (charset == null || charset.length() < 1) {
			charset = "utf-8"; 
		}
		List<String> lineList = new ArrayList<String>();
		try {
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), charset);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (lineTxt.length() > 0) {
						lineList.add(lineTxt.substring(0,lineTxt.lastIndexOf(";")));
					}
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		return lineList;
	}
	
	/**
	 * 读取JSONObject格式的数据
	 * 
	 * @param filename 文件全路径
	 * @param charset 字符集编码，默认为utf-8
	 * @return 
	 */
	public static JSONObject getJSONFile(String filename, String charset){
		
		if (charset == null || charset.length() < 1) {
			charset = "utf-8"; 
		}
		if (os.toLowerCase().startsWith("win")) {
			charset = "gbk";      //针对在win环境下, 读取到的dip配置中文乱码添加
//			charset = "utf-8";     //针对在win环境下, 读取到的dip配置中文乱码添加
		}
		InputStream fis=null;
		try{
			fis = new FileInputStream(new File(filename));
			byte[] buff = new byte[1024*1024];
			List<Byte> list = new ArrayList<Byte>();
			while(true){
				int index = fis.read(buff);
				if(index>=0){
					for(int i=0;i<index;i++){
						list.add(Byte.valueOf(buff[i]));
					}
					buff = new byte[1024*1024];
				}else{
					break;
				}
			}
			byte[] buff_final = new byte[list.size()];
			for(int i=0;i<list.size();i++){
				buff_final[i] = list.get(i).byteValue();
			}
			JSONObject jsObj = JSONObject.fromObject(new String(buff_final, charset).trim());
			return jsObj;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			if(fis!=null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			fis = null;
		}
	}
	
	/**
	 * 读取JSONArray格式的数据
	 * 
	 * @param filename
	 * @param charset
	 * @return
	 */
	public static JSONArray getJSONArrayFile(String filename, String charset){
		
		if (charset == null || charset.length() < 1) {
			charset = "utf-8"; 
		}
		
		InputStream fis=null;
		try{
			byte[] buff = new byte[1024*1024];
			List<Byte> list = new ArrayList<Byte>();
			fis = new FileInputStream(filename);
			while(true){
				int index = fis.read(buff);
				if(index>=0){
					for(int i=0;i<index;i++){
						list.add(Byte.valueOf(buff[i]));
					}
					buff = new byte[1024*1024];
				}else{
					break;
				}
			}
			byte[] buff_final = new byte[list.size()];
			for(int i=0;i<list.size();i++){
				buff_final[i] = list.get(i).byteValue();
			}
			JSONArray jsObj = JSONArray.fromObject(new String(buff_final, charset).trim());
			return jsObj;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			if(fis!=null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			fis = null;
		}
	}

	/**
	 * 将数据写入制定的文件中
	 * 
	 * @param filename
	 * @param content
	 */
	public static void setJSONFile(String filename, String content){
		FileOutputStream fos = null;
		try{
			File file = new File(filename);
			if(file.isFile()!=true||file.exists()!=true){
				file.createNewFile();
			}
			fos=new FileOutputStream(file.getPath());
			fos.write(content.getBytes("utf-8"));
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(fos!=null){
				try {
					fos.close();
				} catch (IOException e) {
					log.error("写入文件内容出错，错误信息为：" + e);
				}
			}
			fos = null;
		}
	}
	
	
	/**
	 * 数字的四舍五入，并保留小数位
	 * 
	 * @param numberStr
	 * @param decimalDigit
	 * @return 返回四舍五入之后的数字
	 */
	public static Object roudNumber (Object numberStr, int decimalDigit) {
		Object retVal = new Object();
		NumberFormat nf = NumberFormat.getNumberInstance();   
        nf.setMaximumFractionDigits(decimalDigit);
		retVal = nf.format(Double.parseDouble(String.valueOf(numberStr)));
		return retVal;
	}
	
	/**
	 * 正则判断字符串是否是整数，小数，包括负数
	 * 
	 * @param str
	 * @return boolean
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("^\\d+$|-\\d+$"); // 就是判断是否为整数
		Pattern pattern2 = Pattern.compile("\\d+\\.\\d+$|-\\d+\\.\\d+$");// 判断是否为小数
		return pattern.matcher(str).matches()
				|| pattern2.matcher(str).matches();
	}
	
	/**
	 * 集合升序排序
	 * 
	 * @param datas
	 * @param sortAttr
	 * @return
	 */
	public static List<Map<String, Object>> collectionsSortAsc (List<Map<String, Object>> datas, final String sortAttr) {
		Collections.sort(datas, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				Long name1 = Long.valueOf(String.valueOf(o1.get(sortAttr))) ;//name1是从你list里面拿出来的一个 
				Long name2 = Long.valueOf(String.valueOf(o2.get(sortAttr))) ; //name1是从你list里面拿出来的第二个name
                return name1.compareTo(name2);
			}
		});
		return datas;
	}
	
	/**
	 * 集合倒序排序
	 * 
	 * @param datas
	 * @param sortAttr
	 * @return
	 */
	public static List<Map<String, Object>> collectionsSortDesc (List<Map<String, Object>> datas, final String sortAttr) {
		Collections.sort(datas, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				Long name1 = Long.valueOf(String.valueOf(o1.get(sortAttr))) ;//name1是从你list里面拿出来的一个 
				Long name2 = Long.valueOf(String.valueOf(o2.get(sortAttr))) ; //name1是从你list里面拿出来的第二个name
                return name2.compareTo(name1);
			}
		});
		return datas;
	}
	
	public static String xml2json(String xmlString) {
        XMLSerializer xmlSerializer = new XMLSerializer();
        JSON json = xmlSerializer.read(xmlString);
        return json.toString();
    }
	
	/**
	 * 
	 * @param folderPath 遍历的目录路径
	 * @param charset
	 * @return
	 */
	public static List<Object> loopFolder(String folderPath, String charset){
		ArrayList<Object> res = new ArrayList<>();
		File file = new File(folderPath);
		File[] fileList = file.listFiles();
		for(int i = 0; i < fileList.length; i++) {
			if(fileList[i].isFile()) {
				String fileName = fileList[i].getName();
				String readFileContent = readFileContent(folderPath + "/" + fileName, charset);
				res.add(readFileContent);
			}else if(fileList[i].isDirectory()) {
				String tempFolderPath = folderPath + "/" + fileList[i].getName();
				List<Object> loopFolder = loopFolder(tempFolderPath, charset);
				res.addAll(loopFolder);
			}
		}
		return res;
	}
	
	/***
	 * 测试主函数
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
//		Map<String, Integer> m = new HashMap<>();
//		m.put("a", 1);
//		m.put("c", 2);
//		System.out.println(m.get("d") == null);
//		
//		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//        Map<String, Object> map1 = new HashMap<String, Object>();
//        map1.put("name", "p");
//        map1.put("cj", "5");
//        Map<String, Object> map2 = new HashMap<String, Object>();
//        map2.put("name", "h");
//        map2.put("cj", "12");
//        Map<String, Object> map3 = new HashMap<String, Object>();
//        map3.put("name", "f");
//        map3.put("cj", "31");
//        Map<String, Object> map4 = new HashMap<String, Object>();
//        map4.put("name", "ss");
//        map4.put("cj", "1");
//        list.add(map1);
//        list.add(map3);
//        list.add(map2);
//        list.add(map4);
//        //排序前 
//        for (Map<String, Object> map : list) {
//            System.out.println(map.get("cj"));
//        }
//        System.out.println("=======================");
//        list = ToolUtil.collectionsSort(list, "cj");
//        for (Map<String, Object> map : list) {
//            System.out.println(map.get("cj"));
//        }
        
		String xmlStr = readFileContent("/Users/lijunfeng/Documents/ansible-awx-20190101235610.xml", null);
		System.out.println(xml2json(xmlStr));
	}
}
