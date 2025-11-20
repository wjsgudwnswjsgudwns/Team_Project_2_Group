package com.example.test.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaRedirectController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        // 404 에러 등을 index.html로 리다이렉트
        return "forward:/index.html";
    }
}