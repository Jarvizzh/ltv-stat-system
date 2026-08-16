package com.ltv.stat.controller;

import com.ltv.stat.dto.LoginRequestDto;
import com.ltv.stat.dto.LoginResponseDto;
import com.ltv.stat.dto.TokenInfo;
import com.ltv.stat.entity.SysUser;
import com.ltv.stat.service.UserService;
import com.ltv.stat.util.TokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @Value("${app.auth.token-expire-days:7}")
    private int tokenExpireDays;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto body) {
        String username = body != null ? body.getUsername() : null;
        String password = body != null ? body.getPassword() : null;

        LoginResponseDto response = new LoginResponseDto();
        if (username == null || password == null) {
            response.setCode(400);
            response.setMsg("请输入用户名和密码");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<SysUser> userOpt = userService.findByUsername(username.trim());
        if (userOpt.isPresent()) {
            SysUser user = userOpt.get();
            if (user.getStatus() != null && user.getStatus() == 0) {
                response.setCode(403);
                response.setMsg("该账号已被禁用");
                return ResponseEntity.status(403).body(response);
            }

            if (userService.validatePassword(user, password)) {
                String token = TokenUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), tokenExpireDays);
                response.setCode(0);
                response.setMsg("登录成功");
                response.setToken(token);
                response.setUserId(user.getId());
                response.setUsername(user.getUsername());
                response.setRole(user.getRole());
                response.setExpireDays(tokenExpireDays);
                return ResponseEntity.ok(response);
            }
        }

        response.setCode(401);
        response.setMsg("账号或密码错误");
        return ResponseEntity.status(401).body(response);
    }

    @GetMapping("/check")
    public ResponseEntity<LoginResponseDto> checkAuth(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        } else if (authHeader != null) {
            token = authHeader.trim();
        }

        TokenInfo tokenInfo = TokenUtil.parseToken(token);
        LoginResponseDto response = new LoginResponseDto();
        if (tokenInfo.isValid()) {
            response.setCode(0);
            response.setMsg("Token 有效");
            response.setUserId(tokenInfo.getUserId());
            response.setUsername(tokenInfo.getUsername());
            response.setRole(tokenInfo.getRole());
            return ResponseEntity.ok(response);
        } else {
            response.setCode(401);
            response.setMsg("Token 已过期或无效");
            return ResponseEntity.status(401).body(response);
        }
    }
}
