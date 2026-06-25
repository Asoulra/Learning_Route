package org.example.system1.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 字符编码过滤器
 */
@WebFilter("/*")
public class CharsetFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        // 设置请求和响应的字符编码
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 只对 API 请求设置 JSON Content-Type
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        String servletPath = path.replace(contextPath, "");

        if (servletPath.startsWith("/api/")) {
            response.setContentType("application/json;charset=UTF-8");
        }
        // 其他请求（HTML、CSS、JS等）让容器自动设置 Content-Type

        chain.doFilter(req, resp);
    }
}
