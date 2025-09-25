package com.bruhmosuki.dhoomaKedu.Controller;

import com.bruhmosuki.dhoomaKedu.service.monthPeriodService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.bruhmosuki.dhoomaKedu.entity.leaves;
import com.bruhmosuki.dhoomaKedu.entity.monthPeriod;
import com.bruhmosuki.dhoomaKedu.entity.employee;
import org.springframework.web.multipart.MultipartFile;
import com.bruhmosuki.dhoomaKedu.dto.leaveDto;
import com.bruhmosuki.dhoomaKedu.service.employeeService;
import com.bruhmosuki.dhoomaKedu.service.leavesService;
import com.bruhmosuki.dhoomaKedu.service.commonServices;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/leaves")
public class leavesController {

    private  leavesService theLeaveService;
    private  employeeService theEmployeeService;
    private  monthPeriodService theMonthPeriodService;
    private  commonServices theCommonServices;

    public leavesController(leavesService theLeaveService, employeeService theEmployeeService, monthPeriodService theMonthPeriodService, commonServices theCommonServices) {
        this.theLeaveService = theLeaveService;
        this.theEmployeeService = theEmployeeService;
        this.theMonthPeriodService = theMonthPeriodService;
        this.theCommonServices = theCommonServices;
    }

    @GetMapping("/manage")
    public String manage(Model model, leaves theLeave){
        List<employee> theEmployee = theEmployeeService.findAll();
        monthPeriod theMonthPeriod = theMonthPeriodService.findAll().get(0);
        List<leaves> leavesList = theLeaveService.findByMonthPeriod(theMonthPeriod.getMonth(),theMonthPeriod.getYear());

//        String theSetMonthPeriod = String.valueOf(theMonthPeriod.getMonth())+'*'+String.valueOf(theMonthPeriod.getYear());

        theLeave.setLeaveMpMonth(theMonthPeriod.getMonth());
        theLeave.setLeaveMpYear(theMonthPeriod.getYear());
        model.addAttribute("leaveObj",theLeave);
        model.addAttribute("emp_list",theEmployee);
        model.addAttribute("leavesList",leavesList);

        List<Map<String, String>> lastThreeMonths = theCommonServices.fetchLastThreeMonths();

        System.out.println(theMonthPeriod);
        model.addAttribute("lastThreeMonths", lastThreeMonths);
        model.addAttribute("setMonth",theMonthPeriod);
        return "leaves";
    }

    @PostMapping("/saveLeave")
    public String saveLeave(@ModelAttribute("leaveObj") leaveDto theLeaveDto) throws IOException {
        System.out.println(">>>>>>>>>>"+theLeaveDto.getId()+"<<<<<<<<<<<<<<<<");

        leaves chkAvail = theLeaveService.findByEmpIdAndLeaveMpMonthAndLeaveMpYear(theLeaveDto.getEmpId(),theLeaveDto.getLeaveMpMonth(),theLeaveDto.getLeaveMpYear());
        if (chkAvail != null && theLeaveDto.getId() == null) {
            throw new IllegalStateException("The Leave Already Exists. Try Updating Leave Details.");
        }

        leaves theSavedLeave;
        if (theLeaveDto.getId() != null) {
            // Fetch existing entity for update
            theSavedLeave = theLeaveService.findById(theLeaveDto.getId().intValue());
            if (theSavedLeave == null) {
                theSavedLeave = new leaves(); // fallback if not found
            }
        } else {
            theSavedLeave = new leaves(); // New record
        }
        theSavedLeave.setEmpId(theLeaveDto.getEmpId());
        theSavedLeave.setLeaveStr(theLeaveDto.getLeaveStr());
        theSavedLeave.setHalfDayLeaveCnt(theLeaveDto.getHalfDayLeaveCnt());
        theSavedLeave.setLeaveMpMonth(theLeaveDto.getLeaveMpMonth());
        theSavedLeave.setLeaveMpYear(theLeaveDto.getLeaveMpYear());
        theSavedLeave.setAtSideA(theLeaveDto.getAtSideA().getBytes());
        theSavedLeave.setAtSideB(theLeaveDto.getAtSideB().getBytes());

        theLeaveService.save(theSavedLeave);
        return "redirect:/leaves/manage";
    }

    @GetMapping("/updateForm")
    public String updateLeave(@RequestParam("leaveId") int theId, Model theModel){
        leaves theLeave = theLeaveService.findById(theId);
        List<employee> theEmployee = theEmployeeService.findAll();
        monthPeriod theMonthPeriod = theMonthPeriodService.findAll().get(0);
        List<leaves> leavesList = theLeaveService.findByMonthPeriod(theMonthPeriod.getMonth(),theMonthPeriod.getYear());
        List<Map<String, String>> lastThreeMonths = theCommonServices.fetchLastThreeMonths();

        theModel.addAttribute("leaveObj",theLeave);
        theModel.addAttribute("emp_list",theEmployee);
        theModel.addAttribute("leavesList",leavesList);
        theModel.addAttribute("lastThreeMonths", lastThreeMonths);
        theModel.addAttribute("setMonth",theMonthPeriod);
        return "leaves";
    }

    @GetMapping("/fileGenz")
    public ResponseEntity<byte[]> getFile(@RequestParam("empId") int empId,
                                          @RequestParam("atSide") String atSide) {
        employee theEmployeeData = theEmployeeService.findById(empId);
        monthPeriod theMonthPeriod = theMonthPeriodService.findAll().get(0);
        leaves theLeave = theLeaveService.findByEmpIdAndLeaveMpMonthAndLeaveMpYear(
                theEmployeeData, theMonthPeriod.getMonth(), theMonthPeriod.getYear());

        byte[] atFile = null;
        String sideLabel = "";

        if ("sideA".equalsIgnoreCase(atSide)) {
            atFile = theLeave.getAtSideA();
            sideLabel = "A";
        } else if ("sideB".equalsIgnoreCase(atSide)) {
            atFile = theLeave.getAtSideB();
            sideLabel = "B";
        } else {
            return ResponseEntity.badRequest().build();
        }

        if (atFile == null || atFile.length == 0) {
            return ResponseEntity.notFound().build();
        }

        String fileName = "Att_" +  theEmployeeData.getFirst_name() + "_" + sideLabel  + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(atFile);
    }




    @PostMapping("/changeMonth")
    public String changeMonth(@RequestParam("month") String monthYr){
        String[] my_parts = monthYr.split("\\*");
        int month = Integer.parseInt(my_parts[0]);
        int year = Integer.parseInt(my_parts[1]);

        monthPeriod theMP = new monthPeriod();
        theMP.setMonth(month);
        theMP.setYear(year);

        theMonthPeriodService.DeleteAll();
        theMonthPeriodService.Save(theMP);

        return "redirect:/leaves/manage";
    }

}
