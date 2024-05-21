package com.example.entity.vo.response;

import lombok.Data;

@Data
public class FileDetailVO {
    private String filename;
    private long size;
    private int lastModifiedTime;
    private int permissions;
    private String fileType;
    private Integer owner;
    private Integer group;
    private String permissionsStr;
}
