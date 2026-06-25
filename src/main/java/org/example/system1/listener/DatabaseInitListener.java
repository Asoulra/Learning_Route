package org.example.system1.listener;

import org.example.system1.util.DBUtil;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * 数据库初始化监听器
 * Tomcat 启动时自动建库建表 + 插入测试数据
 */
@WebListener
public class DatabaseInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[Init] Starting database initialization...");
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            // 检查 t_user 表是否存在
            try {
                stmt.executeQuery("SELECT 1 FROM t_user LIMIT 1");
                System.out.println("[Init] Tables already exist, skipping init.");
                return;
            } catch (Exception e) {
                // 表不存在，需要创建
                System.out.println("[Init] Tables not found, creating...");
            }

            // 读取并执行 schema.sql
            executeScript(stmt, "sql/schema.sql");

            // 读取并执行 data.sql
            executeScript(stmt, "sql/data.sql");

            // 更新密码为真实 BCrypt 哈希
            String realHash = BCrypt.hashpw("123456", BCrypt.gensalt(10));
            stmt.executeUpdate("UPDATE t_user SET password='" + realHash + "'");
            System.out.println("[Init] Passwords updated with real BCrypt hashes.");

            System.out.println("[Init] Database initialization completed successfully!");

        } catch (Exception e) {
            System.err.println("[Init] Database init failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 执行 SQL 脚本（按分号分割语句，逐条执行）
     */
    private void executeScript(Statement stmt, String scriptPath) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(scriptPath);
        if (in == null) {
            throw new RuntimeException("SQL script not found: " + scriptPath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String content = reader.lines().collect(Collectors.joining("\n"));
            // 移除注释行
            content = content.replaceAll("(?m)^\\s*--.*$", "");
            // 按分号分割
            String[] statements = content.split(";");
            for (String sql : statements) {
                sql = sql.trim();
                if (sql.isEmpty()) continue;
                try {
                    stmt.execute(sql);
                } catch (Exception e) {
                    // 容错：忽略单条失败
                    System.err.println("[Init] SQL failed (ignored): " + sql.substring(0, Math.min(80, sql.length())) + "...");
                }
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // do nothing
    }
}
