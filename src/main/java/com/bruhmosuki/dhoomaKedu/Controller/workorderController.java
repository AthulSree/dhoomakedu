package com.bruhmosuki.dhoomaKedu.Controller;

import com.bruhmosuki.dhoomaKedu.entity.workorder;
import com.bruhmosuki.dhoomaKedu.entity.employee;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.bruhmosuki.dhoomaKedu.service.workorderService;
import com.bruhmosuki.dhoomaKedu.service.employeeService;
import com.bruhmosuki.dhoomaKedu.service.commonServices;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


@Controller
@RequestMapping("/workorder")
public class workorderController {

    private workorderService theWorkorderService;
    private employeeService theEmployeeService;
    private commonServices theCommonServices;

    public workorderController(workorderService theWorkorderService, employeeService theEmployeeService, commonServices theCommonServices) {
        this.theWorkorderService = theWorkorderService;
        this.theEmployeeService = theEmployeeService;
        this.theCommonServices = theCommonServices;
    }

    @GetMapping("/manage")
    public String manageWo(Model theModel, HttpServletRequest request){
        theModel.addAttribute("panelName", "Work Order");
        workorder theWorkorder = new workorder();
        List<employee> emp_list = theEmployeeService.findAll();

        String userIp = theCommonServices.getUserIp(request);
        employee loggedUser = theEmployeeService.findBySysIp(userIp);
        if(!loggedUser.getIs_admin()){
            return "onlyAdminError";
        }

        List<workorder> workorderList = theWorkorderService.findAll();
        theModel.addAttribute("workorder",theWorkorder);
        theModel.addAttribute("emp_list",emp_list);
        theModel.addAttribute("wo_list",workorderList);
        return "workorder";
    }

    @PostMapping("/saveWorkorder")
    public String saveWo(@ModelAttribute("workorder") workorder theWorkorder){
        System.out.println(">>>>>>>>>>>" + theWorkorder.getId() + "<<<<<<<<<<<<<<");
        
        workorder chkAvail = theWorkorderService.findByEmpId(theWorkorder.getEmpId());
        if(chkAvail != null && theWorkorder.getId() == null){
            throw new IllegalStateException("The Workorder Already Exists. Try Updating the existing Workorder Details.");
        }
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
