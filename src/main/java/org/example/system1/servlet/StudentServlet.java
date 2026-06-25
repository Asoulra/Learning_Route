package org.example.system1.servlet;

import org.example.system1.entity.Student;
import org.example.system1.service.StudentService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/students/*")
public class StudentServlet extends BaseServlet {
    private final StudentService studentService = new StudentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/stats".equals(pathInfo)) {
            success(resp, studentService.getStats());
            return;
        }

        if ("/list".equals(pathInfo)) {
            // 简单列表（供下拉选择）
            success(resp, studentService.getPage(null, null, null, 1, 9999).get("list"));
            return;
        }

        Long id = getPathId(req);
        if (id != null) {
            Student s = studentService.getById(id);
            if (s != null) {
                success(resp, s);
            } else {
                fail(resp, 404, "学生不存在");
            }
            return;
        }

        // 分页查询
        int page = parseInt(req.getParameter("page"), 1);
        int pageSize = parseInt(req.getParameter("pageSize"), 10);
        String keyword = req.getParameter("keyword");
        Long classId = parseLong(req.getParameter("classId"));
        String gender = req.getParameter("gender");

        Map<String, Object> result = studentService.getPage(keyword, classId, gender, page, pageSize);
        success(resp, result);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Student student = parseBody(req, Student.class);
            Map<String, Object> data = studentService.add(student);
            success(resp, "添加成功", data);
        } catch (Exception e) {
            fail(resp, 400, "添加失败: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = getPathId(req);
        if (id == null) {
            fail(resp, 400, "缺少学生ID");
            return;
        }
        try {
            Student student = parseBody(req, Student.class);
            student.setId(id);
            studentService.update(student);
            success(resp, "更新成功", null);
        } catch (Exception e) {
            fail(resp, 400, "更新失败: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = getPathId(req);
        if (id == null) {
            fail(resp, 400, "缺少学生ID");
            return;
        }
        try {
            studentService.delete(id);
            success(resp, "删除成功", null);
        } catch (Exception e) {
            fail(resp, 400, "删除失败: " + e.getMessage());
        }
    }

    private int parseInt(String val, int def) {
        try { return val != null ? Integer.parseInt(val) : def; } catch (NumberFormatException e) { return def; }
    }

    private Long parseLong(String val) {
        try { return val != null && !val.isEmpty() ? Long.parseLong(val) : null; } catch (NumberFormatException e) { return null; }
    }
}
