package com.web.dao.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.web.dao.PersonDao;

import net.sf.ehcache.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2020/1/11 15:42
 */
//@Repository(value = "personDaoimpl")
public class PersonDaoimpl implements PersonDao {
    @Autowired
    PersonCache personCache;
    @Override
    public synchronized Integer saveFiles(JSONObject singleFile){
        try {
            JSONArray fileList = new JSONArray();
            if(personCache.get("fileList")!=null){
                fileList= (JSONArray) personCache.get("fileList").getObjectValue();
            }
            fileList.add(singleFile);
            Element fileListEle = new Element("fileList", fileList);
            Element single = new Element(singleFile.getString("filename"), singleFile);
            personCache.put(single);
            personCache.put(fileListEle);
            return 1;
        }catch (Exception e){

        }
        return 0;
    }

    @Override
    public synchronized JSONArray getFiles() throws Exception{
        JSONArray fileList = new JSONArray();
        if(personCache.get("fileList")!=null){
            fileList= (JSONArray) personCache.get("fileList").getObjectValue();
        }
        return fileList;
    }

    @Override
    public synchronized Integer deleteFile(String fileIndex) throws Exception{
        JSONArray fileList = new JSONArray();
        if(personCache.get("fileList")!=null){
            fileList = (JSONArray) personCache.get("fileList").getObjectValue();
            fileList.remove(fileIndex);
            Element fileListEle = new Element("fileList", fileList);
            personCache.put(fileListEle);
            return 1;
        }else{
            return 0;
        }
    }

    @Override
    public Boolean createDB(JSONObject singleDB) throws Exception {
        return null;
    }

    @Override
    public Boolean updateDB(String dbIndex, JSONObject singleDB) throws Exception {
        return null;
    }

    @Override
    public Integer deleteDB(String dbIndex) throws Exception {
        return null;
    }

    @Override
    public JSONArray getAlldbs() throws Exception {
        return null;
    }
}
