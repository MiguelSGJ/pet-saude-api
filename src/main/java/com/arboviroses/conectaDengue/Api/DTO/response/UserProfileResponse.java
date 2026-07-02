package com.arboviroses.conectaDengue.Api.DTO.response;

import com.arboviroses.conectaDengue.Domain.Entities.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileResponse {
    private Integer id;
    private String fullName;
    private String cpf;
    private String role;

    public UserProfileResponse(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.cpf = user.getCpf();
        this.role = user.getRole().name();
    }
}
