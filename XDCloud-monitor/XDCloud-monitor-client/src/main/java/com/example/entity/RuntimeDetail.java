package com.example.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RuntimeDetail {
    //时间戳
    long timestamp;
    //CPU使用率
    double cpuUsage;
    //CPU温度
    double cpuTemperature;
    //内存使用率
    double memoryUsage;
    //磁盘使用率
    double diskUsage;
    //网络上传速度
    double networkUpload;
    //网络下载速度
    double networkDownload;
    //磁盘读取速度
    double diskRead;
    //磁盘写入速度
    double diskWrite;
}
