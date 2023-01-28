package com.example.server.dto;

import lombok.Data;

@Data
public class SignInRequest {
    private String username;
    private String password;
}
