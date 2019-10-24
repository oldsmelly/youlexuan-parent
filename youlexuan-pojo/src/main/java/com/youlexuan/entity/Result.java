package com.youlexuan.entity;

import java.io.Serializable;

public class Result implements Serializable {

    //成功与否的表示
    private boolean success;
    private String message;

    public Result() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Result(boolean success, String message) {

        this.success = success;
        this.message = message;
    }
}
