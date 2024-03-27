package com.example.websocket;

        import com.example.entity.dto.ClientDetail;
        import com.example.entity.dto.ClientSsh;
        import com.example.mapper.ClientDetailMapper;
        import com.example.mapper.ClientSshMapper;
        import com.jcraft.jsch.ChannelShell;
        import com.jcraft.jsch.JSch;
        import com.jcraft.jsch.JSchException;
        import jakarta.annotation.Resource;
        import jakarta.websocket.*;
        import jakarta.websocket.server.PathParam;
        import jakarta.websocket.server.ServerEndpoint;
        import lombok.extern.slf4j.Slf4j;
        import org.springframework.stereotype.Component;

        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.nio.charset.StandardCharsets;
        import java.util.Arrays;
        import java.util.Map;
        import java.util.concurrent.ConcurrentHashMap;
        import java.util.concurrent.ExecutorService;
        import java.util.concurrent.Executors;

@Slf4j
@Component
@ServerEndpoint("/terminal/{clientId}")
public class TerminalWebSocket {

    private static ClientDetailMapper detailMapper;

    @Resource
    public void setDetailMapper(ClientDetailMapper detailMapper) {
        TerminalWebSocket.detailMapper = detailMapper;
    }

    private static ClientSshMapper sshMapper;

    @Resource
    public void setSshMapper(ClientSshMapper sshMapper) {
        TerminalWebSocket.sshMapper = sshMapper;
    }

    private static final Map<Session, Shell> sessionMap = new ConcurrentHashMap<>();
    private final ExecutorService service = Executors.newSingleThreadExecutor();

    @OnOpen
    public void onOpen(Session session,
                       @PathParam(value = "clientId") String clientId) throws Exception {
        ClientDetail detail = detailMapper.selectById(clientId);
        ClientSsh ssh = sshMapper.selectById(clientId);
        if(detail == null || ssh == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "无法识别此主机"));
            return;
        }
        if(this.createSshConnection(session, ssh, detail.getIp())) {
            log.info("主机 {} 的SSH连接已创建", detail.getIp());
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        Shell shell = sessionMap.get(session);
        OutputStream output = shell.output;
        output.write(message.getBytes(StandardCharsets.UTF_8));
        output.flush();
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        Shell shell = sessionMap.get(session);
        if(shell != null) {
            shell.close();
            sessionMap.remove(session);
            log.info("主机 {} 的SSH连接已断开", shell.js.getHost());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        log.error("用户WebSocket连接出现错误", error);
        session.close();
    }

    private boolean createSshConnection(Session session, ClientSsh ssh, String ip) throws IOException{
        try {
            JSch jSch = new JSch();
            com.jcraft.jsch.Session js = jSch.getSession(ssh.getUsername(), ip, ssh.getPort());
            js.setPassword(ssh.getPassword());
            js.setConfig("StrictHostKeyChecking", "no");
            js.setTimeout(3000);
            js.connect();
            ChannelShell channel = (ChannelShell) js.openChannel("shell");
            channel.setPtyType("xterm");
            channel.connect(1000);
            sessionMap.put(session, new Shell(session, js, channel));
            return true;
        } catch (JSchException e) {
            String message = e.getMessage();
            if(message.equals("Auth fail")) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
                        "登录SSH失败，用户名或密码错误"));
                log.error("连接SSH失败，用户名或密码错误，登录失败");
            } else if(message.contains("Connection refused")) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
                        "连接被拒绝，可能是没有启动SSH服务或是放开端口"));
                log.error("连接SSH失败，连接被拒绝，可能是没有启动SSH服务或是放开端口");
            } else {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, message));
                log.error("连接SSH时出现错误", e);
            }
        }
        return false;
    }

    private class Shell {
        private final Session session;
        private final com.jcraft.jsch.Session js;
        private final ChannelShell channel;
        private final InputStream input;
        private final OutputStream output;

        public Shell(Session session, com.jcraft.jsch.Session js, ChannelShell channel) throws IOException {
            this.js = js;
            this.session = session;
            this.channel = channel;
            this.input = channel.getInputStream();
            this.output = channel.getOutputStream();
            service.submit(this::read);
        }

        private void read() {
            try {
                byte[] buffer = new byte[1024 * 1024];
                int i;
                while ((i = input.read(buffer)) != -1) {
                    String text = new String(Arrays.copyOfRange(buffer, 0, i), StandardCharsets.UTF_8);
                    session.getBasicRemote().sendText(text);
                }
            } catch (Exception e) {
                log.error("读取SSH输入流时出现问题", e);
            }
        }

        public void close() throws IOException {
            input.close();
            output.close();
            channel.disconnect();
            js.disconnect();
            service.shutdown();
        }
    }
}

