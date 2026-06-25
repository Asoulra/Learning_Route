package org.example.system1.dao;

import org.example.system1.entity.ClassInfo;
import org.example.system1.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassInfoDao {

    /**
     * 查询班级列表
     */
    public List<ClassInfo> findAll(String keyword, String grade) {
        StringBuilder sql = new StringBuilder(
            "SELECT c.*, (SELECT COUNT(*) FROM t_student s WHERE s.class_id = c.id) AS student_count " +
            "FROM t_class c WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (c.class_name LIKE ? OR c.grade LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (grade != null && !grade.isEmpty()) {
            sql.append(" AND c.grade = ?");
            params.add(grade);
        }
        sql.append(" ORDER BY c.id");

        List<ClassInfo> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rowToClass(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据ID查找
     */
    public ClassInfo findById(Long id) {
        String sql = "SELECT c.*, (SELECT COUNT(*) FROM t_student s WHERE s.class_id = c.id) AS student_count FROM t_class c WHERE c.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rowToClass(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加班级
     */
    public int insert(ClassInfo cls) {
        String sql = "INSERT INTO t_class (class_name, grade, head_teacher, room_location, remark) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cls.getClassName());
            ps.setString(2, cls.getGrade());
            ps.setString(3, cls.getHeadTeacher());
            ps.setString(4, cls.getRoomLocation());
            ps.setString(5, cls.getRemark());
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
     * 更新班级
     */
    public int update(ClassInfo cls) {
        String sql = "UPDATE t_class SET class_name=?, grade=?, head_teacher=?, room_location=?, remark=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cls.getClassName());
            ps.setString(2, cls.getGrade());
            ps.setString(3, cls.getHeadTeacher());
            ps.setString(4, cls.getRoomLocation());
            ps.setString(5, cls.getRemark());
            ps.setLong(6, cls.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 删除班级
     */
    public int delete(Long id) {
        String sql = "DELETE FROM t_class WHERE id = ?";
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
     * 获取统计
     */
    public double[] getStats() {
        double[] stats = new double[3];
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            // total
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM t_class")) {
                if (rs.next()) stats[0] = rs.getLong(1);
            }
            // totalStudents
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM t_student")) {
                if (rs.next()) stats[1] = rs.getLong(1);
            }
            // avgSize
            if (stats[0] > 0) {
                stats[2] = Math.round(stats[1] / stats[0] * 10.0) / 10.0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    private ClassInfo rowToClass(ResultSet rs) throws SQLException {
        ClassInfo c = new ClassInfo();
        c.setId(rs.getLong("id"));
        c.setClassName(rs.getString("class_name"));
        c.setGrade(rs.getString("grade"));
        c.setHeadTeacher(rs.getString("head_teacher"));
        c.setRoomLocation(rs.getString("room_location"));
        c.setRemark(rs.getString("remark"));
        c.setCreatedAt(rs.getString("created_at"));
        try { c.setStudentCount(rs.getInt("student_count")); } catch (SQLException ignored) {}
        return c;
    }
}
