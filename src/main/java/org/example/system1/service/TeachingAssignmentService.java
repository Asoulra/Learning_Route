package org.example.system1.service;

import org.example.system1.dao.TeachingAssignmentDao;
import org.example.system1.entity.TeachingAssignment;
import java.util.*;

public class TeachingAssignmentService {
    private final TeachingAssignmentDao assignmentDao = new TeachingAssignmentDao();

    public List<TeachingAssignment> getList() {
        return assignmentDao.findAll();
    }

    public List<TeachingAssignment> getByTeacherId(Long teacherId) {
        return assignmentDao.findByTeacherId(teacherId);
    }

    public Map<String, Object> add(TeachingAssignment ta) {
        // 校验是否已存在相同安排
        if (assignmentDao.exists(ta.getClassId(), ta.getSubject(), ta.getSemester())) {
            throw new RuntimeException("该班级在当前学期已有该科目的授课教师");
        }
        int id = assignmentDao.insert(ta);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return data;
    }

    public void delete(Long id) {
        assignmentDao.delete(id);
    }

    public Map<String, Object> getStats() {
        int[] stats = assignmentDao.getStats();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("classCount", stats[0]);
        data.put("totalCount", stats[1]);
        data.put("avgLoad", stats[2]);
        return data;
    }
}
