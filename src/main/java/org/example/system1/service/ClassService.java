package org.example.system1.service;

import org.example.system1.dao.ClassInfoDao;
import org.example.system1.entity.ClassInfo;
import java.util.*;

public class ClassService {
    private final ClassInfoDao classDao = new ClassInfoDao();

    public List<ClassInfo> getList(String keyword, String grade) {
        return classDao.findAll(keyword, grade);
    }

    public ClassInfo getById(Long id) {
        return classDao.findById(id);
    }

    public Map<String, Object> add(ClassInfo cls) {
        int id = classDao.insert(cls);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return data;
    }

    public void update(ClassInfo cls) {
        classDao.update(cls);
    }

    public void delete(Long id) {
        classDao.delete(id);
    }

    public Map<String, Object> getStats() {
        double[] stats = classDao.getStats();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", (long)stats[0]);
        data.put("totalStudents", (long)stats[1]);
        data.put("avgSize", stats[2]);
        return data;
    }
}
