package org.example.system1.filter;

import org.example.system1.util.JwtUtil;
import org.example.system1.util.JsonUtil;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 认证过滤器
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        // 设置 CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");

        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        String servletPath = path.replace(contextPath, "");

        // 不需要认证的路径
        if (servletPath.equals("/api/auth/login")
                || servletPath.equals("/api/auth/reset-password")
                || servletPath.equals("/api/scores/export")
                || servletPath.equals("/api/teachers/template")
                || servletPath.equals("/api/teachers/import")
                || servletPath.startsWith("/html/")
                || servletPath.startsWith("/css/")
                || servletPath.startsWith("/js/")
                || servletPath.startsWith("/images/")
                || servletPath.equals("/")
                || servletPath.equals("/index.jsp")) {
            chain.doFilter(req, resp);
            return;
        }

        // API 接口需要认证
        if (servletPath.startsWith("/api/")) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                JsonUtil.unauthorized(response, "未登录，请先登录");
                return;
            }

            String token = authHeader.substring(7);
            if (!JwtUtil.validateToken(token)) {
                JsonUtil.unauthorized(response, "Token已过期，请重新登录");
                return;
            }

            // 将用户信息存入 request 属性
            request.setAttribute("userId", JwtUtil.getUserId(token));
            request.setAttribute("username", JwtUtil.getUsername(token));
            request.setAttribute("role", JwtUtil.getRole(token));
        }

        chain.doFilter(req, resp);
    }
}
