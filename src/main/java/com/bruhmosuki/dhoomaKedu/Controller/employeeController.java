package com.bruhmosuki.dhoomaKedu.Controller;

import com.bruhmosuki.dhoomaKedu.entity.employee;
import com.bruhmosuki.dhoomaKedu.entity.workorder;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.bruhmosuki.dhoomaKedu.service.employeeService;
import com.bruhmosuki.dhoomaKedu.service.workorderService;
import com.bruhmosuki.dhoomaKedu.service.commonServices;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/employee")
public class employeeController {

    private final employeeService employeeService;
    private final commonServices theCommonServices;
    private workorderService theWorkorderService;

    public employeeController(employeeService theEmployeeService, commonServices theCommonServices,
            workorderService theWorkorderService) {
        this.employeeService = theEmployeeService;
        this.theCommonServices = theCommonServices;
        this.theWorkorderService = theWorkorderService;
    }

    @GetMapping("/manage")
    public String manage(Model model, HttpServletRequest request) {
        String userIp = theCommonServices.getUserIp(request);
        System.out.println("................." + userIp);
        employee loggedUser = employeeService.findBySysIp(userIp);
        workorder emp_wo = theWorkorderService.findByEmpId(loggedUser);
        if (!loggedUser.getIs_admin()) {
            if (emp_wo != null && emp_wo.getWoToDate() != null) {
                long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(), emp_wo.getWoToDate());
                model.addAttribute("daysToExpiry", daysToExpiry);
            }
            model.addAttribute("loggedUser", loggedUser);
            model.addAttribute("emp_wo", emp_wo);
            return "userDetails";
        }
        List<employee> employeeData = employeeService.findAll();
        employee theEmployee = new employee();
        model.addAttribute("employee", theEmployee);
        model.addAttribute("employeeData", employeeData);
        return "employee";
    }

    @GetMapping("/showFormForUpdate")
    public String manageUpdate(@RequestParam("employeeId") int theId, Model model) {
        List<employee> employeeData = employeeService.findAll();
        employee theEmployee = employeeService.findById(theId);
        model.addAttribute("employee", theEmployee);
        model.addAttribute("employeeData", employeeData);
        System.out.println(theId);
        return "employee";
    }

    @GetMapping("/deleteEmployee")
    public String manageDelete(@RequestParam("employeeId") int theId, Model model) {
        employeeService.deleteById(theId);
        List<employee> employeeData = employeeService.findAll();
        employee theEmployee = new employee();
        model.addAttribute("employee", theEmployee);
        model.addAttribute("employeeData", employeeData);
        return "employee";
    }

    @PostMapping("/save_employee")
    public String saveEmployee(@ModelAttribute("employee") employee theEmployee) {
        String employeeIp = theEmployee.getSys_ip();
        if (theEmployee.getId() == null || theEmployee.getId() == 0) {
            employee ipAvail = employeeService.findBySysIp(employeeIp);
            if (ipAvail != null) {
                throw new IllegalStateException(
                        "The IP Address Already Exists. Try Updating the existing IP Address Details.");
            }
        }
        employeeService.save(theEmployee);
        return "redirect:/employee/manage";
    }
}
