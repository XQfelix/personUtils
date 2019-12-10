package com.common.util.Ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketException;


/**
 * @Title 从Ftp读取
 * @author GQ_Yin
 * */
public class ReadFTPFile {
	private static Logger logger = LoggerFactory.getLogger(ReadFTPFile.class);

	/**
	 * 去 服务器的FTP路径下上读取文件
	 * 
	 * @param ftpUserName
	 * @param ftpPassword
	 * @param ftpPath
	 * @return
	 */
	public static String readConfigFileForFTP(String ftpUserName, String ftpPassword, String ftpPath, String ftpHost,
			int ftpPort, String fileName) {
		StringBuffer resultBuffer = new StringBuffer();
		FileInputStream inFile = null;
		InputStream in = null;
		FTPClient ftpClient = null;
		logger.info("开始读取绝对路径" + ftpPath + "文件!");
		try {
			ftpClient = FTPUtil.getFTPClient(ftpHost, ftpPassword, ftpUserName, ftpPort);
			ftpClient.setControlEncoding("UTF-8"); // 中文支持
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftpClient.enterLocalPassiveMode();
			ftpClient.changeWorkingDirectory(ftpPath);
			in = ftpClient.retrieveFileStream(fileName);
		} catch (FileNotFoundException e) {
			logger.error("没有找到" + ftpPath + "文件");
			e.printStackTrace();
			return "下载配置文件失败，请联系管理员.";
		} catch (SocketException e) {
			logger.error("连接FTP失败.");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("文件读取错误。");
			e.printStackTrace();
			return "配置文件读取失败，请联系管理员.";
		}
		if (in != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String data = null;
			try {
				while ((data = br.readLine()) != null) {
					resultBuffer.append(data + "\n");
				}
			} catch (IOException e) {
				logger.error("文件读取错误。");
				e.printStackTrace();
				return "配置文件读取失败，请联系管理员.";
			} finally {
				try {
					ftpClient.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			logger.error("in为空，不能读取。");
			return "配置文件读取失败，请联系管理员.";
		}
		return resultBuffer.toString();
	}
	
	
	/*
     * 从FTP服务器下载文件
     *
     * @param ftpHost FTP IP地址
     * @param ftpUserName FTP 用户名
     * @param ftpPassword FTP用户名密码
     * @param ftpPort FTP端口
     * @param ftpPath FTP服务器中文件所在路径 格式： ftptest/aa
     * @param localPath 下载到本地的位置 格式：H:/download
     * @param fileName 文件名称
     * @param saveFileName 要保存的文件名
     */
    public static void downloadFtpFile(String ftpHost, String ftpUserName, String ftpPassword, String ftpPort, String ftpPath, String localPath, String fileName, String saveFileName) {

        FTPClient ftpClient = null;

        try {
            ftpClient = FTPUtil.getFTPClient(ftpHost, ftpPassword, ftpUserName, Integer.parseInt(ftpPort));
            ftpClient.setControlEncoding("UTF-8"); // 中文支持
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(ftpPath);

            File localFile = new File(localPath + File.separatorChar + saveFileName);
            OutputStream os = new FileOutputStream(localFile);
            ftpClient.retrieveFile(fileName, os);
            os.close();
            ftpClient.logout();
            logger.info("文件下载成功");
        } catch (FileNotFoundException e) {
            System.out.println("没有找到" + ftpPath + "文件");
            e.printStackTrace();
        } catch (SocketException e) {
            System.out.println("连接FTP失败.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件读取错误。");
            e.printStackTrace();
        }

    }
	
	
	
	
    public static void main(String[] args) {
		//downloadFtpFile("39.105.35.188", "felix","123456", "21", "/www/wwwroot/felixFTP", "D:/", "HELLO.pdf", "a.doc");
//		System.out.println(ret);
//		File file = new File("D:/HELLO.pdf");
//		if (file.isFile() && file.exists()) {  
//	        //file.delete();  
//			file.delete();
//	    }  
	}
}