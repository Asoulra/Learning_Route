package org.example.system1.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON 工具类 - 统一响应
 */
public class JsonUtil {
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    /**
     * 成功响应
     */
    public static void success(HttpServletResponse resp, Object data) {
        write(resp, 200, "操作成功", data);
    }

    public static void success(HttpServletResponse resp, String message, Object data) {
        write(resp, 200, message, data);
    }

    /**
     * 失败响应
     */
    public static void fail(HttpServletResponse resp, int code, String message) {
        write(resp, code, message, null);
    }

    /**
     * 未授权响应
     */
    public static void unauthorized(HttpServletResponse resp, String message) {
        write(resp, 401, message, null);
    }

    /**
     * 写入 JSON 响应
     */
    public static void write(HttpServletResponse resp, int code, String message, Object data) {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(code == 200 ? HttpServletResponse.SC_OK : code);

        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", data);

        try {
            PrintWriter out = resp.getWriter();
            out.write(gson.toJson(result));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
}
