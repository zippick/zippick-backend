package zippick.auth.service;

import zippick.auth.dto.request.LoginRequest;
import zippick.auth.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    void logout(String token);
}
