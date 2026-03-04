package com.arboviroses.conectaDengue.Api.DTO.response;
import com.arboviroses.conectaDengue.Domain.Entities.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUser {
    int id;
    String name;
    String cpf;
    
    public RegisterUser(User user) 
    {
        this.id = user.getId();
        this.name = user.getFullName();
        this.cpf = user.getCpf();
    }
}
