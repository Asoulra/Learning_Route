package org.example.system1.servlet;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.system1.service.ScoreService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@WebServlet("/api/statistics/*")
public class StatisticsServlet extends BaseServlet {
    private final ScoreService scoreService = new ScoreService();
    private static final String[] SUBJECTS = {"语文","数学","英语","物理","化学","生物"};

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        // GET /api/statistics/class/{classId}
        if (pathInfo != null && pathInfo.startsWith("/class/")) {
            Long classId = Long.parseLong(pathInfo.substring(7));
            String semester = req.getParameter("semester");
            if (semester == null) semester = "2025-2026-1";
            String sortField = req.getParameter("sortField");
            if (sortField == null) sortField = "avgScore";
            String sortOrder = req.getParameter("sortOrder");
            if (sortOrder == null) sortOrder = "desc";

            Map<String, Object> result = scoreService.getStatistics(classId, semester, sortField, sortOrder);
            success(resp, result);
            return;
        }

        // GET /api/statistics/export/report
        if ("/export/report".equals(pathInfo)) {
            exportReport(req, resp);
            return;
        }

        // GET /api/statistics/export/excel
        if ("/export/excel".equals(pathInfo)) {
            exportExcel(req, resp);
            return;
        }

        fail(resp, 404, "接口不存在");
    }

    /**
     * 导出统计报告
     */
    private void exportReport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long classId = Long.parseLong(req.getParameter("classId"));
        String semester = req.getParameter("semester");
        if (semester == null) semester = "2025-2026-1";

        Map<String, Object> stats = scoreService.getStatistics(classId, semester, "avgScore", "desc");
        Map<String, Object> overview = (Map<String, Object>) stats.get("overview");
        Map<String, Object> distribution = (Map<String, Object>) stats.get("distribution");
        List<Map<String, Object>> subjectAvg = (List<Map<String, Object>>) stats.get("subjectAvg");
        List<Map<String, Object>> students = (List<Map<String, Object>>) stats.get("students");

        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("统计报告_" + classId + "_" + semester + ".xlsx", "UTF-8"));

        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Sheet1: 总体统计
            Sheet s1 = wb.createSheet("总体统计");
            int r = 0;
            s1.createRow(r++).createCell(0).setCellValue("班级成绩统计报告");
            r++;
            s1.createRow(r++).createCell(0).setCellValue("总人数: " + overview.get("totalCount"));
            s1.createRow(r++).createCell(0).setCellValue("班级均分: " + overview.get("classAvg"));
            s1.createRow(r++).createCell(0).setCellValue("最高总分: " + overview.get("highestScore"));
            s1.createRow(r++).createCell(0).setCellValue("最低总分: " + overview.get("lowestScore"));
            s1.createRow(r++).createCell(0).setCellValue("不及格人数: " + overview.get("failCount"));
            s1.createRow(r++).createCell(0).setCellValue("及格率: " + overview.get("passRate") + "%");
            s1.createRow(r++).createCell(0).setCellValue("优秀率: " + overview.get("excellentRate") + "%");
            r++;

            // 分数段分布
            Row distHeader = s1.createRow(r++);
            distHeader.createCell(0).setCellValue("分数段分布");
            Row distLabels = s1.createRow(r++);
            distLabels.createCell(0).setCellValue("优秀(>=90)");
            distLabels.createCell(1).setCellValue("良好(75-89)");
            distLabels.createCell(2).setCellValue("及格(60-74)");
            distLabels.createCell(3).setCellValue("不及格(<60)");
            Row distValues = s1.createRow(r++);
            distValues.createCell(0).setCellValue(toInt(distribution.get("excellent")));
            distValues.createCell(1).setCellValue(toInt(distribution.get("good")));
            distValues.createCell(2).setCellValue(toInt(distribution.get("pass")));
            distValues.createCell(3).setCellValue(toInt(distribution.get("fail")));

            // 各科平均分
            r++;
            s1.createRow(r++).createCell(0).setCellValue("各科平均分");
            Row subHeader = s1.createRow(r++);
            Row subValues = s1.createRow(r++);
            for (int i = 0; i < subjectAvg.size(); i++) {
                subHeader.createCell(i).setCellValue((String) subjectAvg.get(i).get("subject"));
                subValues.createCell(i).setCellValue(toDouble(subjectAvg.get(i).get("avg")));
            }

            // Sheet2: 学生排名
            Sheet s2 = wb.createSheet("学生排名");
            Row rankHeader = s2.createRow(0);
            rankHeader.createCell(0).setCellValue("排名");
            rankHeader.createCell(1).setCellValue("学号");
            rankHeader.createCell(2).setCellValue("姓名");
            for (int i = 0; i < SUBJECTS.length; i++) {
                rankHeader.createCell(3 + i).setCellValue(SUBJECTS[i]);
            }
            rankHeader.createCell(9).setCellValue("总分");
            rankHeader.createCell(10).setCellValue("平均分");

            for (int i = 0; i < students.size(); i++) {
                Row row = s2.createRow(i + 1);
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
            }

            try (OutputStream os = resp.getOutputStream()) {
                wb.write(os);
            }
        }
    }

    /**
     * 导出排序后成绩单
     */
    private void exportExcel(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long classId = Long.parseLong(req.getParameter("classId"));
        String semester = req.getParameter("semester");
        if (semester == null) semester = "2025-2026-1";
        String sortField = req.getParameter("sortField");
        if (sortField == null) sortField = "avgScore";
        String sortOrder = req.getParameter("sortOrder");
        if (sortOrder == null) sortOrder = "desc";

        List<Map<String, Object>> students = scoreService.getSortedStudents(classId, semester, sortField, sortOrder);

        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("成绩单_排序_" + classId + ".xlsx", "UTF-8"));

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("成绩单");
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("排名");
            header.createCell(1).setCellValue("学号");
            header.createCell(2).setCellValue("姓名");
            for (int i = 0; i < SUBJECTS.length; i++) {
                header.createCell(3 + i).setCellValue(SUBJECTS[i]);
            }
            header.createCell(9).setCellValue("总分");
            header.createCell(10).setCellValue("平均分");
            for (int i = 0; i < 11; i++) header.getCell(i).setCellStyle(headerStyle);

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
            }

            try (OutputStream os = resp.getOutputStream()) {
                wb.write(os);
            }
        }
    }

    private int toInt(Object obj) {
        if (obj instanceof Number) return ((Number) obj).intValue();
        return 0;
    }

    private double toDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return 0;
    }
}
