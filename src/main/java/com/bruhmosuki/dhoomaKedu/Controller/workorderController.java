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
        String userIp = theCommonServices.getUserIp(request);
        employee loggedUser = theEmployeeService.findBySysIp(userIp);
        if(loggedUser == null || !loggedUser.getIs_admin()){
            return "onlyAdminError";
        }

        workorder theWorkorder = new workorder();
        populateModel(theModel, theWorkorder, loggedUser);
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
    public String updateWo(@RequestParam("woId") int theWoId, Model theModel, HttpServletRequest request){
        theModel.addAttribute("panelName", "Work Order");
        String userIp = theCommonServices.getUserIp(request);
        employee loggedUser = theEmployeeService.findBySysIp(userIp);
        if(loggedUser == null || !loggedUser.getIs_admin()){
            return "onlyAdminError";
        }

        workorder theWorkorder = theWorkorderService.findById(theWoId);
        populateModel(theModel, theWorkorder, loggedUser);
        return "workorder";
    }

    private void populateModel(Model theModel, workorder currentWorkorder, employee loggedUser) {
        List<workorder> workorderList = theWorkorderService.findAll();
        List<employee> emp_list = theEmployeeService.findAll();

        long expiredCount = workorderList.stream().filter(w -> "EXPIRED".equals(w.getExpiryStatus())).count();
        long criticalCount = workorderList.stream().filter(w -> "CRITICAL".equals(w.getExpiryStatus())).count();
        long warningCount = workorderList.stream().filter(w -> "WARNING".equals(w.getExpiryStatus())).count();
        long healthyCount = workorderList.stream().filter(w -> "OK".equals(w.getExpiryStatus())).count();

        workorder loggedUserWo = (loggedUser != null) ? theWorkorderService.findByEmpId(loggedUser) : null;

        theModel.addAttribute("workorder", currentWorkorder);
        theModel.addAttribute("emp_list", emp_list);
        theModel.addAttribute("wo_list", workorderList);
        theModel.addAttribute("loggedUser", loggedUser);
        theModel.addAttribute("loggedUserWo", loggedUserWo);
        theModel.addAttribute("expiredCount", expiredCount);
        theModel.addAttribute("criticalCount", criticalCount);
        theModel.addAttribute("warningCount", warningCount);
        theModel.addAttribute("healthyCount", healthyCount);
    }
}
