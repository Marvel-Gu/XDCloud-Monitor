package com.example.service;

import com.example.entity.vo.request.SftpConnectionVO;
import com.example.entity.vo.response.SftpFileInfo;

import java.util.List;

public interface SftpService {
    String connect(SftpConnectionVO vo);
    List<SftpFileInfo> listFiles(String host);
}
