-- ========================================
-- 学生成绩管理系统 - 数据库初始化脚本
-- ========================================

CREATE DATABASE IF NOT EXISTS system1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE system1;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 成绩表（包含学生ID、班级ID、学期、各科成绩、更新时间等信息）
-- 一键清除现有数据
DROP TABLE IF EXISTS t_score;
CREATE TABLE t_score (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL COMMENT '学生ID',
  class_id BIGINT NOT NULL COMMENT '班级ID',
  semester VARCHAR(20) NOT NULL COMMENT '学期',
  chinese DECIMAL(5,1) COMMENT '语文',
  math DECIMAL(5,1) COMMENT '数学',
  english DECIMAL(5,1) COMMENT '英语',
  physics DECIMAL(5,1) COMMENT '物理',
  chemistry DECIMAL(5,1) COMMENT '化学',
  biology DECIMAL(5,1) COMMENT '生物',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_stu_class_sem (student_id, class_id, semester),
  FOREIGN KEY (student_id) REFERENCES t_student(id) ON DELETE CASCADE,
  FOREIGN KEY (class_id) REFERENCES t_class(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- 授课安排表
DROP TABLE IF EXISTS t_teaching_assignment;
CREATE TABLE t_teaching_assignment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL COMMENT '班级ID',
  teacher_id BIGINT NOT NULL COMMENT '教师ID',
  subject VARCHAR(20) NOT NULL COMMENT '授课科目',
  semester VARCHAR(20) NOT NULL COMMENT '学期',
  remark VARCHAR(200) COMMENT '备注',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_class_sub_sem (class_id, subject, semester),
  FOREIGN KEY (class_id) REFERENCES t_class(id) ON DELETE CASCADE,
  FOREIGN KEY (teacher_id) REFERENCES t_teacher(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='授课安排表';

-- 学生表
DROP TABLE IF EXISTS t_student;
CREATE TABLE t_student (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_no VARCHAR(20) UNIQUE NOT NULL COMMENT '学号',
  name VARCHAR(30) NOT NULL COMMENT '姓名',
  gender VARCHAR(2) NOT NULL COMMENT '性别',
  class_id BIGINT COMMENT '班级ID',
  enroll_date DATE COMMENT '入学日期',
  phone VARCHAR(20) COMMENT '联系电话',
  address VARCHAR(200) COMMENT '家庭住址',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (class_id) REFERENCES t_class(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 教师表
DROP TABLE IF EXISTS t_teacher;
CREATE TABLE t_teacher (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_no VARCHAR(20) UNIQUE NOT NULL COMMENT '工号',
  name VARCHAR(30) NOT NULL COMMENT '姓名',
  gender VARCHAR(2) NOT NULL COMMENT '性别',
  title VARCHAR(20) COMMENT '职称',
  subject VARCHAR(20) NOT NULL COMMENT '授课科目',
  phone VARCHAR(20) COMMENT '联系电话',
  email VARCHAR(80) COMMENT '邮箱',
  status TINYINT DEFAULT 1 COMMENT '状态:1在职 0离职',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 班级表
DROP TABLE IF EXISTS t_class;
CREATE TABLE t_class (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_name VARCHAR(50) NOT NULL COMMENT '班级名称',
  grade VARCHAR(20) COMMENT '年级',
  head_teacher VARCHAR(30) COMMENT '班主任',
  room_location VARCHAR(50) COMMENT '教室位置',
  remark VARCHAR(200) COMMENT '备注',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 用户表
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(30) NOT NULL UNIQUE COMMENT '用户名/工号',
  password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  name VARCHAR(30) NOT NULL COMMENT '姓名',
  role VARCHAR(10) NOT NULL COMMENT '角色: admin/teacher',
  avatar VARCHAR(200) COMMENT '头像',
  status TINYINT DEFAULT 1 COMMENT '状态:1启用 0禁用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;
