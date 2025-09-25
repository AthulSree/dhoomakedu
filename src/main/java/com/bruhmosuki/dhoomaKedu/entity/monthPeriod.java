package com.bruhmosuki.dhoomaKedu.entity;

import jakarta.persistence.*;

@Entity
public class monthPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name="year", nullable = false)
    private int year;

    public monthPeriod() {
    }

    public monthPeriod(Long id, int month, int year) {
        this.id = id;
        this.month = month;
        this.year = year;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "monthPeriod{" +
                "id=" + id +
                ", month=" + month +
                ", year=" + year +
                '}';
    }
}
