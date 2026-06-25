package org.example.system1.servlet;

import com.google.gson.reflect.TypeToken;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.system1.entity.Score;
import org.example.system1.service.ScoreService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@WebServlet("/api/scores/*")
public class ScoreServlet extends BaseServlet {
    private final ScoreService scoreService = new ScoreService();
    private static final String[] SUBJECTS = {"语文","数学","英语","物理","化学","生物"};
    private static final String[] COLS = {"chinese","math","english","physics","chemistry","biology"};

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        // GET /api/scores/class/{classId}
        if (pathInfo != null && pathInfo.startsWith("/class/")) {
            Long classId = Long.parseLong(pathInfo.substring(7));
            String semester = req.getParameter("semester");
            if (semester == null) semester = "2025-2026-1";
            Map<String, Object> result = scoreService.getClassScores(classId, semester);
            success(resp, result);
            return;
        }

        // GET /api/scores/export
        if ("/export".equals(pathInfo)) {
            exportScores(req, resp);
            return;
        }

        fail(resp, 404, "接口不存在");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/batch".equals(pathInfo)) {
            handleBatchSave(req, resp);
            return;
        }

        // POST /api/scores - 单条保存
        try {
            Score score = parseBody(req, Score.class);
            scoreService.saveScore(score);
            success(resp, "成绩保存成功", null);
        } catch (Exception e) {
            fail(resp, 400, "保存失败: " + e.getMessage());
        }
    }

    private void handleBatchSave(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Type listType = new TypeToken<List<Score>>(){}.getType();
            List<Score> scores = gson.fromJson(readBody(req), listType);
            Map<String, Object> result = scoreService.batchSave(scores);
            success(resp, "批量保存完成", result);
        } catch (Exception e) {
            fail(resp, 400, "批量保存失败: " + e.getMessage());
        }
    }

    /**
     * 导出成绩单
     */
    private void exportScores(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long classId = Long.parseLong(req.getParameter("classId"));
        String semester = req.getParameter("semester");
        if (semester == null) semester = "2025-2026-1";

        List<Map<String, Object>> students = scoreService.getSortedStudents(classId, semester, "avgScore", "desc");

        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("成绩单_" + classId + "_" + semester + ".xlsx", "UTF-8"));

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("成绩单");
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("序号");
            header.createCell(1).setCellValue("学号");
            header.createCell(2).setCellValue("姓名");
            for (int i = 0; i < SUBJECTS.length; i++) {
                header.createCell(3 + i).setCellValue(SUBJECTS[i]);
            }
            header.createCell(9).setCellValue("总分");
            header.createCell(10).setCellValue("平均分");
            header.createCell(11).setCellValue("排名");

            for (int i = 0; i < 12; i++) {
                header.getCell(i).setCellStyle(headerStyle);
            }

            for (int i = 0; i < students.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Map<String, Object> stu = students.get(i);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue((String) stu.get("studentNo"));
                row.createCell(2).setCellValue((String) stu.get("name"));
                for (int j = 0; j < SUBJECTS.length; j++) {
                    Object val = stu.get(SUBJECTS[j]);
                    if (val instanceof Number) {
                        row.createCell(3 + j).setCellValue(((Number) val).doubleValue());
                    }
                }
                row.createCell(9).setCellValue(toDouble(stu.get("totalScore")));
                row.createCell(10).setCellValue(toDouble(stu.get("avgScore")));
                row.createCell(11).setCellValue(i + 1);
            }

            try (OutputStream os = resp.getOutputStream()) {
                wb.write(os);
            }
        }
    }

    private double toDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return 0;
    }
}
