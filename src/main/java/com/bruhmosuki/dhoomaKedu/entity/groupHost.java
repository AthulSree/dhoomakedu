package com.bruhmosuki.dhoomaKedu.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "group_host")
public class groupHost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host", nullable = false)
    String host;

    @Column(name = "user_name", nullable = false)
    String userName;

    @Column(name = "password", nullable = false)
    String password;

    @Column(name = "is_active", nullable = false)
    Boolean isActive;


    public groupHost() {
    }

    public groupHost(Long id, String host, String userName, String password, Boolean isActive) {
        this.id = id;
        this.host = host;
        this.userName = userName;
        this.password = password;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }


    @Override
    public String toString() {
        return "groupHost{" +
                "id=" + id +
                ", host='" + host + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
