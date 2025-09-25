package com.bruhmosuki.dhoomaKedu.Controller;


import com.bruhmosuki.dhoomaKedu.entity.employee;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class DashboardController {

    @GetMapping("/")
    public String home(Model model){
        return "dashboard";
    }






}


