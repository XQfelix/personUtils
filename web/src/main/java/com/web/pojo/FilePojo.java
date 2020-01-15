package com.web.pojo;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2020/1/11 15:51
 */
@Component
public class FilePojo {
    private String fileName;
    private String filePath;
    private String fileSize;
    private String createTime;


//    public FilePojo(String fileName, String filePath, String fileSize, String createTime) {
//        this.fileName = fileName;
//        this.filePath = filePath;
//        this.fileSize = fileSize;
//        this.createTime = createTime;
//    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
