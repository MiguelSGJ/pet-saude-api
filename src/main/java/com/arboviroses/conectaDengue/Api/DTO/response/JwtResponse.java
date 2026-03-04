package com.arboviroses.conectaDengue.Api.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class JwtResponse {
    private String jwtToken;
    private String token;
    private String fullName;
}
