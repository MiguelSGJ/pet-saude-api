package com.arboviroses.conectaDengue.Api.Exceptions;

import lombok.Getter;

@Getter
public class PasswordNotMatchException extends Exception {
    private int code = 404;
    
    public PasswordNotMatchException(String message) {
        super(message);
    }
}
