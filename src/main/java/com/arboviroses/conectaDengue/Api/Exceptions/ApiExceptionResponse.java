package com.arboviroses.conectaDengue.Api.Exceptions;

import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiExceptionResponse {
    private HttpStatus httpStatus;
    private List<String> errors;

    public ApiExceptionResponse(HttpStatus httpStatus, List<String> errors)
    {
        super();
        this.setHttpStatus(httpStatus);
        this.setErrors(errors);
    }

    public ApiExceptionResponse(HttpStatus httpStatus, String error)
    {
        super();
        this.setHttpStatus(httpStatus);
        this.setErrors(Arrays.asList(error));
    }
}
