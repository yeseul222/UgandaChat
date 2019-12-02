package com.example.chat;

public enum ConnectionEnum { //여러 클래스 에서 공통으로 접근하는  상수 선언
    //DB 관리자의  IP주소는 변하지 않는 상수로 고정해 놓는다.
    ServerIP("192.168.15.116");  // DB 관리자의 IP주소

    private String ip;
    private String serverIP;

    ConnectionEnum(String s) {
        this.ip = s;
    }

    public String getIp() {
        return ip;
    }

    public String getServerIP() {
        return serverIP;
    }
}
