package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.request.DeleteFileRequest;
import com.example.entity.vo.request.NewFileRequest;
import com.example.entity.vo.request.SftpConnectionVO;
import com.example.entity.vo.response.FileDetailVO;
import com.example.entity.vo.response.SftpFileInfo;
import com.example.service.SftpService;
import com.example.utils.SftpUtils;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import jakarta.annotation.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/sftp")
public class SftpController {

    @Resource
    SftpService sftpService;

    @Resource
    SftpUtils sftpUtils;

    @PostMapping("/connect")
    public RestBean<Void> connect(@RequestBody SftpConnectionVO vo) {
        String connected = sftpService.connect(vo);
        return connected == null ? RestBean.success() : RestBean.failure(400, connected);
    }

    @GetMapping("/files")
    public RestBean<List<SftpFileInfo>> files(@RequestParam String host, @RequestParam(required = false) String path) throws JSchException, SftpException {
        List<SftpFileInfo> sftpFileInfos = sftpUtils.listFiles(host, path);
        return RestBean.success(sftpFileInfos);
    }

    @PostMapping("/close")
    public RestBean<Void> close(@RequestBody String host) {
        host = host.replace("http://", "").replace("=", "");
        sftpUtils.closeConnection(host);
        return RestBean.success();
    }

    @GetMapping("/pwd")
    public RestBean<String> pwd(@RequestParam String host) {
        return RestBean.success(sftpUtils.pwd(host));
    }

    @GetMapping("/cd")
    public RestBean<Void> cd(@RequestParam String host, @RequestParam String path) {
        sftpUtils.cd(host, path);
        return RestBean.success();
    }

    @PostMapping("/mkdir")
    public RestBean<Void> newFile(@RequestBody NewFileRequest request) {
        sftpUtils.mkdir(request.getHost(), request.getPath());
        return RestBean.success();
    }

    @PostMapping("/mkfile")
    public RestBean<Void> mkfile(@RequestBody NewFileRequest request) {
        sftpUtils.mkfile(request.getHost(), request.getPath());
        return RestBean.success();
    }

    @PostMapping("/upload")
    public RestBean<Void> handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam String host) {
        sftpUtils.handleFileUpload(file,host);
        return RestBean.success();
    }



    @GetMapping("/downloadFile")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam String host, @RequestParam String file) {
        return sftpUtils.downloadFile(host, file);
    }

    @PostMapping("/delete")
    public RestBean<Void> deleteFile(@RequestBody DeleteFileRequest request) {
        sftpUtils.deleteFiles(request.getHost(), request.getFiles());
        return RestBean.success();
    }

    @GetMapping("/fileDetail")
    public RestBean<FileDetailVO> fileDetail(@RequestParam String host, @RequestParam String file) {
        return RestBean.success(sftpUtils.getFileDetail(host, file));
    }

}
