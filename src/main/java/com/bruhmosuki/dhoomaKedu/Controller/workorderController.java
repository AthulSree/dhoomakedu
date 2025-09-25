package com.bruhmosuki.dhoomaKedu.Controller;

import com.bruhmosuki.dhoomaKedu.entity.workorder;
import com.bruhmosuki.dhoomaKedu.entity.employee;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.bruhmosuki.dhoomaKedu.service.workorderService;
import com.bruhmosuki.dhoomaKedu.service.employeeService;

import java.util.List;


@Controller
@RequestMapping("/workorder")
public class workorderController {

    private workorderService theWorkorderService;
    private employeeService theEmployeeService;

    public workorderController(workorderService theWorkorderService, employeeService theEmployeeService) {
        this.theWorkorderService = theWorkorderService;
        this.theEmployeeService = theEmployeeService;
    }

    @GetMapping("/manage")
    public String manageWo(Model theModel){
        workorder theWorkorder = new workorder();
        List<employee> emp_list = theEmployeeService.findAll();
        List<workorder> workorderList = theWorkorderService.findAll();
        theModel.addAttribute("workorder",theWorkorder);
        theModel.addAttribute("emp_list",emp_list);
        theModel.addAttribute("wo_list",workorderList);
        return "workorder";
    }

    @PostMapping("/saveWorkorder")
    public String saveWo(@ModelAttribute("workorder") workorder theWorkorder){
        theWorkorderService.save(theWorkorder);
        return "redirect:/workorder/manage";
    }

    @GetMapping("/updateFormWorkorder")
    public String updateWo(@RequestParam("woId") int theWoId, Model theModel){
        workorder theWorkorder = theWorkorderService.findById(theWoId);
        List<workorder> workorderList = theWorkorderService.findAll();
        List<employee> emp_list = theEmployeeService.findAll();
        theModel.addAttribute("workorder",theWorkorder);
        theModel.addAttribute("emp_list",emp_list);
        theModel.addAttribute("wo_list",workorderList);
        return "workorder";
    }
}
