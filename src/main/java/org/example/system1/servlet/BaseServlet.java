package org.example.system1.servlet;

import com.google.gson.Gson;
import org.example.system1.util.JsonUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 基础 Servlet
 */
public abstract class BaseServlet extends HttpServlet {
    protected final Gson gson = new Gson();

    /**
     * 读取请求体 JSON
     */
    protected String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * 读取请求体并解析为指定类型
     */
    protected <T> T parseBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        String body = readBody(req);
        if (body == null || body.isEmpty()) return null;
        return gson.fromJson(body, clazz);
    }

    /**
     * 获取路径参数
     */
    protected Long getPathId(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                return Long.parseLong(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     */
    protected Long getUserId(HttpServletRequest req) {
        return (Long) req.getAttribute("userId");
    }

    /**
     * 获取当前登录用户角色
     */
    protected String getRole(HttpServletRequest req) {
        return (String) req.getAttribute("role");
    }

    /**
     * 检查是否为管理员
     */
    protected boolean isAdmin(HttpServletRequest req) {
        return "admin".equals(getRole(req));
    }

    /**
     * 返回成功
     */
    protected void success(HttpServletResponse resp, Object data) {
        JsonUtil.success(resp, data);
    }

    protected void success(HttpServletResponse resp, String message, Object data) {
        JsonUtil.success(resp, message, data);
    }

    /**
     * 返回失败
     */
    protected void fail(HttpServletResponse resp, int code, String message) {
        JsonUtil.fail(resp, code, message);
    }
}
