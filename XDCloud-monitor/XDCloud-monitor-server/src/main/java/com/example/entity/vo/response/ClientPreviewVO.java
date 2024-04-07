package com.example.entity.vo.response;

import lombok.Data;

@Data
public class ClientPreviewVO {
    int id;
    //是否运行中
    boolean online;
    String name;
    //地区
    String location;
    String osName;
    String osVersion;
    String ip;
    String cpuName;
    int cpuCore;
    double memory;
    double cpuUsage;
    double cpuTemperature;
    double memoryUsage;
    double networkUpload;
    double networkDownload;
}
