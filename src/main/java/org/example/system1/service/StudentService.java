package org.example.system1.service;

import org.example.system1.dao.StudentDao;
import org.example.system1.entity.Student;
import java.util.*;

public class StudentService {
    private final StudentDao studentDao = new StudentDao();

    public Map<String, Object> getPage(String keyword, Long classId, String gender, int page, int pageSize) {
        List<Student> list = studentDao.findPage(keyword, classId, gender, page, pageSize);
        long total = studentDao.count(keyword, classId, gender);
        int totalPages = (int) Math.ceil((double) total / pageSize);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("totalCount", total);
        result.put("totalPages", totalPages);
        result.put("currentPage", page);
        return result;
    }

    public Student getById(Long id) {
        return studentDao.findById(id);
    }

    public Map<String, Object> add(Student student) {
        int id = studentDao.insert(student);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return data;
    }

    public void update(Student student) {
        studentDao.update(student);
    }

    public void delete(Long id) {
        studentDao.delete(id);
    }

    public Map<String, Object> getStats() {
        long[] stats = studentDao.getStats();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", stats[0]);
        data.put("maleCount", stats[1]);
        data.put("femaleCount", stats[2]);
        data.put("classCount", stats[3]);
        return data;
    }
}
