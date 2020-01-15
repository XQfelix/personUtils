package com.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.web.dao.impl.PersonRedisDao;
import com.web.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/10 10:51
 */
@Service(value = "personserviceimpl")
public class PersonServiceimpl implements PersonService {
      /**Ehcache 模式*/
//    @Autowired
//    PersonDao personDaoimpl;


      /**redis 模式*/
      @Autowired
      PersonRedisDao personRedisDao;

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }
        String fileName = file.getOriginalFilename();
        String filePath = "D://";
        File dest = new File(filePath + fileName);
        ;
        try {
            file.transferTo(dest);
            SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
            JSONObject singleFile = new JSONObject();
            singleFile.put("filename", fileName);
            singleFile.put("filepath", filePath);
            long fileSizeSingle = dest.length();
            if(fileSizeSingle<1024){
                singleFile.put("filesize",fileSizeSingle+" b");
            }else if(fileSizeSingle/1024 < 1024){
                singleFile.put("filesize",fileSizeSingle/1024+" Kb");
            }else{
                singleFile.put("filesize",fileSizeSingle/(1024*1024)+" Mb");
            }
            singleFile.put("filetime", sdf.format(new Date()));
            int ret = personRedisDao.saveFiles(singleFile);
            if(ret == 1){
                return "success";
            }else{
                return "error";
            }
        } catch (IOException e) {
        }
        return "error！";
    }

    @Override
    public byte[] downloadFile(String fileName) throws Exception {
        return getFile("D:/"+fileName);
    }

    @Override
    public String deleteFile(String fileName, String fileIndex) throws Exception {
        File file = new File("D:/"+fileName);
        if (file.exists()) {
            file.delete();
        }
        int ret = personRedisDao.deleteFile(fileIndex);
        return ret > 0 ? "success" : "error";
    }

    @Override
    public String getFiles() throws Exception {
       JSONArray retArr = personRedisDao.getFiles();
       return retArr.toJSONString();
    }


    @Override
    public String createDB(String singleDB) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        JSONObject single = JSONObject.parseObject(singleDB);
        single.put("dbcreatetime", sdf.format(new Date()));
        return personRedisDao.createDB(single)?"success":"error";
    }

    @Override
    public String updateDB(String dbIndex, String singleDB) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        JSONObject single = JSONObject.parseObject(singleDB);
        single.put("dbcreatetime", sdf.format(new Date()));
        return personRedisDao.updateDB(dbIndex, single)?"success":"error";
    }

    @Override
    public String deleteDB(String dbIndex) throws Exception {
        return personRedisDao.deleteDB(dbIndex)>0 ? "success" : "error";
    }

    @Override
    public String getAlldbs() throws Exception {
        JSONArray retArr = personRedisDao.getAlldbs();
        return retArr.toJSONString();
    }


    private byte[] getFile(String fileName) throws Exception {
        byte[] ret = new byte[]{};
        FileInputStream fis = null;
        try {
            File file = new File(fileName);
            if (file.exists()) {
                fis = new FileInputStream(file);
                ret = new byte[(int) file.length()];
                byte[] buff = new byte[4096];
                int index = 0;
                while (true) {
                    int length = fis.read(buff);
                    if (length >= 0) {
                        for (int i = 0; i < length; i++) {
                            ret[index] = buff[i];
                            index++;
                        }
                    } else {
                        break;
                    }
                }
            } else {
                throw new Exception("File '" + fileName + "' is not exist.");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (fis != null) {
                fis.close();
            }
            fis = null;
        }
        return ret;
    }


}
