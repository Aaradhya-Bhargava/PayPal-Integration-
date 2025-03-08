package com.cpt.payments.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class AdditionController {
		
    @PostMapping("/add")
    public int add(@RequestParam int num1, @RequestParam int num2) {
        System.out.println("num1:" + num1 + "|num2:" + num2);
    	
        log.trace("num1:" + num1 + "|num2:" + num2);
        
        log.info("num1:" + num1 + "|num2:" + num2);
        log.warn("num1:" + num1 + "|num2:" + num2);
        log.error("num1:" + num1 + "|num2:" + num2);

        int sumResult = num1 + num2;
      System.out.println("sumResult:" + sumResult);

        return sumResult;
    }
}
