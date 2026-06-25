package org.example.system1.servlet;

import org.example.system1.entity.TeachingAssignment;
import org.example.system1.entity.User;
import org.example.system1.service.AuthService;
import org.example.system1.service.TeachingAssignmentService;
import org.example.system1.util.DBUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

@WebServlet("/api/teaching-assignments/*")
public class TeachingAssignmentServlet extends BaseServlet {
    private final TeachingAssignmentService assignmentService = new TeachingAssignmentService();
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/stats".equals(pathInfo)) {
            success(resp, assignmentService.getStats());
            return;
        }

        // /api/teaching-assignments/teacher/me - 当前登录教师的课表
        if ("/teacher/me".equals(pathInfo)) {
            Long userId = (Long) req.getAttribute("userId");
            if (userId == null) { fail(resp, 401, "未登录"); return; }
            User user = authService.getUserInfo(userId);
            if (user == null) { fail(resp, 404, "用户不存在"); return; }
            Long teacherId = findTeacherIdByName(user.getName());
            if (teacherId == null) {
                success(resp, java.util.Collections.emptyList());
                return;
            }
            success(resp, assignmentService.getByTeacherId(teacherId));
            return;
        }

        // /api/teaching-assignments/teacher/{id} - 查询指定教师的授课安排
        if (pathInfo != null && pathInfo.startsWith("/teacher/")) {
            String idStr = pathInfo.substring("/teacher/".length());
            try {
                Long teacherId = Long.parseLong(idStr);
                success(resp, assignmentService.getByTeacherId(teacherId));
            } catch (NumberFormatException e) {
                fail(resp, 400, "教师ID格式错误");
            }
            return;
        }

        success(resp, assignmentService.getList());
    }

    /**
     * 根据教师姓名查找 t_teacher.id
     */
    private Long findTeacherIdByName(String name) {
        if (name == null) return null;
        String sql = "SELECT id FROM t_teacher WHERE name = ? LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            TeachingAssignment ta = parseBody(req, TeachingAssignment.class);
            Map<String, Object> data = assignmentService.add(ta);
            success(resp, "安排成功", data);
        } catch (RuntimeException e) {
            fail(resp, 400, e.getMessage());
        } catch (Exception e) {
            fail(resp, 500, "操作失败: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = getPathId(req);
        if (id == null) {
            fail(resp, 400, "缺少授课安排ID");
            return;
        }
        try {
            assignmentService.delete(id);
            success(resp, "删除成功", null);
        } catch (Exception e) {
            fail(resp, 400, "删除失败: " + e.getMessage());
        }
    }
}
