package com.bruhmosuki.dhoomaKedu.Controller;

import com.bruhmosuki.dhoomaKedu.entity.employee;
import com.bruhmosuki.dhoomaKedu.entity.leaves;
import com.bruhmosuki.dhoomaKedu.entity.monthPeriod;
import com.bruhmosuki.dhoomaKedu.entity.workorder;
import com.bruhmosuki.dhoomaKedu.service.leavesService;
import com.bruhmosuki.dhoomaKedu.service.monthPeriodService;
import com.bruhmosuki.dhoomaKedu.service.workorderService;
import com.bruhmosuki.dhoomaKedu.service.commonServices;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.bruhmosuki.dhoomaKedu.service.employeeService;
import com.bruhmosuki.dhoomaKedu.dto.employeeLeaveDto;

import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pdf")
public class PdfGeneratorController {

    @Autowired
    private TemplateEngine templateEngine; // Thymeleaf engine

    private employeeService theEmployeeService;
    private leavesService theLeaveService;
    private monthPeriodService theMonthPeriodService;
    private workorderService theWorkOrderService;
    private commonServices theCommonService;

    public PdfGeneratorController(TemplateEngine templateEngine, employeeService theEmployeeService,
            leavesService theLeaveService, monthPeriodService theMonthPeriodService,
            workorderService theWorkOrderService, commonServices theCommonService) {
        this.templateEngine = templateEngine;
        this.theEmployeeService = theEmployeeService;
        this.theLeaveService = theLeaveService;
        this.theMonthPeriodService = theMonthPeriodService;
        this.theWorkOrderService = theWorkOrderService;
        this.theCommonService = theCommonService;
    }

    @GetMapping("/genPdf")
    public String genPdf(Model model) {
        List<employeeLeaveDto> empMprData = theEmployeeService.getAllEmployeesLeaveDetails();
        System.out.println(empMprData);
        model.addAttribute("empMprData", empMprData);
        return "generate_pdf";
    }

    @GetMapping("/genz")
    public void pdfGen(@RequestParam("empId") int empId, HttpServletResponse response) throws IOException {
        employee theEmployeeData = theEmployeeService.findById(empId);
        // leaves theLeaveData = theLeaveService.findById(empId);
        workorder theWoData = theWorkOrderService.findByEmpId(theEmployeeData);
        monthPeriod theMonthPeriod = theMonthPeriodService.findAll().get(0);
        leaves leavesList = theLeaveService.findByEmpIdAndLeaveMpMonthAndLeaveMpYear(theEmployeeData,
                theMonthPeriod.getMonth(), theMonthPeriod.getYear());


        // Concat name
        String empName = theEmployeeData.getFirst_name() + " " + theEmployeeData.getLast_name();
        String asGrp = theEmployeeData.getAs_group();

        System.out.println("ASGrp: " + asGrp);

        // get Month & Period
        int month = theMonthPeriod.getMonth(); // e.g. 8
        int year = theMonthPeriod.getYear(); // e.g. 2025

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy");
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = YearMonth.of(year, month).atEndOfMonth();

        YearMonth ym = YearMonth.of(year, month);
        DateTimeFormatter mYformatter = DateTimeFormatter.ofPattern("MMMM, yyyy", Locale.ENGLISH);
        DateTimeFormatter mformatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH);

        // Removing combo leaves from leave list
        String finalLeaveStr = removeLastElements(leavesList.getLeaveStr(), leavesList.getUsedComboLeaves());
        // Count Leave
        String leaveStr = finalLeaveStr; // e.g. "12,15,19"
        float halfDayLeaveCnt = leavesList.getHalfDayLeaveCnt();
        float empAbsentCnt = 0;

        if (leaveStr != null && !leaveStr.isBlank()) {
            empAbsentCnt = leaveStr.split(",").length;
        }
        empAbsentCnt += (float) (halfDayLeaveCnt * 0.5);

        DecimalFormat df = new DecimalFormat("0.#");

        // Current date formatted and tomorrow's date for the MPR
        String today = LocalDate.now().format(formatter);
        String tomorrow = LocalDate.now().plusDays(1).format(formatter);

        // 1. Prepare data for Thymeleaf
        Context context = new Context();
        context.setVariable("empProjNo", theWoData.getProjectNo());
        context.setVariable("empWorkOrdr", theWoData.getWoNumber());
        context.setVariable("empMonthPeriod", ym.format(mYformatter));
        context.setVariable("empMonth", ym.format(mformatter));
        context.setVariable("empName", empName);
        context.setVariable("empDesign", theWoData.getDesignation());
        context.setVariable("empJoinDate", theWoData.getJoinDate().format(formatter));
        context.setVariable("empJoinDateMonthAbb", theWoData.getJoinDate().format(monthFormatter));
        context.setVariable("empFromDate", firstDay.format(formatter));
        context.setVariable("empToDate", lastDay.format(formatter));
        context.setVariable("empAbsentCnt", df.format(empAbsentCnt));
        context.setVariable("currentDate", tomorrow); // MPR generation date (next day)

