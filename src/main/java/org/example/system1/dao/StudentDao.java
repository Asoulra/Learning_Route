package org.example.system1.dao;

import org.example.system1.entity.Student;
import org.example.system1.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {

    /**
     * 分页查询学生列表
     */
    public List<Student> findPage(String keyword, Long classId, String gender, int page, int pageSize) {
        StringBuilder sql = new StringBuilder(
            "SELECT s.*, c.class_name FROM t_student s " +
            "LEFT JOIN t_class c ON s.class_id = c.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (s.student_no LIKE ? OR s.name LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (classId != null && classId > 0) {
            sql.append(" AND s.class_id = ?");
            params.add(classId);
        }
        if (gender != null && !gender.isEmpty()) {
            sql.append(" AND s.gender = ?");
            params.add(gender);
        }
        sql.append(" ORDER BY s.student_no LIMIT ?, ?");
        params.add((page - 1) * pageSize);
        params.add(pageSize);

        List<Student> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rowToStudent(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 统计总数
     */
    public long count(String keyword, Long classId, String gender) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM t_student s WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (s.student_no LIKE ? OR s.name LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (classId != null && classId > 0) {
            sql.append(" AND s.class_id = ?");
            params.add(classId);
        }
        if (gender != null && !gender.isEmpty()) {
            sql.append(" AND s.gender = ?");
            params.add(gender);
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 根据ID查找
     */
    public Student findById(Long id) {
        String sql = "SELECT s.*, c.class_name FROM t_student s LEFT JOIN t_class c ON s.class_id = c.id WHERE s.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rowToStudent(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据班级ID查找学生列表
     */
    public List<Student> findByClassId(Long classId) {
        String sql = "SELECT s.*, c.class_name FROM t_student s LEFT JOIN t_class c ON s.class_id = c.id WHERE s.class_id = ? ORDER BY s.student_no";
        List<Student> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rowToStudent(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 添加学生
     */
    public int insert(Student student) {
        String sql = "INSERT INTO t_student (student_no, name, gender, class_id, enroll_date, phone, address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getStudentNo());
            ps.setString(2, student.getName());
            ps.setString(3, student.getGender());
            ps.setObject(4, student.getClassId());
            ps.setString(5, student.getEnrollDate());
            ps.setString(6, student.getPhone());
            ps.setString(7, student.getAddress());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 更新学生
     */
    public int update(Student student) {
        String sql = "UPDATE t_student SET student_no=?, name=?, gender=?, class_id=?, enroll_date=?, phone=?, address=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getStudentNo());
            ps.setString(2, student.getName());
            ps.setString(3, student.getGender());
            ps.setObject(4, student.getClassId());
            ps.setString(5, student.getEnrollDate());
            ps.setString(6, student.getPhone());
            ps.setString(7, student.getAddress());
            ps.setLong(8, student.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 删除学生
     */
    public int delete(Long id) {
        String sql = "DELETE FROM t_student WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取统计信息
     */
    public long[] getStats() {
        long[] stats = new long[4]; // total, male, female, classCount
        try (Connection conn = DBUtil.getConnection()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total, SUM(CASE WHEN gender='男' THEN 1 ELSE 0 END) as male, SUM(CASE WHEN gender='女' THEN 1 ELSE 0 END) as female FROM t_student")) {
                if (rs.next()) {
                    stats[0] = rs.getLong("total");
                    stats[1] = rs.getLong("male");
                    stats[2] = rs.getLong("female");
                }
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT class_id) FROM t_student WHERE class_id IS NOT NULL")) {
                if (rs.next()) {
                    stats[3] = rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    private Student rowToStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getLong("id"));
        s.setStudentNo(rs.getString("student_no"));
        s.setName(rs.getString("name"));
        s.setGender(rs.getString("gender"));
        s.setClassId(rs.getLong("class_id"));
        s.setEnrollDate(rs.getString("enroll_date"));
        s.setPhone(rs.getString("phone"));
        s.setAddress(rs.getString("address"));
        try { s.setClassName(rs.getString("class_name")); } catch (SQLException ignored) {}
        return s;
    }
}
