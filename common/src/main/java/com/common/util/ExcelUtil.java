package com.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class ExcelUtil {

	private static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

	public static Workbook getWorkbook(String filePath) throws Exception {
		Workbook wb = null;
		InputStream is = null;
		try {
			File file = new File(filePath);
			is = new FileInputStream(file);
			if (filePath.endsWith(".xls")) {
				wb = new HSSFWorkbook(is);
			} else if (filePath.endsWith(".xlsx")) {
				wb = new XSSFWorkbook(is);
			} else {
				logger.error("The Wrong file type！");
			}
		} finally {
			if (is != null)
				is.close();
		}
		return wb;
	}

	/**
	 * 读取Excel文件
	 *
	 * @param filePath 文件路径
	 * @return
	 * @throws Exception
	 */
	public static Map<String, List<Map<String, String>>> importExcle(String filePath) throws Exception {

		Map<String, List<Map<String, String>>> result = new HashMap<String, List<Map<String, String>>>();
		Workbook wb = getWorkbook(filePath);
		if (wb == null)
			return result;
		int sheetSize = wb.getNumberOfSheets();
		for (int i = 0; i < sheetSize; i++) {
			Sheet sheet = wb.getSheetAt(i);
			String sheetName = sheet.getSheetName();
			List<Map<String, String>> sheetList = new ArrayList<Map<String, String>>();// 对应sheet页
			List<String> titles = new ArrayList<String>();

			int rowSize = sheet.getLastRowNum() + 1;
			for (int j = 0; j < rowSize; j++) {// 遍历行
				Row row = sheet.getRow(j);
				if (row == null) {// 略过空行
					continue;
				}
				int cellSize = row.getLastCellNum();// 行中有多少个单元格，也就是有多少列
				if (j == 0) {// 第一行是标题行
					for (int k = 0; k < cellSize; k++) {
						String cellValue = "";
						Cell cell = row.getCell(k);// getCell(k);
						if (cell != null) {
							cellValue = cell.toString().trim();
						}
						titles.add(cellValue);
					}
				} else {// 其他行是数据行
					Map<String, String> rowMap = new HashMap<String, String>();// 对应一个数据行
					for (int k = 0; k < titles.size(); k++) {
						Cell cell = row.getCell(k);
						String key = titles.get(k);
						String value = "";
						if (cell != null)
							value = cell.toString().trim();
						if (value != null && value.length() > 0)
							rowMap.put(key, value);
					}
					if (rowMap != null && rowMap.size() > 0)
						sheetList.add(rowMap);
				}
			}
			result.put(sheetName, sheetList);
		}
		return result;
	}

	/**
	 * 根据sheet页名称读取Excel文件
	 *
	 * @param filePath
	 * @param sheetName
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, String>> importExcleBySheetName(String filePath, String sheetName) throws Exception {
		List<Map<String, String>> ls = new ArrayList<Map<String, String>>();
		Map<String, List<Map<String, String>>> map = importExcle(filePath);
		if (map != null && map.size() > 0) {
			if (map.containsKey(sheetName)) {
				ls = map.get(sheetName);
			} else {
				logger.info("The sheet name \"" + sheetName + "\" is not exist!");
			}
		}
		return ls;
	}


	/**
	 * 读取指定csv文件
	 * @param filePath 文件全路径 G:\\test.csv
	 */
	public static List<Map<String, String>> readCsvFile(String filePath) {

		ArrayList<Map<String, String>> retList = new ArrayList<Map<String,String>>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String title = reader.readLine();
			if(!title.trim().equals("")) {
				String[] titleArr = title.split(",");//标题行
				String line = null;
				while((line=reader.readLine())!=null){
					if(!line.trim().equals("")) {//跳出空行
						String item[] = line.split(",");//分隔每个字段
						for(int i=0; i<titleArr.length; i++) {
							HashMap<String, String> singleMap = new HashMap<String, String>();
							if(!titleArr[i].trim().equals("")) {
								String key = titleArr[i].trim();
								String value = i < item.length ? item[i].trim() : "";
								singleMap.put(key, value);
								retList.add(singleMap);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("读取CSV文件异常: " + e);
		}
		return retList;
	}

	public static List<Map<String, Object>> getCSV2Map(String filePath, String separation) {
		List<Map<String, Object>> list = new ArrayList<>();
		List<Map<String, String>> datas = readCsvFile(filePath);
		if (datas != null && datas.size() > 0) {
			for (Map<String, String> data : datas) {
				Iterator<Map.Entry<String,String>> iter = data.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String,String> entry = iter.next();
					String key = entry.getKey();
					String value = entry.getValue();
					String[] keys = key.split(separation);
					String[] values = value.split(separation);
					int vLength = values.length;
					Map<String, Object> map = new HashMap<>();
					for (int i = 0; i < keys.length; i++) {
						map.put(keys[i], values[i]);
						if ((i+1) == vLength) {
							break;
						}
					}
					list.add(map);
				}
			}
		}
		return list;
	}

	public static void writeExcel(List<Map<String, Object>> dataList, Map<String, Integer> nameToColumn, String finalXlsxPath, String viewType){
		OutputStream out = null;
		try {
			Workbook workBook = getWorkbook(finalXlsxPath);
			// sheet 对应一个工作页
			Sheet sheet = workBook.getSheetAt(0);
			/**
			 * 删除原有数据，除了属性列
			 */
			int rowNumber = sheet.getLastRowNum();    // 第一行从0开始算
			//System.out.println("原始数据总行数，除属性列：" + rowNumber);
			for (int i = 1; i <= rowNumber; i++) {
				Row row = sheet.getRow(i);
				if(row != null && (viewType == null || viewType.equals("") || row.getCell(nameToColumn.get("viewType")).toString().equals(viewType))){
					sheet.removeRow(row);
				}
			}

			/**
			 * 往Excel中写新数据
			 */
			int insertIndex = 0;
			for (int j = 0; j < dataList.size(); j++) {
				// 创建一行：从第二行开始，跳过属性列
				insertIndex++;
				//遍历直到没有存有信息的那一行，用来存储新的数据
				while(sheet.getRow(insertIndex) != null){
					insertIndex++;
				}
				Row row = sheet.createRow(insertIndex);

				// 得到要插入的每一条记录
				Map<String, Object> dataMap = dataList.get(j);
				for(String key:dataMap.keySet()){
					Cell cell = row.createCell(nameToColumn.get(key));
					cell.setCellValue(dataMap.get(key).toString());
				}
			}
			// 创建文件输出流，输出电子表格：这个必须有，否则你在sheet上做的任何操作都不会有效
			out =  new FileOutputStream(finalXlsxPath);
			workBook.write(out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				if(out != null){
					out.flush();
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//System.out.println("数据导出成功");
	}

	public static void main(String[] args) {
		/*String filePath = "G:/YZYWLSB_ZHZY_DAY_20190819 (1).csv";
		String separation = "\\|";
		JSONArray arr = JSONArray.parseArray(JSON.toJSONString(ExcelUtil.getCSV2Map(filePath, separation)));
		System.out.println(arr);*/

		Map<String, Integer> nameToColumn = new HashMap<>();
		/*nameToColumn.put("SourceCIName", 0);
		nameToColumn.put("SourceEventID", 0);
		nameToColumn.put("SourceIdentifier", 0);
		nameToColumn.put("SourceID", 0);
		nameToColumn.put("SourceAlertKey", 0);
		nameToColumn.put("SourceSeverity", 0);
		nameToColumn.put("Severity", 0);
		nameToColumn.put("LastOccurrence", 0);
		nameToColumn.put("Summary", 0);
		nameToColumn.put("Status", 0);*/
		nameToColumn.put("BankName", 0);
		nameToColumn.put("Addr", 1);
		nameToColumn.put("Phone", 2);

		List<Map<String, Object>> datas = new ArrayList<>();
		Map<String, Object> data = new HashMap<>();
		data.put("BankName", "ChinaBank4");
		data.put("Addr", "Beijing4");
		data.put("Phone", "4");
		datas.add(data);
		Map<String, Object> data1 = new HashMap<>();
		data1.put("BankName", "ChinaBank5");
		data1.put("Addr", "Beijing5");
		data1.put("Phone", "5");
		datas.add(data1);
		writeExcel(datas, nameToColumn, "C:\\Users\\uinnova\\Desktop\\锐捷补丁\\test.xlsx", "Beijing6");
		try {
			System.out.println(importExcleBySheetName("C:\\Users\\uinnova\\Desktop\\锐捷补丁\\test.xlsx","test"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
