package com.example.entity.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SshConnectionVO {
    int id;
    int port;
    @NotNull
    @Length(min = 1)
    String username;
    @NotNull
    @Length(min = 1)
    String password;
}
