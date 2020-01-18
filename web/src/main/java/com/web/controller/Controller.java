package com.web.controller;

import com.web.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
<<<<<<< HEAD
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
=======
import java.net.URLEncoder;
>>>>>>> 0309382ce2657623fce6e46e7b588607138441bc

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/10 10:53
 */
@RestController
@CrossOrigin
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    @Autowired
    private PersonService personserviceimpl;


    /**
     * 上传文件
     *
     * @param file
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/person/file/upload", method = RequestMethod.POST)
    public String uploadFlie(@RequestParam(value = "file") MultipartFile file) throws Exception {
        return personserviceimpl.uploadFile(file);
    }

    /**
     * 下载文件
     *
     * @param request
     * @param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/person/file/download", method = RequestMethod.POST)
    public byte[] downLoadFile(HttpServletRequest request, @RequestBody byte[] body, HttpServletResponse response) throws Exception {
<<<<<<< HEAD
        String fileName = URLDecoder.decode(new String(body, "utf-8"), "UTF-8");

=======
        String fileName = new String(body, "utf-8");
>>>>>>> 0309382ce2657623fce6e46e7b588607138441bc
        fileName = fileName.substring(0, fileName.indexOf("="));
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
<<<<<<< HEAD


=======
>>>>>>> 0309382ce2657623fce6e46e7b588607138441bc
        return personserviceimpl.downloadFile(fileName);
    }

    /**
     * 删除文件
     *
     * @param filename
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/person/file/delete", method = RequestMethod.GET)
    public String deleteFile(@RequestParam(value = "filename") String filename, @RequestParam(value = "fileindex") String fileindex) throws Exception {
        return personserviceimpl.deleteFile(filename, fileindex);
    }

    /**
     * 加载文件列表
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/person/file/getallfile", method = RequestMethod.GET)
    public String getFiles() throws Exception {
        return personserviceimpl.getFiles();
    }

<<<<<<< HEAD
=======

    /**
     * 添加数据源
     *@param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/person/dblist/add", method = RequestMethod.POST)
    public String createDB(@RequestBody byte[] body) throws Exception {
        return personserviceimpl.createDB(new String(body, "utf-8"));
    }


    /**
     * 更新数据源
     *
     * @param body
     * @param index
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/person/dblist/update/{index}", method = RequestMethod.POST)
    public String updateDB(@PathVariable String index, @RequestBody byte[] body) throws Exception {
        return personserviceimpl.updateDB(index, new String(body, "utf-8"));
    }
>>>>>>> 0309382ce2657623fce6e46e7b588607138441bc

    /**
     * 添加数据源
     *@param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/person/dblist/add", method = RequestMethod.POST)
    public String createDB(@RequestBody byte[] body) throws Exception {
        return personserviceimpl.createDB(new String(body, "utf-8"));
    }


    /**
     * 更新数据源
     *
     * @param body
     * @param index
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/person/dblist/update/{index}", method = RequestMethod.POST)
    public String updateDB(@PathVariable String index, @RequestBody byte[] body) throws Exception {
        return personserviceimpl.updateDB(index, new String(body, "utf-8"));
    }


    /**
     * 删除数据源
     *
     * @param dbindex
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/person/dblist/delete", method = RequestMethod.GET)
    public String deleteDB(@RequestParam(value = "dbindex") String dbindex) throws Exception {
        return personserviceimpl.deleteDB(dbindex);
    }

<<<<<<< HEAD

    /**
     * 获取全部数据源
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/person/dblist/getall", method = RequestMethod.GET)
    public String getAlldbs() throws Exception {
        return personserviceimpl.getAlldbs();
    }


    /**
     * 加密解密/时间转换
     *@param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/person/encrypt", method = RequestMethod.POST)
    public String encryptUtil(@RequestBody byte[] body) throws Exception {
        return personserviceimpl.encryptUtil(new String(body, "utf-8"));
    }


=======
    /**
     * 删除数据源
     *
     * @param dbindex
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/person/dblist/delete", method = RequestMethod.GET)
    public String deleteDB(@RequestParam(value = "dbindex") String dbindex) throws Exception {
        return personserviceimpl.deleteDB(dbindex);
    }


    /**
     * 获取全部数据源
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/person/dblist/getall", method = RequestMethod.GET)
    public String getAlldbs() throws Exception {
        return personserviceimpl.getAlldbs();
    }


>>>>>>> 0309382ce2657623fce6e46e7b588607138441bc


}
