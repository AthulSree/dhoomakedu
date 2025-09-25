package com.bruhmosuki.dhoomaKedu.dto;

import java.time.LocalDate;
import java.util.Date;

public class employeeLeaveDto {
    private Long empId;
    private String firstName;
    private String lastName;
    private String asGroup;
    private Character at1;
    private Character at2;
    private String leaveList;
    private String agency;
    private Date joinDate;
    private String projectNum;

    public employeeLeaveDto(Long empId, String firstName, String lastName, String asGroup, Character at1, Character at2, String leaveList, String agency, Date joinDate, String projectNum) {
        this.empId = empId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.asGroup = asGroup;
        this.at1 = at1;
        this.at2 = at2;
        this.leaveList = leaveList;
        this.agency = agency;
        this.joinDate = joinDate;
        this.projectNum = projectNum;
    }

    public Long getEmpId() {
        return empId;
    }

    public void setEmpId(Long empId) {
        this.empId = empId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAsGroup() {
        return asGroup;
    }

    public void setAsGroup(String asGroup) {
        this.asGroup = asGroup;
    }

    public Character getAt1() {
        return at1;
    }

    public void setAt1(Character at1) {
        this.at1 = at1;
    }

    public Character getAt2() {
        return at2;
    }

    public void setAt2(Character at2) {
        this.at2 = at2;
    }

    public String getLeaveList() {
        return leaveList;
    }

    public void setLeaveList(String leaveList) {
        this.leaveList = leaveList;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public String getProjectNum() {
        return projectNum;
    }

    public void setProjectNum(String projectNum) {
        this.projectNum = projectNum;
    }

    @Override
    public String toString() {
        return "employeeLeaveDto{" +
                "empId=" + empId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", asGroup='" + asGroup + '\'' +
                ", at1=" + at1 +
                ", at2=" + at2 +
                ", leaveList='" + leaveList + '\'' +
                ", agency='" + agency + '\'' +
                ", joinDate=" + joinDate +
                ", projectNum='" + projectNum + '\'' +
                '}';
    }
}
