package com.common.util.Ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


/**
 * @Title 上传文件到Ftp
 * @author GQ_Yin
 * */
public class WriteFTPFile {

	private static Logger logger = LoggerFactory.getLogger(WriteFTPFile.class);

	/**
	 * 本地上传文件到FTP服务器
	 * 
	 * @param ftpPath
	 *            远程文件路径FTP
	 * @throws IOException
	 */
	public static void upload(String ftpPath, String ftpUserName, String ftpPassword, String ftpHost, int ftpPort,
			String fileContent, String writeTempFielPath) {
		FTPClient ftpClient = null;
		logger.info("开始上传文件到FTP.");
		try {
			ftpClient = FTPUtil.getFTPClient(ftpHost, ftpPassword, ftpUserName, ftpPort);
			// 设置PassiveMode传输
			ftpClient.enterLocalPassiveMode();
			// 设置以二进制流的方式传输
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			// 对远程目录的处理
			String remoteFileName = ftpPath;
			if (ftpPath.contains("/")) {
				remoteFileName = ftpPath.substring(ftpPath.lastIndexOf("/") + 1);
			}
			// FTPFile[] files = ftpClient.listFiles(new
			// String(remoteFileName));
			// 先把文件写在本地。在上传到FTP上最后在删除
			boolean writeResult = write(remoteFileName, fileContent, writeTempFielPath);
			if (writeResult) {
				File f = new File(writeTempFielPath + "/" + remoteFileName);
				InputStream in = new FileInputStream(f);
				ftpClient.storeFile(remoteFileName, in);
				in.close();
				logger.info("上传文件" + remoteFileName + "到FTP成功!");
				f.delete();
			} else {
				logger.info("写文件失败!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
     * Description: 向FTP服务器上传文件
     * @param ftpHost FTP服务器hostname
     * @param ftpUserName 账号
     * @param ftpPassword 密码
     * @param ftpPort 端口
     * @param ftpPath  FTP服务器中文件所在路径 格式： ftptest/aa
     * @param fileName ftp文件名称
     * @param input 文件流
     * @return 成功返回true，否则返回false
     */
    public static boolean uploadFile(String ftpHost, String ftpUserName,
                                     String ftpPassword, String ftpPort, String ftpPath,
                                     String fileName,InputStream input) {
        boolean success = false;
        FTPClient ftpClient = null;
        try {
            int reply;
            ftpClient = FTPUtil.getFTPClient(ftpHost, ftpPassword, ftpUserName, Integer.parseInt(ftpPort));
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return success;
            }
            ftpClient.setControlEncoding("UTF-8"); // 中文支持
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(ftpPath);

            ftpClient.storeFile(fileName, input);

            input.close();
            ftpClient.logout();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return success;
    }

	/**
	 * 把配置文件先写到本地的一个文件中取
	 * 
	 * @param fileName
	 * @param fileContext
	 * @param writeTempFielPath
	 * @return
	 */
	public static boolean write(String fileName, String fileContext, String writeTempFielPath) {
		try {
			logger.info("开始写配置文件");
			File f = new File(writeTempFielPath + "/" + fileName);
			if (!f.exists()) {
				if (!f.createNewFile()) {
					logger.info("文件不存在，创建失败!");
				}
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
			bw.write(fileContext.replaceAll("\n", "\r\n"));
			bw.flush();
			bw.close();
			return true;
		} catch (Exception e) {
			logger.error("写文件没有成功");
			e.printStackTrace();
			return false;
		}
	}
}