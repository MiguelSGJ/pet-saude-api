package com.arboviroses.conectaDengue.Api.DTO.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SuccessResponseDTO<E> {
    private String message;
    private E data;

    public SuccessResponseDTO(E data, String message) 
    {
        this.message = message == null ? "success" : message;
        this.setData(data);
    }

    public static <E> SuccessResponseDTO<E> setResponse(E object, String message) 
    {
        return new SuccessResponseDTO<E>(object, message);
    }
}
