package com.bruhmosuki.dhoomaKedu.service;

public class HostInfo {
    private String host;
    private String username;
    private String password;

    public HostInfo(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    // getters
    public String getHost() { return host; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
