package com.bruhmosuki.dhoomaKedu.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_master")
public class employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 25)
    private String first_name;

    @Column(name = "last_name", nullable = false, length = 25)
    private String last_name;

    @Column(name = "as_group", nullable = false, length = 25)
    private String as_group;

    @Column(name = "sys_ip", nullable = false, length = 20)
    private String sys_ip;

    @Column(name = "is_admin", nullable = false)
    private Boolean is_admin;

    @Column(name = "updated_time", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updated_time;

    public employee() {
    }

    public employee(Long id, String first_name, String last_name, String as_group, String sys_ip, Boolean is_admin, LocalDateTime updated_time) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.as_group = as_group;
        this.sys_ip = sys_ip;
        this.is_admin = is_admin;
        this.updated_time = updated_time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getAs_group() {
        return as_group;
    }

    public void setAs_group(String as_group) {
        this.as_group = as_group;
    }

    public String getSys_ip() {
        return sys_ip;
    }

    public void setSys_ip(String sys_ip) {
        this.sys_ip = sys_ip;
    }

    public Boolean getIs_admin() {
        return is_admin;
    }

    public void setIs_admin(Boolean is_admin) {
        this.is_admin = is_admin;
    }

    public LocalDateTime getUpdated_time() {
        return updated_time;
    }

    public void setUpdated_time(LocalDateTime updated_time) {
        this.updated_time = updated_time;
    }

    @Override
    public String toString() {
        return "employee{" +
                "id=" + id +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", as_group='" + as_group + '\'' +
                ", sys_ip='" + sys_ip + '\'' +
                ", is_admin=" + is_admin +
                ", updated_time=" + updated_time +
                '}';
    }
}
