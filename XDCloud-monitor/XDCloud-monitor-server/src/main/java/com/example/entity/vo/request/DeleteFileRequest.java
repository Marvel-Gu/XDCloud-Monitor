package com.example.entity.vo.request;

import lombok.Data;

import java.util.List;

@Data
public class DeleteFileRequest {
    private String host;
    private List<String> files;
}
