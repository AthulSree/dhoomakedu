package com.bruhmosuki.dhoomaKedu.service;

import com.bruhmosuki.dhoomaKedu.dao.policyAcknowledgmentRepository;
import com.bruhmosuki.dhoomaKedu.entity.policyAcknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PolicyService {

    private final policyAcknowledgmentRepository policyRepo;

    // Policy display modes: "DAILY_PER_IP", "ALWAYS_SHOW", "DISABLED"
    private volatile String policyMode = "DAILY_PER_IP";

    // Version token bumped on reset or mode change to invalidate stale client cookies/localStorage
    private volatile long policyVersion = System.currentTimeMillis();

    // In-memory fast cache: IP -> last acknowledged LocalDate
    private final Map<String, LocalDate> memoryCache = new ConcurrentHashMap<>();

    public PolicyService(policyAcknowledgmentRepository policyRepo) {
        this.policyRepo = policyRepo;
    }

    private String normalizeIp(String ip) {
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            return "127.0.0.1";
        }
        return ip.trim();
    }

    public boolean shouldShowPolicy(String rawIp, boolean forceQuery) {
        if (forceQuery) {
            return true;
        }

        if ("DISABLED".equalsIgnoreCase(policyMode)) {
            return false;
        }

        if ("ALWAYS_SHOW".equalsIgnoreCase(policyMode)) {
            return true;
        }

        String clientIp = normalizeIp(rawIp);
        LocalDate today = LocalDate.now();

        // Check memory cache first
        LocalDate cachedDate = memoryCache.get(clientIp);
        if (cachedDate != null && cachedDate.equals(today)) {
            return false;
        }

        // Check database
        try {
            boolean existsInDb = policyRepo.existsByIpAddressAndAcknowledgedDate(clientIp, today);
            if (existsInDb) {
                memoryCache.put(clientIp, today);
                return false;
            }
        } catch (Exception e) {
            // Fallback gracefully if database table is initializing
            System.err.println("Warning in PolicyService check: " + e.getMessage());
        }

        return true;
    }

    public void acknowledgePolicy(String rawIp) {
        String clientIp = normalizeIp(rawIp);
        LocalDate today = LocalDate.now();
        memoryCache.put(clientIp, today);

        try {
            if (!policyRepo.existsByIpAddressAndAcknowledgedDate(clientIp, today)) {
                policyAcknowledgment ack = new policyAcknowledgment(clientIp, today, LocalDateTime.now());
                policyRepo.save(ack);
            }
        } catch (Exception e) {
            System.err.println("Warning in PolicyService acknowledge: " + e.getMessage());
        }
    }

    public void resetTodayAcknowledgements() {
        LocalDate today = LocalDate.now();
        memoryCache.clear();
        policyVersion = System.currentTimeMillis();

        try {
            policyRepo.deleteByAcknowledgedDate(today);
        } catch (Exception e) {
            System.err.println("Warning resetting today acknowledgments: " + e.getMessage());
        }
    }

    public long getPolicyVersion() {
        return policyVersion;
    }

    public String getPolicyMode() {
        return policyMode;
    }

    public void setPolicyMode(String mode) {
        if (mode != null && (mode.equals("DAILY_PER_IP") || mode.equals("ALWAYS_SHOW") || mode.equals("DISABLED"))) {
            this.policyMode = mode;
            this.policyVersion = System.currentTimeMillis();
        }
    }

    public long getTodayAcknowledgedCount() {
        try {
            return policyRepo.countByAcknowledgedDate(LocalDate.now());
        } catch (Exception e) {
            return memoryCache.size();
        }
    }

    public List<policyAcknowledgment> getTodayAcknowledgedList() {
        try {
            return policyRepo.findByAcknowledgedDate(LocalDate.now());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
