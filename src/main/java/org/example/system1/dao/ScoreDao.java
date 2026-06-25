package org.example.system1.dao;

import org.example.system1.entity.Score;
import org.example.system1.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreDao {

    private static final String[] SUBJECTS = {"语文","数学","英语","物理","化学","生物"};
    private static final String[] COLS = {"chinese","math","english","physics","chemistry","biology"};

    /**
     * 根据班级和学期查询成绩（含学生信息）
     */
    public List<Score> findByClassAndSemester(Long classId, String semester) {
        String sql = "SELECT s.*, st.student_no, st.name AS student_name, st.gender " +
                     "FROM t_score s JOIN t_student st ON s.student_id = st.id " +
                     "WHERE s.class_id = ? AND s.semester = ? ORDER BY st.student_no";
        List<Score> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classId);
            ps.setString(2, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rowToScore(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * 保存或更新单个学生成绩（UPSERT）
     */
    public void upsert(Score score) {
        String sql = "INSERT INTO t_score (student_id, class_id, semester, chinese, math, english, physics, chemistry, biology) " +
                     "VALUES (?,?,?,?,?,?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE chinese=VALUES(chinese), math=VALUES(math), english=VALUES(english), " +
                     "physics=VALUES(physics), chemistry=VALUES(chemistry), biology=VALUES(biology)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, score.getStudentId());
            ps.setLong(2, score.getClassId());
            ps.setString(3, score.getSemester());
            setScoreParam(ps, 4, score.getChinese());
            setScoreParam(ps, 5, score.getMath());
            setScoreParam(ps, 6, score.getEnglish());
            setScoreParam(ps, 7, score.getPhysics());
            setScoreParam(ps, 8, score.getChemistry());
            setScoreParam(ps, 9, score.getBiology());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * 批量保存成绩
     */
    public void batchUpsert(List<Score> scores) {
        String sql = "INSERT INTO t_score (student_id, class_id, semester, chinese, math, english, physics, chemistry, biology) " +
                     "VALUES (?,?,?,?,?,?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE chinese=VALUES(chinese), math=VALUES(math), english=VALUES(english), " +
                     "physics=VALUES(physics), chemistry=VALUES(chemistry), biology=VALUES(biology)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Score score : scores) {
                ps.setLong(1, score.getStudentId());
                ps.setLong(2, score.getClassId());
                ps.setString(3, score.getSemester());
                setScoreParam(ps, 4, score.getChinese());
                setScoreParam(ps, 5, score.getMath());
                setScoreParam(ps, 6, score.getEnglish());
                setScoreParam(ps, 7, score.getPhysics());
                setScoreParam(ps, 8, score.getChemistry());
                setScoreParam(ps, 9, score.getBiology());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void setScoreParam(PreparedStatement ps, int index, Double val) throws SQLException {
        if (val == null) ps.setNull(index, Types.DECIMAL);
        else ps.setDouble(index, val);
    }

    private Score rowToScore(ResultSet rs) throws SQLException {
        Score s = new Score();
        s.setId(rs.getLong("id"));
        s.setStudentId(rs.getLong("student_id"));
        s.setClassId(rs.getLong("class_id"));
        s.setSemester(rs.getString("semester"));
        s.setChinese(getDouble(rs, "chinese"));
        s.setMath(getDouble(rs, "math"));
        s.setEnglish(getDouble(rs, "english"));
        s.setPhysics(getDouble(rs, "physics"));
        s.setChemistry(getDouble(rs, "chemistry"));
        s.setBiology(getDouble(rs, "biology"));
        return s;
    }

    private Double getDouble(ResultSet rs, String col) throws SQLException {
        double val = rs.getDouble(col);
        return rs.wasNull() ? null : val;
    }
}
