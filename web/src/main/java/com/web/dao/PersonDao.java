package com.web.dao;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2020/1/11 15:42
 */
public interface PersonDao {
    public Integer saveFiles(JSONObject singleFile);
    public JSONArray getFiles() throws Exception;
    public Integer deleteFile(String fileIndex) throws Exception;

    public Boolean createDB(JSONObject singleDB) throws Exception;
    public Boolean updateDB(String dbIndex, JSONObject singleDB) throws Exception;
    public Integer deleteDB(String dbIndex) throws Exception;
    public JSONArray getAlldbs() throws Exception;
}
