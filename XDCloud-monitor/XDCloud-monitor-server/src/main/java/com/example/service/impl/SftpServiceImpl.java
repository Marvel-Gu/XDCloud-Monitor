package com.example.service.impl;

import com.example.entity.vo.request.SftpConnectionVO;
import com.example.entity.vo.response.SftpFileInfo;
import com.example.service.SftpService;
import com.example.utils.SftpUtils;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static com.example.utils.SftpUtils.sftpMap;

@Service
public class SftpServiceImpl implements SftpService {
    @Autowired
    SftpUtils sftpUtils;
    @Override
    public String connect(SftpConnectionVO vo) {
        return sftpUtils.getSftpConnection(vo);
    }

    @Override
    public List<SftpFileInfo> listFiles(String host) {
        return null;
    }

}
