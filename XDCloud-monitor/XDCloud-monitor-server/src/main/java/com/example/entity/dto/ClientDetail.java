package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("db_client_detail")
public class ClientDetail {
    @TableId
    Integer id;
    String osArch;
    String osName;
    String osVersion;
    int osBit;
    String cpuName;
    int cpuCore;
    double memory;
    double disk;
    String ip;
}
