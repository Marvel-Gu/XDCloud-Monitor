package com.example.entity.vo.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SftpFileInfo {
    private String name;
    private long size;
    private LocalDateTime modifyTime;
    private boolean isDirectory;
}
