package org.example.system1.servlet;

import org.example.system1.service.AuthService;
import org.example.system1.entity.User;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/api/auth/*")
public class AuthServlet extends BaseServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if ("/login".equals(pathInfo)) {
            handleLogin(req, resp);
        } else if ("/reset-password".equals(pathInfo)) {
            handleResetPassword(req, resp);
        } else {
            fail(resp, 404, "接口不存在");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if ("/userinfo".equals(pathInfo)) {
            Long userId = getUserId(req);
            if (userId != null) {
                User user = authService.getUserInfo(userId);
                success(resp, user);
            } else {
                fail(resp, 401, "未登录");
            }
        } else {
            fail(resp, 404, "接口不存在");
        }
    }

    private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, String> data = parseBody(req, Map.class);
            String username = data.get("username");
            String newPassword = data.get("newPassword");
            String confirmPassword = data.get("confirmPassword");

            if (username == null || username.trim().isEmpty()) {
                fail(resp, 400, "请输入用户名");
                return;
            }
            if (newPassword == null || newPassword.length() < 6) {
                fail(resp, 400, "新密码长度不能少于6位");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                fail(resp, 400, "两次输入的密码不一致");
                return;
            }

            authService.resetPassword(username.trim(), newPassword);
            success(resp, "密码重置成功，请返回登录", null);
        } catch (RuntimeException e) {
            fail(resp, 400, e.getMessage());
        } catch (Exception e) {
            fail(resp, 500, "服务器错误: " + e.getMessage());
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, String> loginData = parseBody(req, Map.class);
            String username = loginData.get("username");
            String password = loginData.get("password");
            String role = loginData.get("role");

            if (username == null || password == null) {
                fail(resp, 400, "用户名和密码不能为空");
                return;
            }

            String token = authService.login(username, password, role);
            User user = authService.findByUsername(username);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("token", token);
            Map<String, Object> userMap = new LinkedHashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("name", user.getName());
            userMap.put("role", user.getRole());
            userMap.put("avatar", user.getAvatar());
            data.put("user", userMap);
            data.put("expiresIn", 86400);

            success(resp, "登录成功", data);
        } catch (RuntimeException e) {
            fail(resp, 400, e.getMessage());
        } catch (Exception e) {
            fail(resp, 500, "服务器错误: " + e.getMessage());
        }
    }
}
