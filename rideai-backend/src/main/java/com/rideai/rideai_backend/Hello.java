package com.rideai.rideai_backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Hello {
    
    @GetMapping("/")
    public String print(){
        return "Hello";
    }

    @GetMapping("/home")
    public String home(){
        return "This is Home Page";
    }    
}
