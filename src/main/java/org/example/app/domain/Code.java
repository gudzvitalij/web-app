package org.example.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Code {
    private String code;
    private long userId;
}
