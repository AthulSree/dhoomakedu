package com.bruhmosuki.dhoomaKedu.Controller;

import com.bruhmosuki.dhoomaKedu.entity.employee;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.bruhmosuki.dhoomaKedu.service.employeeService;

import java.util.List;

@Controller
@RequestMapping("/employee")
public class employeeController {

    private final employeeService employeeService;

    public employeeController(employeeService theEmployeeService) {
        this.employeeService = theEmployeeService;
    }

    @GetMapping("/manage")
    public String manage(Model model){
        List<employee> employeeData = employeeService.findAll();
        employee theEmployee = new employee();
        model.addAttribute("employee",theEmployee);
        model.addAttribute("employeeData",employeeData);
        return "employee";
    }

    @GetMapping("/showFormForUpdate")
    public String manageUpdate(@RequestParam("employeeId") int theId, Model model){
        List<employee> employeeData = employeeService.findAll();
        employee theEmployee = employeeService.findById(theId);
        model.addAttribute("employee",theEmployee);
        model.addAttribute("employeeData",employeeData);
        System.out.println(theId);
        return "employee";
    }

    @GetMapping("/deleteEmployee")
    public String manageDelete(@RequestParam("employeeId") int theId, Model model){
        employeeService.deleteById(theId);
        List<employee> employeeData = employeeService.findAll();
        employee theEmployee = new employee();
        model.addAttribute("employee",theEmployee);
        model.addAttribute("employeeData",employeeData);
        return "employee";
    }

    @PostMapping("/save_employee")
    public String saveEmployee(@ModelAttribute("employee") employee theEmployee){
        employeeService.save(theEmployee);
        return "redirect:/employee/manage";
    }
}
