package com.bruhmosuki.dhoomaKedu.Controller;

import com.bruhmosuki.dhoomaKedu.entity.employee;
import com.bruhmosuki.dhoomaKedu.entity.leaves;
import com.bruhmosuki.dhoomaKedu.entity.monthPeriod;
import com.bruhmosuki.dhoomaKedu.entity.workorder;
import com.bruhmosuki.dhoomaKedu.service.commonServices;
import com.bruhmosuki.dhoomaKedu.service.employeeService;
import com.bruhmosuki.dhoomaKedu.service.leavesService;
import com.bruhmosuki.dhoomaKedu.service.monthPeriodService;
import com.bruhmosuki.dhoomaKedu.service.workorderService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final monthPeriodService theMonthPeriodService;
    private final commonServices theCommonServices;
    private final employeeService theEmployeeService;
    private final workorderService theWorkorderService;
    private final leavesService theLeaveService;

    public SettingsController(monthPeriodService theMonthPeriodService,
                              commonServices theCommonServices,
                              employeeService theEmployeeService,
                              workorderService theWorkorderService,
                              leavesService theLeaveService) {
        this.theMonthPeriodService = theMonthPeriodService;
        this.theCommonServices = theCommonServices;
        this.theEmployeeService = theEmployeeService;
        this.theWorkorderService = theWorkorderService;
        this.theLeaveService = theLeaveService;
    }

    @GetMapping("/panel")
    public String settingsPanel(Model model, HttpServletRequest request) {

        // ── Current month period
        monthPeriod currentPeriod = theMonthPeriodService.findAll().get(0);
        model.addAttribute("currentPeriod", currentPeriod);

        // ── Last 6 months for quick-select
        List<Map<String, String>> lastSixMonths = theCommonServices.fetchLastSixMonths();
        model.addAttribute("lastSixMonths", lastSixMonths);

        // ── Base counts
        List<employee> allEmployees = theEmployeeService.findAll();
        List<workorder> allWorkorders = theWorkorderService.findAll();
        int totalLeaveRecords = theLeaveService.findAll().size();
        model.addAttribute("totalEmployees", allEmployees.size());
        model.addAttribute("totalWorkorders", allWorkorders.size());
        model.addAttribute("totalLeaveRecords", totalLeaveRecords);

        // ── Leaves submitted this month
        List<leaves> thisMonthLeaves = theLeaveService.findByMonthPeriod(
                currentPeriod.getMonth(), currentPeriod.getYear());
        model.addAttribute("submittedThisMonth", thisMonthLeaves.size());

        // ── Per-employee leave submission status for current month
        List<Map<String, Object>> leaveStatus = new ArrayList<>();
        for (employee emp : allEmployees) {
            leaves l = theLeaveService.findByEmpIdAndLeaveMpMonthAndLeaveMpYear(
                    emp, currentPeriod.getMonth(), currentPeriod.getYear());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", emp.getFirst_name() + " " + emp.getLast_name());
            row.put("group", emp.getAs_group());
            row.put("submitted", l != null);
            if (l != null) {
                int leaveDays = 0;
                if (l.getLeaveStr() != null && !l.getLeaveStr().isBlank()) {
                    leaveDays = l.getLeaveStr().split(",").length;
                }
                row.put("leaveDays", leaveDays);
                row.put("halfDays", l.getHalfDayLeaveCnt());
                row.put("comboUsed", l.getUsedComboLeaves());
            } else {
                row.put("leaveDays", "-");
                row.put("halfDays", "-");
                row.put("comboUsed", "-");
            }
            leaveStatus.add(row);
        }
        model.addAttribute("leaveStatus", leaveStatus);

        // ── Work order expiry alerts
        List<Map<String, Object>> woAlerts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (workorder wo : allWorkorders) {
            if (wo.getWoToDate() == null) continue;
            long daysLeft = ChronoUnit.DAYS.between(today, wo.getWoToDate());
            String status;
            if      (daysLeft < 0)   status = "EXPIRED";
            else if (daysLeft <= 30) status = "CRITICAL";
            else if (daysLeft <= 60) status = "WARNING";
            else                     status = "OK";

            Map<String, Object> alert = new LinkedHashMap<>();
            alert.put("name", wo.getEmpId().getFirst_name() + " " + wo.getEmpId().getLast_name());
            alert.put("woNumber", wo.getWoNumber());
            alert.put("designation", wo.getDesignation());
            alert.put("expiryDate", wo.getWoToDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            alert.put("daysLeft", daysLeft);
            alert.put("status", status);
            woAlerts.add(alert);
        }
        woAlerts.sort(Comparator.comparingLong(a -> (Long) a.get("daysLeft")));
        model.addAttribute("woAlerts", woAlerts);

        long expiredCount  = woAlerts.stream().filter(a -> "EXPIRED".equals(a.get("status"))).count();
        long criticalCount = woAlerts.stream().filter(a -> "CRITICAL".equals(a.get("status"))).count();
        long warningCount  = woAlerts.stream().filter(a -> "WARNING".equals(a.get("status"))).count();
        model.addAttribute("expiredCount", expiredCount);
        model.addAttribute("criticalCount", criticalCount);
        model.addAttribute("warningCount", warningCount);

        // ── System info
        String currentIp = theCommonServices.getUserIp(request);
        String serverTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));
        model.addAttribute("currentIp", currentIp);
        model.addAttribute("serverTime", serverTime);

        return "settings";
    }

    @PostMapping("/changeMonth")
    public String changeMonth(@RequestParam("month") String monthYr,
                              RedirectAttributes redirectAttributes) {
        String[] parts = monthYr.split("\\*");
        int month = Integer.parseInt(parts[0]);
        int year  = Integer.parseInt(parts[1]);

        monthPeriod theMP = new monthPeriod();
        theMP.setMonth(month);
        theMP.setYear(year);

        theMonthPeriodService.DeleteAll();
        theMonthPeriodService.Save(theMP);

        redirectAttributes.addFlashAttribute("successMsg", "Active period changed successfully!");
        return "redirect:/settings/panel";
    }
}
