package com.common.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FTPUtil {

	private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

	private String host;
	private int port;
	private String username;
	private String password;

	private static FTPUtil ftpUtil = null;

	private static FTPClient ftpClient = null;

	/**
	 * 获取FTP服务对象
	 * 
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @return
	 */
	public static FTPUtil getInstance(String host, int port, String username, String password) {
		if (ftpUtil == null) {
			ftpUtil = new FTPUtil(host, port, username, password);
		}
		return ftpUtil;
	}

	private FTPUtil(String host, int port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	/**
	 * 读取FTP指定目录下的指定文件
	 * 
	 * @param ftpPath
	 * @param fileName
	 * @param serverCharset
	 * @param localCharset
	 * @return
	 */
	public String getContentByFileName(String ftpPath, String fileName, String serverCharset, String localCharset) {
		connect(host, port, username, password);
		String result = null;
		if (ftpClient != null) {
			try {
				// 判断是否存在该目录
				if (!ftpClient.changeWorkingDirectory(ftpPath)) {
					logger.error(ftpPath + "该目录不存在");
					return result;
				}
				ftpClient.enterLocalPassiveMode(); // 设置被动模式，开通一个端口来传输数据
//				String[] fs = ftpClient.listNames();
//				// 判断该目录下是否有文件
//				if (fs == null || fs.length == 0) {
//					logger.error(ftpPath + "该目录下没有文件");
//					return result;
//				}
				try (InputStream is = ftpClient.retrieveFileStream(fileName)) {
					if (is != null) {
						if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
							result = processExcleFile(is, fileName);
						} else {
							ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
							byte[] buffer = new byte[1024];
							int readLength = 0;
							while ((readLength = is.read(buffer, 0, 1024)) > 0) {
								byteStream.write(buffer, 0, readLength);
							}
							result = new String(byteStream.toByteArray(), localCharset);
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			} catch (IOException e) {
				logger.error("获取文件失败", e);
			} finally {
				closeConnect();
			}
		}
		return result;
	}

	/**
	 * 读取FTP指定目录下的所有文件
	 * 
	 * @param ftpPath
	 * @param serverCharset
	 * @param localCharset
	 * @return
	 */
	public Map<String, String> getContentByFilePath(String ftpPath, String serverCharset, String localCharset) {
		connect(host, port, username, password);
		Map<String, String> map = new HashMap<>();
		if (ftpClient != null) {
			try {
				// 判断是否存在该目录
				if (!ftpClient.changeWorkingDirectory(ftpPath)) {
					logger.error(ftpPath + "该目录不存在");
					return map;
				}
				ftpClient.enterLocalPassiveMode(); // 设置被动模式，开通一个端口来传输数据
				String[] fs = ftpClient.listNames();
				// 判断该目录下是否有文件
				if (fs == null || fs.length == 0) {
					logger.error(ftpPath + "该目录下没有文件");
					return map;
				}
				for (String ff : fs) {
					try (InputStream is = ftpClient.retrieveFileStream(ff)) {
						String ftpName = new String(ff.getBytes(serverCharset), localCharset);
						if (ftpName.endsWith(".xls") || ftpName.endsWith(".xlsx")) {
							map.put(ftpName, processExcleFile(is, ftpName));
						} else {
							ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
							byte[] buffer = new byte[1024];
							int readLength = 0;
							while ((readLength = is.read(buffer, 0, 1024)) > 0) {
								byteStream.write(buffer, 0, readLength);
							}
							map.put(ftpName, new String(byteStream.toByteArray()));
						}
						ftpClient.completePendingCommand(); // 处理多个文件
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			} catch (IOException e) {
				logger.error("获取文件失败", e);
			} finally {
				closeConnect();
			}
		}
		return map;
	}

	/**
	 * 删除指定文件
	 * 
	 * @param filePath 文件相对路径，例如：test/123/test.txt
	 * @return 成功返回true，否则返回false
	 */
	public boolean deleteFile(String filePath) {
		// 登录
		connect(host, port, username, password);
		boolean flag = false;
		if (ftpClient != null) {
			try {
				flag = ftpClient.deleteFile(filePath);
			} catch (IOException e) {
				logger.error("删除文件失败", e);
			} finally {
				closeConnect();
			}
		}
		return flag;
	}

	/**
	 * 删除目录下所有文件
	 * 
	 * @param dirPath 文件相对路径，例如：test/123
	 * @return 成功返回true，否则返回false
	 */
	public boolean deleteFiles(String dirPath) {
		// 登录
		connect(host, port, username, password);
		boolean flag = false;
		if (ftpClient != null) {
			try {
				ftpClient.enterLocalPassiveMode(); // 设置被动模式，开通一个端口来传输数据
				String[] fs = ftpClient.listNames(dirPath);
				// 判断该目录下是否有文件
				if (fs == null || fs.length == 0) {
					logger.error(dirPath + "该目录下没有文件");
					return flag;
				}
				for (String ftpFile : fs) {
					ftpClient.deleteFile(ftpFile);
				}
				flag = true;
			} catch (IOException e) {
				logger.error("删除文件失败", e);
			} finally {
				closeConnect();
			}
		}
		return flag;
	}

	/**
	 * 连接FTP服务
	 * 
	 * @param host FTP 服务IP
	 * @param port 端口
	 * @param username 登录名
	 * @param password 密码
	 */
	private void connect(String host, int port, String username, String password) {
		ftpClient = new FTPClient();
		try {
			ftpClient.connect(host, port);
			ftpClient.login(username, password);
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				closeConnect();
				logger.error("FTP服务器连接失败");
			}
		} catch (Exception e) {
			logger.error("FTP登录失败", e);
		}
	}

	/**
	 * 关闭FTP连接
	 * 
	 */
	private void closeConnect() {
		if (ftpClient != null && ftpClient.isConnected()) {
			try {
				ftpClient.logout();
				ftpClient.disconnect();
			} catch (IOException e) {
				logger.error("关闭FTP连接失败", e);
			}
		}
	}

	private String processExcleFile(InputStream is, String fileName) throws Exception {
		Workbook wb = null;
		if (fileName.endsWith(".xls")) {
			wb = new HSSFWorkbook(is);
		} else if (fileName.endsWith(".xlsx")) {
			wb = new XSSFWorkbook(is);
		} else {
			logger.error("The Wrong file type！");
		}

		Map<String, List<Map<String, String>>> result = new HashMap<String, List<Map<String, String>>>();
		if (wb != null) {

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
							Cell cell = row.getCell(k);// getCell(k);
							titles.add(cell.toString().trim());
						}
					} else {// 其他行是数据行
						Map<String, String> rowMap = new HashMap<String, String>();// 对应一个数据行
						for (int k = 0; k < titles.size(); k++) {
							Cell cell = row.getCell(k);
							String key = titles.get(k);
							String value = null;
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
		}
		return JSON.toJSONString(result);
	}

	public static void main(String[] args) {
		FTPUtil util = FTPUtil.getInstance("192.168.1.82", 21, "ftpuser", "test");
		Map<String, String> map = util.getContentByFilePath("/",  "iso-8859-1", "GBK");

		System.out.println(map);
		String str = util.getContentByFileName("/","autotest.txt","UTF-8", "UTF-8");
		System.out.println(str);
	}

}
