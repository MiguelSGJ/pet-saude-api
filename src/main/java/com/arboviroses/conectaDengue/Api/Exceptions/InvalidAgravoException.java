package com.arboviroses.conectaDengue.Api.Exceptions;

import lombok.Getter;

@Getter
public class InvalidAgravoException extends Exception {
    private int code = 404;
    public InvalidAgravoException(String message) {
        super(message);
    }
}
