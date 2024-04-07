package com.example.entity.vo.request;


import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * InfluxDB使用的对象
 *
 */
@Data
public class RuntimeDetailVO {
    @NotNull
    long timestamp;
    @NotNull
    double cpuUsage;
    @NotNull
    double cpuTemperature;
    @NotNull
    double memoryUsage;
    @NotNull
    double diskUsage;
    @NotNull
    double networkUpload;
    @NotNull
    double networkDownload;
    @NotNull
    double diskRead;
    @NotNull
    double diskWrite;
}
