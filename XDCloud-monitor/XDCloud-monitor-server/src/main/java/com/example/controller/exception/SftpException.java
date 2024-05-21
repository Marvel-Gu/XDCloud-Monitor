package com.example.controller.exception;

import lombok.Getter;

@Getter
public class SftpException extends RuntimeException{
    private Integer code;
    public String message;

    public SftpException(Integer code, String message) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public SftpException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
