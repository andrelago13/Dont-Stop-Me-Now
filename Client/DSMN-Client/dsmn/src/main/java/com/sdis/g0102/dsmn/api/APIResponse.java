package com.sdis.g0102.dsmn.api;

/**
 * Created by Gustavo on 20/05/2016.
 */
public class APIResponse {
    private int code;
    private byte[] message;

    public APIResponse(int code, byte[] message)
    {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public byte[] getMessage() {
        return message;
    }
}
