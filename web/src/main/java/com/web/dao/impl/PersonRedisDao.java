package com.web.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.web.config.RedisUtil;
import com.web.dao.PersonDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2020/1/12 11:02
 */
@Repository(value = "personRedisDao")
public class PersonRedisDao implements PersonDao {

    @Autowired
    RedisUtil redisUtil;

    @Override
    public Integer saveFiles(JSONObject singleFile) {
        boolean ret = redisUtil.lSet("fileList", singleFile);
        return ret ? 1 : 0;
    }

    @Override
    public JSONArray getFiles() throws Exception {
        List<Object> allFiles = redisUtil.lGet("fileList", 0, -1);
        if(allFiles != null && allFiles.size()>0){
            return JSONArray.parseArray(JSON.toJSONString(allFiles));
        }else{
            return new JSONArray();
        }
    }

    @Override
    public Integer deleteFile(String fileIndex) throws Exception {
        return (int)redisUtil.lRemove("fileList", 1, redisUtil.lGetIndex("fileList",Long.parseLong(fileIndex)));
    }


    @Override
    public Boolean createDB(JSONObject singleDB) throws Exception {
        return redisUtil.lSet("dbList", singleDB);
    }

    @Override
    public Boolean updateDB(String dbIndex, JSONObject singleDB) throws Exception {
        return redisUtil.lUpdateIndex("dbList", Long.parseLong(dbIndex), singleDB);
    }

    @Override
    public Integer deleteDB(String dbIndex) throws Exception {
        return (int)redisUtil.lRemove("dbList", 1, redisUtil.lGetIndex("dbList",Long.parseLong(dbIndex)));
    }

    @Override
    public JSONArray getAlldbs() throws Exception {
        List<Object> allDBs = redisUtil.lGet("dbList", 0, -1);
        if(allDBs != null && allDBs.size()>0){
            return JSONArray.parseArray(JSON.toJSONString(allDBs));
        }else{
            return new JSONArray();
        }
    }
}
