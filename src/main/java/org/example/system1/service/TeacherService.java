package org.example.system1.service;

import org.example.system1.dao.TeacherDao;
import org.example.system1.entity.Teacher;
import java.util.*;

public class TeacherService {
    private final TeacherDao teacherDao = new TeacherDao();

    public List<Teacher> getList(String keyword, String subject) {
        return teacherDao.findAll(keyword, subject);
    }

    public Teacher getById(Long id) {
        return teacherDao.findById(id);
    }

    public Map<String, Object> add(Teacher teacher) {
        int id = teacherDao.insert(teacher);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return data;
    }

    public void update(Teacher teacher) {
        teacherDao.update(teacher);
    }

    public void delete(Long id) {
        teacherDao.delete(id);
    }

    public Map<String, Object> getStats() {
        int[] stats = teacherDao.getStats();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", stats[0]);
        data.put("subjectCount", stats[1]);
        return data;
    }

    /**
     * 批量导入教师
     */
    public Map<String, Object> importTeachers(List<Teacher> teachers) {
        int successCount = 0;
        int failCount = 0;
        List<Map<String, Object>> errors = new ArrayList<>();

        for (int i = 0; i < teachers.size(); i++) {
            Teacher t = teachers.get(i);
            try {
                // 检查工号重复
                Teacher existing = teacherDao.findByTeacherNo(t.getTeacherNo());
                if (existing != null) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("row", i + 2); // +2 因为第1行是表头
                    err.put("reason", "工号重复: " + t.getTeacherNo());
                    errors.add(err);
                    failCount++;
                    continue;
                }
                // 验证必填字段
                if (t.getTeacherNo() == null || t.getTeacherNo().isEmpty()) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("row", i + 2);
                    err.put("reason", "工号不能为空");
                    errors.add(err);
                    failCount++;
                    continue;
                }
                if (t.getName() == null || t.getName().isEmpty()) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("row", i + 2);
                    err.put("reason", "姓名不能为空");
                    errors.add(err);
                    failCount++;
                    continue;
                }
                teacherDao.insert(t);
                successCount++;
            } catch (Exception e) {
                Map<String, Object> err = new HashMap<>();
                err.put("row", i + 2);
                err.put("reason", e.getMessage());
                errors.add(err);
                failCount++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        return result;
    }
}
