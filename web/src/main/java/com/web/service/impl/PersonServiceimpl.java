package com.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
<<<<<<< HEAD
import com.common.util.*;
import com.web.dao.impl.PersonRedisDao;
import com.web.service.PersonService;
import org.apache.commons.codec.digest.DigestUtils;
=======
import com.web.dao.impl.PersonRedisDao;
import com.web.service.PersonService;
>>>>>>> 0309382ce2657623fce6e46e7b588607138441bc
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
<<<<<<< HEAD
        String filePath = System.getProperty("user.dir") + "/upload/";
=======
        String filePath = "D://";
>>>>>>> 0309382ce2657623fce6e46e7b588607138441bc
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
<<<<<<< HEAD
        return getFile(System.getProperty("user.dir") + "/upload/"+fileName);
=======
        return getFile("D:/"+fileName);
>>>>>>> 0309382ce2657623fce6e46e7b588607138441bc
    }

    @Override
    public String deleteFile(String fileName, String fileIndex) throws Exception {
<<<<<<< HEAD
        File file = new File(System.getProperty("user.dir") + "/upload/" +fileName);
=======
        File file = new File("D:/"+fileName);
>>>>>>> 0309382ce2657623fce6e46e7b588607138441bc
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

<<<<<<< HEAD
    @Override
    public String encryptUtil(String param) throws Exception {
        Thread.sleep(500);
        JSONObject paramObj = JSONObject.parseObject(param);
        String ret = "";
        switch (paramObj.getString("type")){
            case "jasyptenc":
                JasyptEncUtil jeuenc = new JasyptEncUtil(paramObj.getString("publicPassword"));
                ret = jeuenc.encrypt(paramObj.getString("key"));
                break;
            case "jasyptdec":
                JasyptEncUtil jeudec = new JasyptEncUtil(paramObj.getString("publicPassword"));
                ret = jeudec.decrypt(paramObj.getString("key"));
                break;
            case "desenc": //公钥需要8位
                ret = DESUtil.encrypt(paramObj.getString("publicPassword"), paramObj.getString("key"));
                break;
            case "desdec": //公钥需要8位
                ret = DESUtil.decrypt(paramObj.getString("publicPassword"), paramObj.getString("key"));
                break;
            case "aesenc"://公钥需要16位
                ret = AES.Encrypt(paramObj.getString("publicPassword"), paramObj.getString("key"));
                break;
            case "aesdec"://公钥需要16位
                ret = AES.Decrypt(paramObj.getString("publicPassword"), paramObj.getString("key"));
                break;
            case "md5":
                ret = MD5Utils.string2MD5(paramObj.getString("key"));
                break;
            case "base64enc":
                ret = Base64Util.base64(paramObj.getString("key"));
                break;
            case "base64dec":
                ret = Base64Util.base64dec(paramObj.getString("key"));
                break;
            case "time2date":
                ret = CommonUtil.conLong2Date(Long.parseLong(paramObj.getString("key").trim()), null);
                break;
            case "date2time":
                SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long time = format.parse(paramObj.getString("key").trim()).getTime();
                ret = String.valueOf(time);
                break;
            case "md2":
                ret = DigestUtils.md2Hex(paramObj.getString("key"));
                break;
            case "sha1":
                ret = DigestUtils.sha1Hex(paramObj.getString("key"));
                break;
            case "sha256":
                ret = DigestUtils.sha256Hex(paramObj.getString("key"));
                break;
            case "sha384":
                ret = DigestUtils.sha384Hex(paramObj.getString("key"));
                break;
            case "sha512":
                ret = DigestUtils.sha512Hex(paramObj.getString("key"));
                break;
            default:
                break;
        }
        return ret;
    }

=======
>>>>>>> 0309382ce2657623fce6e46e7b588607138441bc

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
