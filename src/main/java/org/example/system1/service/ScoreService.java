package org.example.system1.service;

import org.example.system1.dao.ScoreDao;
import org.example.system1.dao.StudentDao;
import org.example.system1.entity.Score;
import org.example.system1.entity.Student;
import java.util.*;

public class ScoreService {
    private final ScoreDao scoreDao = new ScoreDao();
    private final StudentDao studentDao = new StudentDao();
    private static final String[] SUBJECTS = {"语文","数学","英语","物理","化学","生物"};
    private static final String[] COLS = {"chinese","math","english","physics","chemistry","biology"};

    /**
     * 获取某班级某学期成绩（含学生信息）
     */
    public Map<String, Object> getClassScores(Long classId, String semester) {
        List<Student> students = studentDao.findByClassId(classId);
        List<Score> scores = scoreDao.findByClassAndSemester(classId, semester);

        // 建立成绩映射
        Map<Long, Score> scoreMap = new HashMap<>();
        for (Score s : scores) {
            scoreMap.put(s.getStudentId(), s);
        }

        // 组装学生+成绩
        List<Map<String, Object>> studentList = new ArrayList<>();
        double totalSum = 0;
        double maxAvg = 0;
        double minAvg = 100;
        int failCount = 0;
        int count = 0;

        for (Student stu : students) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", stu.getId());
            item.put("studentNo", stu.getStudentNo());
            item.put("name", stu.getName());
            item.put("gender", stu.getGender());

            Map<String, Object> scoreMap2 = new LinkedHashMap<>();
            Score sc = scoreMap.get(stu.getId());

            double stuTotal = 0;
            int subCount = 0;
            for (int i = 0; i < SUBJECTS.length; i++) {
                Double val = null;
                if (sc != null) {
                    val = getSubjectScore(sc, COLS[i]);
                }
                scoreMap2.put(SUBJECTS[i], val);
                if (val != null) {
                    stuTotal += val;
                    subCount++;
                }
            }
            item.put("scores", scoreMap2);

            if (subCount > 0) {
                double avg = stuTotal / subCount;
                totalSum += avg;
                if (avg > maxAvg) maxAvg = avg;
                if (avg < minAvg) minAvg = avg;
                if (avg < 60) failCount++;
                count++;
            }

            studentList.add(item);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("count", students.size());
        stats.put("avg", count > 0 ? Math.round(totalSum / count * 10.0) / 10.0 : 0);
        stats.put("max", maxAvg);
        stats.put("min", minAvg == 100 ? 0 : minAvg);
        stats.put("failCount", failCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("students", studentList);
        result.put("stats", stats);
        return result;
    }

    /**
     * 保存单个学生成绩
     */
    public void saveScore(Score score) {
        scoreDao.upsert(score);
    }

    /**
     * 批量保存成绩
     */
    public Map<String, Object> batchSave(List<Score> scores) {
        scoreDao.batchUpsert(scores);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("successCount", scores.size());
        result.put("failCount", 0);
        return result;
    }

    /**
     * 获取统计（用于图表和排名）
     */
    public Map<String, Object> getStatistics(Long classId, String semester, String sortField, String sortOrder) {
        List<Student> students = studentDao.findByClassId(classId);
        List<Score> scores = scoreDao.findByClassAndSemester(classId, semester);

        Map<Long, Score> scoreMap = new HashMap<>();
        for (Score s : scores) {
            scoreMap.put(s.getStudentId(), s);
        }

        // 构建学生成绩列表
        List<Map<String, Object>> studentList = new ArrayList<>();
        double classTotalAvg = 0;
        double highest = 0;
        double lowest = 999;
        int failCount = 0;
        int validCount = 0;
        int excellentCount = 0;
        int goodCount = 0;
        int passCount = 0;

        // 各科目累加
        double[] subSums = new double[SUBJECTS.length];
        int[] subCounts = new int[SUBJECTS.length];

        for (Student stu : students) {
            Score sc = scoreMap.get(stu.getId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", stu.getId());
            item.put("studentNo", stu.getStudentNo());
            item.put("name", stu.getName());
            item.put("gender", stu.getGender());

            double stuTotal = 0;
            int subCnt = 0;
            for (int i = 0; i < SUBJECTS.length; i++) {
                Double val = sc != null ? getSubjectScore(sc, COLS[i]) : null;
                item.put(SUBJECTS[i], val);
                if (val != null) {
                    stuTotal += val;
                    subCnt++;
                    subSums[i] += val;
                    subCounts[i]++;
                }
            }

            double avgScore = subCnt > 0 ? Math.round(stuTotal / subCnt * 100.0) / 100.0 : 0;
            item.put("totalScore", Math.round(stuTotal * 100.0) / 100.0);
            item.put("avgScore", avgScore);

            if (subCnt > 0) {
                classTotalAvg += avgScore;
                validCount++;
                if (stuTotal > highest) highest = stuTotal;
                if (stuTotal < lowest) lowest = stuTotal;
                if (avgScore < 60) failCount++;
                if (avgScore >= 90) excellentCount++;
                else if (avgScore >= 75) goodCount++;
                else if (avgScore >= 60) passCount++;
            }

            studentList.add(item);
        }

        // 排序
        studentList.sort((a, b) -> {
            double va = getSortValue(a, sortField);
            double vb = getSortValue(b, sortField);
            return "asc".equals(sortOrder) ? Double.compare(va, vb) : Double.compare(vb, va);
        });

        double classAvg = validCount > 0 ? Math.round(classTotalAvg / validCount * 10.0) / 10.0 : 0;

        // 概述
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalCount", students.size());
        overview.put("classAvg", classAvg);
        overview.put("highestScore", highest);
        overview.put("lowestScore", lowest == 999 ? 0 : lowest);
        overview.put("failCount", failCount);
        overview.put("passRate", students.size() > 0 ? Math.round((validCount - failCount) * 1000.0 / students.size()) / 10.0 : 0);
        overview.put("excellentRate", students.size() > 0 ? Math.round(excellentCount * 1000.0 / students.size()) / 10.0 : 0);

        // 分布
        Map<String, Object> distribution = new LinkedHashMap<>();
        distribution.put("excellent", excellentCount);
        distribution.put("good", goodCount);
        distribution.put("pass", passCount);
        distribution.put("fail", failCount);
        distribution.put("totalCount", students.size());

        // 各科平均
        List<Map<String, Object>> subjectAvg = new ArrayList<>();
        for (int i = 0; i < SUBJECTS.length; i++) {
            Map<String, Object> sa = new LinkedHashMap<>();
            sa.put("subject", SUBJECTS[i]);
            sa.put("avg", subCounts[i] > 0 ? Math.round(subSums[i] / subCounts[i] * 10.0) / 10.0 : 0);
            subjectAvg.add(sa);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overview", overview);
        result.put("distribution", distribution);
        result.put("subjectAvg", subjectAvg);
        result.put("students", studentList);
        return result;
    }

    /**
     * 获取排序后的成绩列表（用于导出）
     */
    public List<Map<String, Object>> getSortedStudents(Long classId, String semester, String sortField, String sortOrder) {
        Map<String, Object> stats = getStatistics(classId, semester, sortField, sortOrder);
        return (List<Map<String, Object>>) stats.get("students");
    }

    private double getSortValue(Map<String, Object> item, String field) {
        if (field == null) field = "avgScore";
        Object val = item.get(field);
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0; }
    }

    private Double getSubjectScore(Score sc, String col) {
        switch (col) {
            case "chinese": return sc.getChinese();
            case "math": return sc.getMath();
            case "english": return sc.getEnglish();
            case "physics": return sc.getPhysics();
            case "chemistry": return sc.getChemistry();
            case "biology": return sc.getBiology();
            default: return null;
        }
    }
}
