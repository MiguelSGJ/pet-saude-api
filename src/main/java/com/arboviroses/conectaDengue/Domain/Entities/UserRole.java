package com.arboviroses.conectaDengue.Domain.Entities;

public enum UserRole {
    USER,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
