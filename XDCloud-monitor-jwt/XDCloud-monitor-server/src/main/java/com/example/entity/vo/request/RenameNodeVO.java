package com.example.entity.vo.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RenameNodeVO {
    int id;
    @Length(min = 1, max = 10)
    String node;
    @Pattern(regexp = "(cn|hk|jp|us|sg|kr|de)")
    String location;
}
