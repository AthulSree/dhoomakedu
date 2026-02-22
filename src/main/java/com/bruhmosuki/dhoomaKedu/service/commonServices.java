package com.bruhmosuki.dhoomaKedu.service;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

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

    public String getUserIp(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        if(ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if(ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if(ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // If it's localhost (IPv4 or IPv6), get the machine's actual IP address
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            try {
                InetAddress inetAddress = InetAddress.getLocalHost();
                ip = inetAddress.getHostAddress();
            } catch (UnknownHostException e) {
                // If resolving fails, keep the loopback IP
                e.printStackTrace();
            }
        }

        // For X-Forwarded-For, sometimes there's a comma-separated list of IPs.
        // We usually want the first one.
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
