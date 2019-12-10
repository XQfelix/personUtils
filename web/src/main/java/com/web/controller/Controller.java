package com.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/10 10:53
 */
@RestController
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);


    @RequestMapping("/demo")
    public String demo(){
        return "hello World";
    }

}
