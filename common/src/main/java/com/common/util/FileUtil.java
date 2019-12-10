package com.common.util;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import net.sf.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FileUtil {
	
	public static String readFile (String filePath) {
		return readFile(filePath, "utf-8");
	}

	public static String readFile(String filePath, String charset) {

		if (charset == null || charset.length() < 1) {
			charset = "utf-8";
		}

		FileInputStream fis = null;
		try {
			byte[] buff = new byte[1024 * 1024];
			List<Byte> list = new ArrayList<Byte>();
			fis = new FileInputStream(filePath);
			while (true) {
				int index = fis.read(buff);
				if (index >= 0) {
					for (int i = 0; i < index; i++) {
						list.add(Byte.valueOf(buff[i]));
					}
					buff = new byte[1024 * 1024];
				} else {
					break;
				}
			}
			byte[] buff_final = new byte[list.size()];
			for (int i = 0; i < list.size(); i++) {
				buff_final[i] = list.get(i).byteValue();
			}
			return new String(buff_final, charset).trim();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			fis = null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		System.out.println(new JSONObject());
		System.out.println(FileUtil.readFile("/Users/lijunfeng/Documents/dmcode.txt", null));
		DslJson<Map<String, Object>> dslJson = new DslJson<Map<String, Object>>();
		try {
			String paramStr = FileUtil.readFile("/Users/lijunfeng/Desktop/xbrother参数配置.txt");
			byte[] buff = paramStr.getBytes("UTF-8");
			JsonReader<Map<String, Object>> jsonReader = dslJson.newReader(buff);
			Map<String, Object> params = jsonReader.next(Map.class);
			for (String key : params.keySet()) {
				System.out.println(params.get(key));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
