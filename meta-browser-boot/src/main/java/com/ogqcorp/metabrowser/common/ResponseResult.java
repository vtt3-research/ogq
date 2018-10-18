package com.ogqcorp.metabrowser.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseResult {
    private boolean success;
    private int code;
    private Object data;
    private String message;

    public ResponseResult(boolean success, int code, Object data,String message){
        this.success = success;
        this.code = code;
        this.data = data;
        this.message = message;
    }
}
