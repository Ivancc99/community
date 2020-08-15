package com.donghua.community.controller;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LogginController {

    @RequestMapping(path="/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        System.out.println("被访问到了");
        return "/site/register";
    }
//    @RequestMapping(path = "/register", method = RequestMethod.GET)
//    public String getRegisterPage() {
//        return "/site/register";
//    }
}
