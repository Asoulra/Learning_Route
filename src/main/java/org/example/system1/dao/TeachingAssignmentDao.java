package org.example.system1.dao;

import org.example.system1.entity.TeachingAssignment;
import org.example.system1.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeachingAssignmentDao {

    public List<TeachingAssignment> findAll() {
        String sql = "SELECT ta.*, c.class_name, t.name AS teacher_name, t.title " +
                     "FROM t_teaching_assignment ta " +
                     "JOIN t_class c ON ta.class_id = c.id " +
                     "JOIN t_teacher t ON ta.teacher_id = t.id " +
                     "ORDER BY c.class_name, ta.subject";
        List<TeachingAssignment> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rowToAssignment(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * 查询某教师的全部授课安排
     */
    public List<TeachingAssignment> findByTeacherId(Long teacherId) {
        String sql = "SELECT ta.*, c.class_name, t.name AS teacher_name, t.title " +
                     "FROM t_teaching_assignment ta " +
                     "JOIN t_class c ON ta.class_id = c.id " +
                     "JOIN t_teacher t ON ta.teacher_id = t.id " +
                     "WHERE ta.teacher_id = ? " +
                     "ORDER BY c.class_name, ta.subject";
        List<TeachingAssignment> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rowToAssignment(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public TeachingAssignment findById(Long id) {
        String sql = "SELECT ta.*, c.class_name, t.name AS teacher_name, t.title " +
                     "FROM t_teaching_assignment ta " +
                     "JOIN t_class c ON ta.class_id = c.id " +
                     "JOIN t_teacher t ON ta.teacher_id = t.id " +
                     "WHERE ta.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rowToAssignment(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * 检查是否已存在相同的授课安排
     */
    public boolean exists(Long classId, String subject, String semester) {
        String sql = "SELECT COUNT(*) FROM t_teaching_assignment WHERE class_id=? AND subject=? AND semester=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classId);
            ps.setString(2, subject);
            ps.setString(3, semester);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int insert(TeachingAssignment ta) {
        String sql = "INSERT INTO t_teaching_assignment (class_id, teacher_id, subject, semester, remark) VALUES (?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, ta.getClassId());
            ps.setLong(2, ta.getTeacherId());
            ps.setString(3, ta.getSubject());
            ps.setString(4, ta.getSemester());
            ps.setString(5, ta.getRemark());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int delete(Long id) {
        String sql = "DELETE FROM t_teaching_assignment WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int[] getStats() {
        int[] stats = new int[3];
        try (Connection conn = DBUtil.getConnection()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT class_id) FROM t_teaching_assignment")) {
                if (rs.next()) stats[0] = rs.getInt(1);
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM t_teaching_assignment")) {
                if (rs.next()) stats[1] = rs.getInt(1);
            }
            if (stats[1] > 0) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT teacher_id) FROM t_teaching_assignment")) {
                    if (rs.next()) {
                        int teacherCount = rs.getInt(1);
                        if (teacherCount > 0) stats[2] = Math.round((float)stats[1] / teacherCount);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }

    private TeachingAssignment rowToAssignment(ResultSet rs) throws SQLException {
        TeachingAssignment ta = new TeachingAssignment();
        ta.setId(rs.getLong("id"));
        ta.setClassId(rs.getLong("class_id"));
        ta.setTeacherId(rs.getLong("teacher_id"));
        ta.setSubject(rs.getString("subject"));
        ta.setSemester(rs.getString("semester"));
        ta.setRemark(rs.getString("remark"));
        try { ta.setClassName(rs.getString("class_name")); } catch (SQLException ignored) {}
        try { ta.setTeacherName(rs.getString("teacher_name")); } catch (SQLException ignored) {}
        try { ta.setTitle(rs.getString("title")); } catch (SQLException ignored) {}
        return ta;
    }
}