//
//import com.example.entity.dto.ClientDetail;
//import com.example.entity.dto.ClientSsh;
//import com.example.mapper.ClientDetailMapper;
//import com.example.mapper.ClientSshMapper;
//import jakarta.annotation.Resource;
//import jakarta.websocket.*;
//import jakarta.websocket.server.PathParam;
//import jakarta.websocket.server.ServerEndpoint;
//import lombok.extern.slf4j.Slf4j;
//import net.schmizz.sshj.SSHClient;
//import net.schmizz.sshj.common.LoggerFactory;
//import net.schmizz.sshj.common.SSHException;
//import net.schmizz.sshj.common.StreamCopier;
//import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.nio.charset.StandardCharsets;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@Slf4j
//@Component
//@ServerEndpoint("/terminal/{clientId}")
//public class TerminalWebSocket {
//
//    private static ClientDetailMapper detailMapper;
//
//    @Resource
//    public void setDetailMapper(ClientDetailMapper detailMapper) {
//        TerminalWebSocket.detailMapper = detailMapper;
//    }
//
//    private static ClientSshMapper sshMapper;
//
//    @Resource
//    public void setSshMapper(ClientSshMapper sshMapper) {
//        TerminalWebSocket.sshMapper = sshMapper;
//    }
//
//    private static final Map<Session, Shell> sessionMap = new ConcurrentHashMap<>();
//    private final ExecutorService service = Executors.newSingleThreadExecutor();
//
//    @OnOpen
//    public void onOpen(Session session,
//                        @PathParam(value = "clientId") String clientId) throws Exception {
//        ClientDetail detail = detailMapper.selectById(clientId);
//        ClientSsh ssh = sshMapper.selectById(clientId);
//        if(detail == null || ssh == null) {
//            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "无法识别此主机"));
//            return;
//        }
//        if(this.createSshConnection(session, ssh, detail.getIp())) {
//            log.info("主机 {} 的SSH连接已创建", detail.getIp());
//        }
//    }
//
//    @OnMessage
//    public void onMessage(Session session, String message) throws IOException {
//        Shell shell = sessionMap.get(session);
//        OutputStream output = shell.output;
//        output.write(message.getBytes(StandardCharsets.UTF_8));
//        output.flush();
//    }
//
//    @OnClose
//    public void onClose(Session session) throws IOException {
//        Shell shell = sessionMap.get(session);
//        if(shell != null) {
//            shell.close();
//            sessionMap.remove(session);
//            log.info("主机 {} 的SSH连接已断开", shell.js.getID());
//        }
//    }
//
//
//
//    @OnError
//    public void onError(Session session, Throwable error) throws IOException {
//        log.error("用户WebSocket连接出现错误", error);
//        session.close();
//    }
//
//    private boolean createSshConnection(Session session, ClientSsh ssh, String ip) throws IOException{
//        try {
//            SSHClient sshClient=new SSHClient();
//            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
//            sshClient.connect(ip, ssh.getPort());
//            sshClient.authPassword(ssh.getUsername(), ssh.getPassword());
//            sshClient.setConnectTimeout(3000);
//            sshClient.useCompression();
//            // 创建Session
//            net.schmizz.sshj.connection.channel.direct.Session js = sshClient.startSession();
////            js.setEnvVar("TERM","xterm");
//            // 打开Shell
//            js.allocatePTY("xterm", 80, 24, 0, 0, Collections.emptyMap());
//
//            net.schmizz.sshj.connection.channel.direct.Session.Shell channel=  js.startShell();
//
//
//
//            sessionMap.put(session, new Shell(session, js,channel));
//
//            new StreamCopier(channel.getInputStream(), System.out, LoggerFactory.DEFAULT)
//                    .bufSize(channel.getLocalMaxPacketSize())
//                    .spawn("stdout");
//
//
//            new StreamCopier(channel.getErrorStream(), System.err, LoggerFactory.DEFAULT)
//                    .bufSize(channel.getLocalMaxPacketSize())
//                    .spawn("stderr");
//
//            new StreamCopier(System.in, channel.getOutputStream(), LoggerFactory.DEFAULT)
//                    .bufSize(channel.getRemoteMaxPacketSize())
//                    .copy();
//            return true;
//        } catch (SSHException e) {
//            String message = e.getMessage();
//            if(message.equals("Auth fail")) {
//                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
//                        "登录SSH失败，用户名或密码错误"));
//                log.error("连接SSH失败，用户名或密码错误，登录失败");
//            } else if(message.contains("Connection refused")) {
//                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
//                        "连接被拒绝，可能是没有启动SSH服务或是放开端口"));
//                log.error("连接SSH失败，连接被拒绝，可能是没有启动SSH服务或是放开端口");
//            } else {
//                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, message));
//                log.error("连接SSH时出现错误", e);
//            }
//        }
//        return false;
//    }
//
//
//    private class Shell {
//        private final Session session;
//        private final net.schmizz.sshj.connection.channel.direct.Session js;
//        net.schmizz.sshj.connection.channel.direct.Session.Shell channel;
//        private final InputStream input;
//        private final OutputStream output;
//
//        public Shell(Session session, net.schmizz.sshj.connection.channel.direct.Session js, net.schmizz.sshj.connection.channel.direct.Session.Shell channel) throws IOException {
//            this.js = js;
//            this.session = session;
//            this.channel=channel;
//            this.input = channel.getInputStream();
//            this.output = channel.getOutputStream();
//            service.submit(this::read);
//        }
//
//
//        private void read() {
//            try {
//                byte[] buffer = new byte[1024 * 1024];
//                int i;
//                while ((i = input.read(buffer)) != -1) {
//                    String text = new String(Arrays.copyOfRange(buffer, 0, i), StandardCharsets.UTF_8);
//                    session.getBasicRemote().sendText(text);
//                }
//            } catch (Exception e) {
//                log.error("读取SSH输入流时出现问题", e);
//            }
//        }
//
//        public void close() throws IOException {
//            input.close();
//            output.close();
//            channel.close();
//            js.close();
//            service.shutdown();
//        }
//    }
//}





