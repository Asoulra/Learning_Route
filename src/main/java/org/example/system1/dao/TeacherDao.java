package org.example.system1.dao;

import org.example.system1.entity.Teacher;
import org.example.system1.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDao {

    public List<Teacher> findAll(String keyword, String subject) {
        StringBuilder sql = new StringBuilder("SELECT * FROM t_teacher WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (teacher_no LIKE ? OR name LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (subject != null && !subject.isEmpty()) {
            sql.append(" AND subject = ?");
            params.add(subject);
        }
        sql.append(" ORDER BY id");

        List<Teacher> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rowToTeacher(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Teacher findById(Long id) {
        String sql = "SELECT * FROM t_teacher WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rowToTeacher(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Teacher findByTeacherNo(String teacherNo) {
        String sql = "SELECT * FROM t_teacher WHERE teacher_no = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, teacherNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rowToTeacher(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int insert(Teacher teacher) {
        String sql = "INSERT INTO t_teacher (teacher_no, name, gender, title, subject, phone, email) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, teacher.getTeacherNo());
            ps.setString(2, teacher.getName());
            ps.setString(3, teacher.getGender());
            ps.setString(4, teacher.getTitle());
            ps.setString(5, teacher.getSubject());
            ps.setString(6, teacher.getPhone());
            ps.setString(7, teacher.getEmail());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int update(Teacher teacher) {
        String sql = "UPDATE t_teacher SET teacher_no=?, name=?, gender=?, title=?, subject=?, phone=?, email=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, teacher.getTeacherNo());
            ps.setString(2, teacher.getName());
            ps.setString(3, teacher.getGender());
            ps.setString(4, teacher.getTitle());
            ps.setString(5, teacher.getSubject());
            ps.setString(6, teacher.getPhone());
            ps.setString(7, teacher.getEmail());
            ps.setLong(8, teacher.getId());
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int delete(Long id) {
        String sql = "DELETE FROM t_teacher WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int[] getStats() {
        int[] stats = new int[2];
        try (Connection conn = DBUtil.getConnection()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM t_teacher")) {
                if (rs.next()) stats[0] = rs.getInt(1);
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT subject) FROM t_teacher")) {
                if (rs.next()) stats[1] = rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }

    /**
     * 批量插入（导入用）
     */
    public int batchInsert(List<Teacher> teachers) {
        String sql = "INSERT INTO t_teacher (teacher_no, name, gender, title, subject, phone, email) VALUES (?,?,?,?,?,?,?)";
        int count = 0;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Teacher t : teachers) {
                ps.setString(1, t.getTeacherNo());
                ps.setString(2, t.getName());
                ps.setString(3, t.getGender());
                ps.setString(4, t.getTitle());
                ps.setString(5, t.getSubject());
                ps.setString(6, t.getPhone());
                ps.setString(7, t.getEmail());
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    private Teacher rowToTeacher(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setId(rs.getLong("id"));
        t.setTeacherNo(rs.getString("teacher_no"));
        t.setName(rs.getString("name"));
        t.setGender(rs.getString("gender"));
        t.setTitle(rs.getString("title"));
        t.setSubject(rs.getString("subject"));
        t.setPhone(rs.getString("phone"));
        t.setEmail(rs.getString("email"));
        t.setStatus(rs.getInt("status"));
        return t;
    }
}
