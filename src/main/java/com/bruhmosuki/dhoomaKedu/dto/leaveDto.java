package com.bruhmosuki.dhoomaKedu.dto;

import com.bruhmosuki.dhoomaKedu.entity.employee;
import org.springframework.web.multipart.MultipartFile;


public class leaveDto {

    private Long id;
    private employee empId;
    private String leaveStr;
    private int halfDayLeaveCnt;
    private int leaveMpMonth;
    private int leaveMpYear;
    private MultipartFile atSideA;
    private MultipartFile atSideB;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public employee getEmpId() {
        return empId;
    }

    public void setEmpId(employee empId) {
        this.empId = empId;
    }

    public String getLeaveStr() {
        return leaveStr;
    }

    public void setLeaveStr(String leaveStr) {
        this.leaveStr = leaveStr;
    }

    public int getHalfDayLeaveCnt() {
        return halfDayLeaveCnt;
    }

    public void setHalfDayLeaveCnt(int halfDayLeaveCnt) {
        this.halfDayLeaveCnt = halfDayLeaveCnt;
    }

    public int getLeaveMpMonth() {
        return leaveMpMonth;
    }

    public void setLeaveMpMonth(int leaveMpMonth) {
        this.leaveMpMonth = leaveMpMonth;
    }

    public int getLeaveMpYear() {
        return leaveMpYear;
    }

    public void setLeaveMpYear(int leaveMpYear) {
        this.leaveMpYear = leaveMpYear;
    }

    public MultipartFile getAtSideA() {
        return atSideA;
    }

    public void setAtSideA(MultipartFile atSideA) {
        this.atSideA = atSideA;
    }

    public MultipartFile getAtSideB() {
        return atSideB;
    }

    public void setAtSideB(MultipartFile atSideB) {
        this.atSideB = atSideB;
    }


    @Override
    public String toString() {
        return "leaveDto{" +
                "id=" + id +
                ", empId=" + empId +
                ", leaveStr='" + leaveStr + '\'' +
                ", halfDayLeaveCnt=" + halfDayLeaveCnt +
                ", leaveMpMonth=" + leaveMpMonth +
                ", leaveMpYear=" + leaveMpYear +
                ", atSideA=" + atSideA +
                ", atSideB=" + atSideB +
                '}';
    }
}
