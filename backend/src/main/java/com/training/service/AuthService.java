package com.training.service;

import com.training.model.dto.LoginRequest;
import com.training.model.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request, String ip);
}
