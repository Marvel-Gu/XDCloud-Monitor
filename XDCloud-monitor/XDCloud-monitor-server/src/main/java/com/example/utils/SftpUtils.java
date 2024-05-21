package com.example.utils;

import com.example.entity.vo.request.SftpConnectionVO;
import com.example.entity.vo.response.FileDetailVO;
import com.example.entity.vo.response.SftpFileInfo;
import com.jcraft.jsch.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class SftpUtils {
    public static Map<String, SftpInfo> sftpMap = new ConcurrentHashMap<>();

    public String getSftpConnection(SftpConnectionVO vo) {
        // 定义一个 JSch 对象，用于执行 SFTP 操作
        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        ChannelSftp sftp = null;
        try {
            log.info("准备连接 {}，用户名 {}，密码 {}，端口 {}", vo.getHost(), vo.getUsername(), vo.getPassword(), vo.getPort());
            // 通过 JSch 对象，根据用户名，地址，端口，获取一个 SFTP 会话对象
            session = jsch.getSession(vo.getUsername(), vo.getHost(), vo.getPort());
            // 设置 SFTP 会话的密码
            session.setPassword(vo.getPassword());
            // 设置 SFTP 会话的配置，关闭严格的主机密钥检查
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(5000);
            // 连接 SFTP 会话

            session.connect();
            // 通过 SFTP 会话，打开一个 SFTP 通道
            channel = session.openChannel("sftp");
//            channel.connect(3000); // 连接 shell 通道，设置超时时间为 1 秒
            // 连接 SFTP 通道
            channel.connect();
            // 把 SFTP 通道转换成 ChannelSftp 对象
            sftp = (ChannelSftp) channel;
            sftpMap.put(vo.getHost(), new SftpInfo(session, channel, sftp));
            log.info("{} 已经连接", vo.getHost());
            cd(vo.getHost(), "/");
            return null;
        } catch (JSchException e) { // 捕获 JSchException 异常，表示 Sftp 连接出现问题
            String message = e.getMessage();
            if (message.equals("Auth fail")) { // 如果异常信息是 Auth fail，表示 Sftp 认证失败，用户名或密码错误
                log.error("连接Sftp失败，用户名或密码错误，登录失败");
                return "登录Sftp失败，用户名或密码错误";
            } else if (message.contains("Connection refused")) { // 如果异常信息包含 Connection refused，表示 Sftp 连接被拒绝，可能是没有启动 Sftp 服务或是放开端口
                log.error("连接Sftp失败，连接被拒绝，可能是没有启动Sftp服务或是放开端口");
                return "连接被拒绝，可能是没有启动Sftp服务或是放开端口";
            }else if (message.contains("timeout")){
                log.error("连接Sftp失败，连接超时");
                return "连接Sftp失败，连接超时，可能是服务器没有在线或是放开端口";
            } else { // 如果异常信息是其他情况，表示 Sftp 连接出现其他错误
                log.error("连接Sftp时出现错误", e);
                return "连接Sftp时出现错误";
            }
        }
    }



    // 将时间戳转换成 LocalDateTime 对象
    private LocalDateTime convertToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
    }


    public List<SftpFileInfo> listFiles(String host, String path) throws JSchException, SftpException {
        ChannelSftp sftp = getChannelSftp(host);
        try {
            if (path != null) sftp.cd(path);
        }catch (SftpException e){
            throw new com.example.controller.exception.SftpException(400, "目录不存在");
        }
        // 获取当前目录下的所有文件

        Vector<ChannelSftp.LsEntry> entries = sftp.ls(".");

        // 将 LsEntry 转换为 SftpFileInfo 对象
        List<SftpFileInfo> fileInfos = new ArrayList<>();
        for (ChannelSftp.LsEntry entry : entries) {
            SftpFileInfo fileInfo = new SftpFileInfo();
            fileInfo.setName(entry.getFilename());
            fileInfo.setSize(entry.getAttrs().getSize());
            fileInfo.setModifyTime(convertToLocalDateTime(entry.getAttrs().getMTime()));
            fileInfo.setDirectory(entry.getAttrs().isDir()); // 判断是否为文件夹
            fileInfos.add(fileInfo);
        }

        return fileInfos;
    }

    public void cd(String host, String path) {
        ChannelSftp sftp = getChannelSftp(host);
        try {
            sftp.cd(path);
        } catch (SftpException e) {
            throw new com.example.controller.exception.SftpException(400, "目录不存在");
        }
    }

    public String pwd(String host) {
        ChannelSftp sftp = getChannelSftp(host);
        try {
            return sftp.pwd();
        }catch (SftpException e){
            throw new com.example.controller.exception.SftpException(400, "目录不存在,请尝试断开连接并刷新");
        }
    }


    public void deleteFiles(String host, List<String> files) {
        ChannelSftp sftp = getChannelSftp(host);
        for (String file : files) {
            try {
                deleteFileOrDirectory(sftp, file);
            } catch (SftpException e) {
                throw new com.example.controller.exception.SftpException(400, "删除失败");
            }
        }
    }

    private void deleteFileOrDirectory(ChannelSftp sftp, String file) throws SftpException {
        SftpATTRS attrs = sftp.lstat(file);
        if (attrs.isDir()) {
            Vector<ChannelSftp.LsEntry> list = sftp.ls(file);
            for (ChannelSftp.LsEntry entry : list) {
                if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
                    deleteFileOrDirectory(sftp, file + "/" + entry.getFilename());
                }
            }
            sftp.rmdir(file);
        } else {
            sftp.rm(file);
        }
    }

    public FileDetailVO getFileDetail(String host, String file) {
        ChannelSftp sftp = getChannelSftp(host);
        FileDetailVO fileDetail = new FileDetailVO();
        try {
            String encodedFile = new String(file.getBytes(), StandardCharsets.UTF_8);
            SftpATTRS attrs = sftp.lstat(encodedFile);
            fileDetail.setFilename(file);
            fileDetail.setSize(attrs.getSize());
            fileDetail.setLastModifiedTime(attrs.getMTime());
            fileDetail.setPermissions(attrs.getPermissions());
            fileDetail.setOwner(attrs.getUId());
            fileDetail.setGroup(attrs.getGId());
            String permissionString = Integer.toOctalString(attrs.getPermissions());
            fileDetail.setPermissionsStr(permissionString);
            if (attrs.isDir()) {
                fileDetail.setFileType("Directory");
            } else {
                fileDetail.setFileType("File");
            }
        } catch (SftpException e) {
            e.printStackTrace();
            throw new com.example.controller.exception.SftpException(401, "获取文件详情失败");
        }
        return fileDetail;
    }
    private static ChannelSftp getChannelSftp(String host) {
        SftpInfo sftpInfo = sftpMap.get(host);
        if (sftpInfo == null) {
            throw new com.example.controller.exception.SftpException(400, "Sftp连接不存在");
        }
        ChannelSftp sftp = sftpInfo.getSftp();
        return sftp;

//        Session session = sftpMap.get(host).getSession();
//        Channel channel = sftpMap.get(host).getChannel();
//        return (ChannelSftp) channel;
    }

    public void closeConnection(String host) {
        // 获取指定host的SFTP连接信息
        SftpInfo sftpInfo = sftpMap.get(host);
        if (sftpInfo != null) {
            // 通过连接信息获取会话和通道
            Session session = sftpInfo.getSession();
            Channel channel = sftpInfo.getChannel();
            try {
                // 关闭SFTP通道
                if (channel != null && channel.isConnected()) {
                    channel.disconnect();
                }
                // 关闭会话
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            } finally {
                // 从缓存连接池中移除
                sftpMap.remove(host);
                log.info("SFTP connection to {} closed", host);
            }
        }
    }

    public void mkdir(String host, String path) {
        ChannelSftp sftp = getChannelSftp(host);
        try {
            sftp.mkdir(path);
        } catch (SftpException e) {
            throw new com.example.controller.exception.SftpException(400, "建立失败：目录已存在或权限不足");
        }
    }

    public void mkfile(String host, String name) {
        ChannelSftp sftp = getChannelSftp(host);
        try {
            sftp.put(new ByteArrayInputStream(new byte[0]), name);
        } catch (SftpException e) {
            throw new com.example.controller.exception.SftpException(400, "建立失败：目录已存在或权限不足");
        }
    }


    public void handleFileUpload(MultipartFile file, String host) {
        ChannelSftp sftp = getChannelSftp(host);
        try {
            sftp.put(file.getInputStream(), file.getOriginalFilename());
        } catch (SftpException | IOException e) {
            throw new com.example.controller.exception.SftpException(400, "上传失败：文件已存在或权限不足");
        }
    }


    private static final ReentrantLock downLoadLock = new ReentrantLock();

    public ResponseEntity<InputStreamResource> downloadFile(String host, String file) {
        downLoadLock.lock();
        try {
            ChannelSftp sftp = getChannelSftp(host);

            SftpATTRS attrs = sftp.lstat(file);
            if (attrs.isDir()) {
//                return ResponseEntity.badRequest().body(null);
                throw new com.example.controller.exception.SftpException(400, "无法下载文件夹，请进入文件夹内部批量下载");
            }
            InputStream inputStream = sftp.get(file);
            byte[] fileBytes = IOUtils.toByteArray(inputStream);

            HttpHeaders headers = new HttpHeaders();
            String encodedFilename = URLEncoder.encode(file, StandardCharsets.UTF_8.name());//用utf8编码 否则中文文件名无法传输
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileBytes.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(new ByteArrayInputStream(fileBytes)));
        } catch (IOException | SftpException e) {
            log.error("下载文件发生异常：{}",e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }finally {
            downLoadLock.unlock();
        }
    }

    @Data
    public class SftpInfo {
        private final Session session;
        private final Channel channel;
        private final ChannelSftp sftp;

        public SftpInfo(Session session, Channel channel, ChannelSftp sftp) {
            this.session = session;
            this.channel = channel;
            this.sftp = sftp;
        }
    }
}