        if ("ASG2".equals(asGrp)) {
            context.setVariable("proj_manager", "Anil V.S");
            if(theEmployeeData.getSys_ip().equals("10.162.6.168")){
                context.setVariable("proj_manager", "J VIOLET CRYSOLYTE");
            }
        } else {
            context.setVariable("proj_manager", "K. Rajan");
        }

        // 2. Render HTML from template
        String htmlText = null;
        if (Objects.equals(theWoData.getAgency(), "AEOLOGIC")) {
            htmlText = templateEngine.process("mpr_template/aeologic_mpr", context);
        } else if (Objects.equals(theWoData.getAgency(), "AKAL")) {
            htmlText = templateEngine.process("mpr_template/akal_mpr", context);
        }

        // 3. Set response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + empName + "_MPR.pdf");

        // 4. Convert HTML to PDF and write to browser
        ConverterProperties converterProperties = new ConverterProperties();
        converterProperties.setFontProvider(new DefaultFontProvider(true, true, true));

        HtmlConverter.convertToPdf(htmlText, response.getOutputStream(), converterProperties);

        response.getOutputStream().flush();
    }

    public static String removeLastElements(String lvstr, int comb) {
        String[] parts = lvstr.split(",");
        if (comb >= parts.length || lvstr.isBlank()) {
            return "";
        }
        return Arrays.stream(parts)
                .limit(parts.length - comb)
                .collect(Collectors.joining(","));
    }

    @GetMapping("/lacGenz")
    public void pdfLacGen(@RequestParam("empId") int empId, HttpServletResponse response) throws IOException {

        // leaves theLeaveData = theLeaveService.findById(empId);
        employee theEmployeeData = theEmployeeService.findById(empId);
        workorder theWoData = theWorkOrderService.findByEmpId(theEmployeeData);
        monthPeriod theMonthPeriod = theMonthPeriodService.findAll().get(0);
        leaves leavesList = theLeaveService.findByEmpIdAndLeaveMpMonthAndLeaveMpYear(theEmployeeData,
                theMonthPeriod.getMonth(), theMonthPeriod.getYear());

        String empName = theEmployeeData.getFirst_name() + " " + theEmployeeData.getLast_name();

        // Removing combo leaves from leave list
        String finalLeaveStr = removeLastElements(leavesList.getLeaveStr(), leavesList.getUsedComboLeaves());


        String leaveStr = finalLeaveStr; // e.g. "29,30,31"
        int month = theMonthPeriod.getMonth();
        int year = theMonthPeriod.getYear();
        String monthStr = String.format("%02d", month);

        List<String> dates = Arrays.stream(leaveStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(d -> String.format("%02d/%s/%d", Integer.parseInt(d), monthStr, year))
                .toList();

        String formattedText = "";
        if (dates.size() == 1) {
            formattedText = dates.get(0);
        } else if (dates.size() == 2) {
            formattedText = dates.get(0) + " and " + dates.get(1);
        } else if (dates.size() > 2) {
            formattedText = String.join(", ", dates.subList(0, dates.size() - 1))
                    + " and " + dates.get(dates.size() - 1);
        }

        int empAbsentCnt = 0;

        if (leaveStr != null && !leaveStr.isBlank()) {
            empAbsentCnt = leaveStr.split(",").length;
        }

        // 1. Prepare data for Thymeleaf
        Context context = new Context();
        context.setVariable("empProjNo", theWoData.getProjectNo());
        context.setVariable("empWorkOrdr", theWoData.getWoNumber());
        context.setVariable("empName", empName);
        context.setVariable("leaveDates", formattedText);
        context.setVariable("leaveCnt", empAbsentCnt);

        // 2. Render HTML from template
        String htmlText = templateEngine.process("mpr_template/aeologic_lac", context);

        // 3. Set response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + empName + "_LAC.pdf");

        // 4. Convert HTML to PDF and write to browser
        ConverterProperties converterProperties = new ConverterProperties();
        converterProperties.setFontProvider(new DefaultFontProvider(true, true, true));

        HtmlConverter.convertToPdf(htmlText, response.getOutputStream(), converterProperties);

        response.getOutputStream().flush();
    }
}
