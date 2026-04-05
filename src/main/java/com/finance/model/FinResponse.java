package com.finance.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinResponse<T> {

    private boolean success;

    private String message;

    private T data;

    private List<FinError> errors = new ArrayList<>();


    public static <T> FinResponse<T> success(String message, T data) {
        FinResponse<T> response = new FinResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> FinResponse<T> success(String message) {
        return success(message, null);
    }

    public static <T> FinResponse<T> failure(String message, FinError error) {
        FinResponse<T> response = new FinResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.getErrors().add(error);
        return response;
    }

    public static <T> FinResponse<T> failure(String message, List<FinError> errors) {
        FinResponse<T> response = new FinResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrors(errors);
        return response;
    }

    public void addError(FinError error) {
        this.errors.add(error);
        this.success = false;
    }
}