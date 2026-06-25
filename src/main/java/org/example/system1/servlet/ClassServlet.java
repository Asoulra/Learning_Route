package org.example.system1.servlet;

import org.example.system1.entity.ClassInfo;
import org.example.system1.service.ClassService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/classes/*")
public class ClassServlet extends BaseServlet {
    private final ClassService classService = new ClassService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/stats".equals(pathInfo)) {
            success(resp, classService.getStats());
            return;
        }

        if ("/list".equals(pathInfo)) {
            // 简单列表（供下拉选择）
            List<ClassInfo> list = classService.getList(null, null);
            success(resp, list);
            return;
        }

        Long id = getPathId(req);
        if (id != null) {
            ClassInfo c = classService.getById(id);
            if (c != null) {
                success(resp, c);
            } else {
                fail(resp, 404, "班级不存在");
            }
            return;
        }

        String keyword = req.getParameter("keyword");
        String grade = req.getParameter("grade");
        success(resp, classService.getList(keyword, grade));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ClassInfo cls = parseBody(req, ClassInfo.class);
            Map<String, Object> data = classService.add(cls);
            success(resp, "添加成功", data);
        } catch (Exception e) {
            fail(resp, 400, "添加失败: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = getPathId(req);
        if (id == null) {
            fail(resp, 400, "缺少班级ID");
            return;
        }
        try {
            ClassInfo cls = parseBody(req, ClassInfo.class);
            cls.setId(id);
            classService.update(cls);
            success(resp, "更新成功", null);
        } catch (Exception e) {
            fail(resp, 400, "更新失败: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = getPathId(req);
        if (id == null) {
            fail(resp, 400, "缺少班级ID");
            return;
        }
        try {
            classService.delete(id);
            success(resp, "删除成功", null);
        } catch (Exception e) {
            fail(resp, 400, "删除失败: " + e.getMessage());
        }
    }
}
