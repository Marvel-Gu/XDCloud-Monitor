package com.example.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)//启用链式访问器,允许开发者通过链式调用方法来构建和配置对象
public class BaseDetail {
    //系统架构
    String osArch;
    //系统名称
    String osName;
    //系统版本
    String osVersion;
    //系统位数
    int osBit;
    //cpu名称
    String cpuName;
    //cpu核心数
    int cpuCore;
    //内存大小
    double memory;
    //磁盘大小
    double disk;
    //ip地址
    String ip;
}
