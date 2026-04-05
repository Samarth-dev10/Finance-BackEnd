package com.finance.model;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class FinError {

    private String code;

    private String field;

    private String message;
}