package com.bruhmosuki.dhoomaKedu.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
@Table(name = "leave_list")
public class leaves {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private employee empId;

    @Column(name = "leave_list", nullable = false, length = 100)
    private String leaveStr;

    @Column(name="half_day_leave_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int halfDayLeaveCnt;

    @Column(name = "leave_month", nullable = false)
    private int leaveMpMonth;

    @Column(name = "leave_year", nullable = false)
    private int leaveMpYear;

    @Lob
    @Column(name = "at_side_a", columnDefinition = "LONGBLOB", nullable = true)
    private byte[] atSideA;

    @Lob
    @Column(name = "at_side_b", columnDefinition = "LONGBLOB", nullable = true)
    private byte[] atSideB;

    @UpdateTimestamp
    private LocalDateTime updatedTime;


    public leaves() {
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

    public byte[] getAtSideA() {
        return atSideA;
    }

    public void setAtSideA(byte[] atSideA) {
        this.atSideA = atSideA;
    }

    public byte[] getAtSideB() {
        return atSideB;
    }

    public void setAtSideB(byte[] atSideB) {
        this.atSideB = atSideB;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }


    @Override
    public String toString() {
        return "leaves{" +
                "id=" + id +
                ", empId=" + empId +
                ", leaveStr='" + leaveStr + '\'' +
                ", halfDayLeaveCnt=" + halfDayLeaveCnt +
                ", leaveMpMonth=" + leaveMpMonth +
                ", leaveMpYear=" + leaveMpYear +
                ", atSideA=" + Arrays.toString(atSideA) +
                ", atSideB=" + Arrays.toString(atSideB) +
                ", updatedTime=" + updatedTime +
                '}';
    }
}
