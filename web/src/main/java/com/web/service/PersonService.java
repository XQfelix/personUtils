package com.web.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/10 10:52
 */
public interface PersonService {

    //上传文件
    public String uploadFile(MultipartFile file) throws Exception;
    //下载文件
    public byte[] downloadFile(String fileName) throws Exception;
    //删除文件
    public String deleteFile(String fileName, String fileIndex) throws Exception;
    //加载所有文件
    public String getFiles() throws Exception;


    //新增数据源
    public String createDB(String singleDB) throws Exception;
    //更新数据源
    public String updateDB(String dbIndex, String singleDB) throws Exception;
    //删除数据源
    public String deleteDB(String dbIndex) throws Exception;
    //加载全部数据源
    public String getAlldbs() throws Exception;
<<<<<<< HEAD

    //CRUD
    public String doCrud(String param) throws Exception;
    //连接测试
    public String doConnect(String param) throws Exception;

    //加密解密/时间转换
    public String encryptUtil(String param) throws Exception;
=======
>>>>>>> 0309382ce2657623fce6e46e7b588607138441bc
}
