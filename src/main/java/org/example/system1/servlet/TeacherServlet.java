package org.example.system1.servlet;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.system1.entity.Teacher;
import org.example.system1.service.TeacherService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

@WebServlet("/api/teachers/*")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024)
public class TeacherServlet extends BaseServlet {
    private final TeacherService teacherService = new TeacherService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/stats".equals(pathInfo)) {
            success(resp, teacherService.getStats());
            return;
        }

        if ("/list".equals(pathInfo)) {
            success(resp, teacherService.getList(null, null));
            return;
        }

        if ("/template".equals(pathInfo)) {
            downloadTemplate(resp);
            return;
        }

        Long id = getPathId(req);
        if (id != null) {
            Teacher t = teacherService.getById(id);
            if (t != null) {
                success(resp, t);
            } else {
                fail(resp, 404, "教师不存在");
            }
            return;
        }

        String keyword = req.getParameter("keyword");
        String subject = req.getParameter("subject");
        success(resp, teacherService.getList(keyword, subject));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String pathInfo = req.getPathInfo();

        if ("/import".equals(pathInfo)) {
            handleImport(req, resp);
            return;
        }

        try {
            Teacher teacher = parseBody(req, Teacher.class);
            Map<String, Object> data = teacherService.add(teacher);
            success(resp, "添加成功", data);
        } catch (Exception e) {
            fail(resp, 400, "添加失败: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = getPathId(req);
        if (id == null) {
            fail(resp, 400, "缺少教师ID");
            return;
        }
        try {
            Teacher teacher = parseBody(req, Teacher.class);
            teacher.setId(id);
            teacherService.update(teacher);
            success(resp, "更新成功", null);
        } catch (Exception e) {
            fail(resp, 400, "更新失败: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = getPathId(req);
        if (id == null) {
            fail(resp, 400, "缺少教师ID");
            return;
        }
        try {
            teacherService.delete(id);
            success(resp, "删除成功", null);
        } catch (Exception e) {
            fail(resp, 400, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 处理 Excel/CSV 导入
     */
    private void handleImport(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            Part filePart = req.getPart("file");
            if (filePart == null) {
                fail(resp, 400, "请上传文件");
                return;
            }

            String fileName = filePart.getSubmittedFileName();
            List<Teacher> teachers;

            if (fileName != null && (fileName.endsWith(".csv") || fileName.endsWith(".CSV"))) {
                teachers = parseCsv(filePart);
            } else {
                teachers = parseExcel(filePart);
            }

            Map<String, Object> result = teacherService.importTeachers(teachers);
            success(resp, "导入完成", result);
        } catch (Exception e) {
            e.printStackTrace();
            fail(resp, 500, "导入失败: " + e.getMessage());
        }
    }

    /**
     * 解析 Excel 文件
     */
    private List<Teacher> parseExcel(Part filePart) throws IOException {
        List<Teacher> teachers = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream is = filePart.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Teacher t = new Teacher();
                t.setTeacherNo(getCellValueSafe(row, 0, formatter));
                t.setName(getCellValueSafe(row, 1, formatter));
                t.setGender(getCellValueSafe(row, 2, formatter));
                t.setTitle(getCellValueSafe(row, 3, formatter));
                t.setSubject(getCellValueSafe(row, 4, formatter));
                t.setPhone(getCellValueSafe(row, 5, formatter));
                t.setEmail(getCellValueSafe(row, 6, formatter));
                teachers.add(t);
            }
        }
        return teachers;
    }

    /**
     * 解析 CSV 文件
     */
    private List<Teacher> parseCsv(Part filePart) throws IOException {
        List<Teacher> teachers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(filePart.getInputStream(), "UTF-8"))) {
            String header = br.readLine(); // 跳过表头
            if (header == null) return teachers;

            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] cols = splitCsvLine(line);
                if (cols.length < 2) continue; // 至少要有工号和姓名

                Teacher t = new Teacher();
                t.setTeacherNo(cols.length > 0 ? cols[0].trim() : null);
                t.setName(cols.length > 1 ? cols[1].trim() : null);
                t.setGender(cols.length > 2 ? cols[2].trim() : null);
                t.setTitle(cols.length > 3 ? cols[3].trim() : null);
                t.setSubject(cols.length > 4 ? cols[4].trim() : null);
                t.setPhone(cols.length > 5 ? cols[5].trim() : null);
                t.setEmail(cols.length > 6 ? cols[6].trim() : null);
                teachers.add(t);
            }
        }
        return teachers;
    }

    /**
     * 简单 CSV 行分割（支持引号包裹的字段）
     */
    private String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * 下载导入模板
     */
    private void downloadTemplate(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("教师导入模板.xlsx", "UTF-8"));

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("教师信息");
            Row header = sheet.createRow(0);
            String[] headers = {"工号", "姓名", "性别", "职称", "授课科目", "联系电话", "邮箱"};
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 示例数据
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("T2025001");
            row.createCell(1).setCellValue("张老师");
            row.createCell(2).setCellValue("男");
            row.createCell(3).setCellValue("讲师");
            row.createCell(4).setCellValue("数学");
            row.createCell(5).setCellValue("13800138000");
            row.createCell(6).setCellValue("zhang@school.edu.cn");

            try (OutputStream os = resp.getOutputStream()) {
                wb.write(os);
            }
        }
    }

    /**
     * 安全获取单元格值（使用 DataFormatter 替代已废弃的 setCellType）
     */
    private String getCellValueSafe(Row row, int col, DataFormatter formatter) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String val = formatter.formatCellValue(cell);
        return (val == null || val.isEmpty()) ? null : val.trim();
    }
}
