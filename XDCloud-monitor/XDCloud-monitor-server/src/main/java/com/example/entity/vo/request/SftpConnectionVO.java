package com.example.entity.vo.request;

import lombok.Data;

@Data
public class SftpConnectionVO {
    String username;
    String host;
    int port = 22;
    String password;
}
