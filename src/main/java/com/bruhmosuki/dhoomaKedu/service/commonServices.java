package com.bruhmosuki.dhoomaKedu.service;

import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class commonServices {

    public List<Map<String, String>> fetchLastThreeMonths(){
        YearMonth current = YearMonth.now();
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MMMM yyyy");

        List<Map<String, String>> lastThreeMonths = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            YearMonth ym = current.minusMonths(i);

            Map<String, String> monthData = new HashMap<>();
            monthData.put("value", ym.getMonthValue() + "*" + ym.getYear()); // e.g. 8*2025
            monthData.put("label", ym.format(labelFmt)); // e.g. August 2025

            lastThreeMonths.add(monthData);
        }
        return lastThreeMonths;
    }
}
