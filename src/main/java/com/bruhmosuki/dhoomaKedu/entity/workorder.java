package com.bruhmosuki.dhoomaKedu.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "work_order")
public class workorder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private employee empId;

    @Column(name = "agency", nullable = false, length = 10)
    private String agency;

    @Column(name = "wo_number", nullable = false, length = 15)
    private String woNumber;

    @Column(name = "project_num", nullable = false, length = 15)
    private String projectNo;

    @Column(name = "designation",nullable = false)
    private String designation;

    @Column(name = "join_date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate joinDate;

    @Column(name = "wo_from_date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate woFromDate;

    @Column(name = "wo_to_date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate woToDate;

    @Column(name = "updated_time", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedTime;


    public workorder() {
    }

    public workorder(Long id, employee empId, String agency, String woNumber, String projectNo, String designation, LocalDate joinDate, LocalDate woFromDate, LocalDate woToDate, LocalDateTime updatedTime) {
        this.id = id;
        this.empId = empId;
        this.agency = agency;
        this.woNumber = woNumber;
        this.projectNo = projectNo;
        this.designation = designation;
        this.joinDate = joinDate;
        this.woFromDate = woFromDate;
        this.woToDate = woToDate;
        this.updatedTime = updatedTime;
    }

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

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getWoNumber() {
        return woNumber;
    }

    public void setWoNumber(String woNumber) {
        this.woNumber = woNumber;
    }

    public String getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(String projectNo) {
        this.projectNo = projectNo;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public LocalDate getWoFromDate() {
        return woFromDate;
    }

    public void setWoFromDate(LocalDate woFromDate) {
        this.woFromDate = woFromDate;
    }

    public LocalDate getWoToDate() {
        return woToDate;
    }

    public void setWoToDate(LocalDate woToDate) {
        this.woToDate = woToDate;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "workorder{" +
                "id=" + id +
                ", empId=" + empId +
                ", agency='" + agency + '\'' +
                ", woNumber='" + woNumber + '\'' +
                ", projectNo='" + projectNo + '\'' +
                ", designation='" + designation + '\'' +
                ", joinDate=" + joinDate +
                ", woFromDate=" + woFromDate +
                ", woToDate=" + woToDate +
                ", updatedTime=" + updatedTime +
                '}';
    }
}
