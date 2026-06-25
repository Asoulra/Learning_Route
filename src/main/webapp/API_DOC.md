# 学生成绩管理系统 - 后端API接口文档

> **版本**: v1.0  
> **基础路径**: `/api`  
> **认证方式**: Bearer Token (JWT)  
> **Content-Type**: `application/json` (除文件上传外)

---

## 通用说明

### 统一响应格式

```json
{
  "code": 200,        // 状态码: 200=成功, 400=参数错误, 401=未授权, 403=无权限, 500=服务器错误
  "message": "操作成功", // 提示信息
  "data": {}           // 业务数据（具体结构见各接口）
}
```

### 错误响应示例

```json
{
  "code": 400,
  "message": "学号不能为空",
  "data": null
}
```

### 认证

所有接口需在请求头中携带 Token：

```
Authorization: Bearer <token>
```

### 分页参数（通用）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |

---

## 一、认证模块 (`/auth`)

### 1.1 用户登录

**POST** `/auth/login`

登录系统，获取访问令牌。

**请求体：**
```json
{
  "username": "admin",      // 用户名或工号
  "password": "123456",     // 密码
  "role": "admin"           // 角色: admin / teacher
}
```

**响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1,
      "username": "admin",
      "name": "管理员",
      "role": "admin",          // admin 或 teacher
      "avatar": null
    },
    "expiresIn": 86400          // Token有效期(秒)
  }
}
```

---

## 二、学生管理模块 (`/students`)

### 2.1 获取学生列表（分页+筛选）

**GET** `/students`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认10 |
| keyword | string | 否 | 搜索关键字（学号/姓名） |
| classId | int | 否 | 班级ID筛选 |
| gender | string | 否 | 性别筛选：男/女 |

**响应：**
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "studentNo": "2024001",
        "name": "张三",
        "gender": "男",
        "classId": 2,
        "className": "高三(1)班",
        "enrollDate": "2024-09-01",
        "phone": "13800138001",
        "address": "北京市朝阳区"
      }
    ],
    "totalPages": 5,
    "totalCount": 48,
    "currentPage": 1
  }
}
```

### 2.2 获取学生统计概览

**GET** `/students/stats`

**响应：**
```json
{
  "code": 200,
  "data": {
    "total": 156,
    "maleCount": 82,
    "femaleCount": 74,
    "classCount": 6
  }
}
```

### 2.3 添加学生

**POST** `/students`

**请求体：**
```json
{
  "studentNo": "2024001",
  "name": "张三",
  "gender": "男",
  "classId": 2,
  "enrollDate": "2024-09-01",
  "phone": "13800138001",
  "address": "北京市朝阳区"
}
```

**响应：**
```json
{ "code": 200, "message": "添加成功", "data": { "id": 10 } }
```

### 2.4 编辑学生

**PUT** `/students/{id}`

**请求体：** 同添加（所有字段可选传）

**响应：**
```json
{ "code": 200, "message": "更新成功", "data": null }
```

### 2.5 删除学生

**DELETE** `/students/{id}`

**响应：**
```json
{ "code": 200, "message": "删除成功", "data": null }
```

---

## 三、班级管理模块 (`/classes`)

### 3.1 获取班级列表

**GET** `/classes`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string |否 | 搜索（班级名称/年级） |
| grade | string | 否 | 年级筛选 |

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "className": "高三(1)班",
      "grade": "2024级",
      "headTeacher": "王老师",
      "roomLocation": "教学楼A301",
      "studentCount": 50,
      "remark": "重点班",
      "createdAt": "2024-09-01T08:00:00"
    }
  ]
}
```

### 3.2 获取班级统计

**GET** `/classes/stats`

**响应：**
```json
{
  "code": 200,
  "data": { "total": 8, "totalStudents": 380, "avgSize": 47.5 }
}
```

### 3.3 添加班级

**POST** `/classes`

**请求体：**
```json
{
  "className": "高三(1)班",
  "grade": "2024级",
  "headTeacher": "王老师",
  "roomLocation": "教学楼A301",
  "remark": ""
}
```

### 3.4 编辑班级

**PUT** `/classes/{id}`

### 3.5 删除班级

**DELETE** `/classes/{id}`

> 注意：删除班级前应确保该班没有关联学生和授课安排

---

## 四、教师管理模块 (`/teachers`)

### 4.1 获取教师列表

**GET** `/teachers`

**查询参数：** keyword, subject

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "teacherNo": "T2024001",
      "name": "李老师",
      "gender": "男",
      "title": "副教授",
      "subject": "数学",
      "phone": "13900139001",
      "email": "li@school.edu.cn"
    }
  ]
}
```

### 4.2 获取教师统计

**GET** `/teachers/stats`

**响应：**
```json
{ "code": 200, "data": { "total": 24, "subjectCount": 9 } }
```

### 4.3 添加教师

**POST** `/teachers`

**请求体：**
```json
{
  "teacherNo": "T2024002",
  "name": "王老师",
  "gender": "女",
  "title": "讲师",
  "subject": "语文",
  "phone": "13900139002",
  "email": "wang@school.edu.cn"
}
```

### 4.4 编辑教师

**PUT** `/teachers/{id}`

### 4.5 删除教师

**DELETE** `/teachers/{id}`

### 4.6 批量导入教师

**POST** `/teachers/import`

**Content-Type**: `multipart/form-data`

**表单字段：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | Excel文件(.xlsx/.xls/.csv)，最大10MB |

**Excel模板格式（第一行为表头）：**

| 工号 | 姓名 | 性别 | 职称 | 授课科目 | 联系电话 | 邮箱 |
|------|------|------|------|---------|----------|------|
| T2024003 | 赵老师 | 女 | 教授 | 英语 | 139... | zhao@... |

**响应：**
```json
{
  "code": 200,
  "message": "导入完成",
  "data": {
    "successCount": 15,
    "failCount": 2,
    "errors": [
      { "row": 5, "reason": "工号重复" },
      { "row": 12, "reason": "性别格式错误" }
    ]
  }
}
```

### 4.7 下载导入模板

**GET** `/teachers/template`

**响应**: 文件流 (Content-Disposition: attachment; filename="教师导入模板.xlsx")

---

## 五、授课安排模块 (`/teaching-assignments`)

### 5.1 获取全部授课安排

**GET** `/teaching-assignments`

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "classId": 2,
      "className": "高三(1)班",
      "teacherId": 5,
      "teacherName": "李老师",
      "title": "副教授",
      "subject": "数学",
      "semester": "2025-2026-1"
    }
  ]
}
```

### 5.2 获取授课安排统计

**GET** `/teaching-assignments/stats`

**响应：**
```json
{ "code": 200, "data": { "classCount": 6, "totalCount": 42, "avgLoad": 7 } }
```

### 5.3 新增授课安排

**POST** `/teaching-assignments`

**请求体：**
```json
{
  "classId": 2,
  "teacherId": 5,
  "subject": "数学",
  "semester": "2025-2026-1",
  "remark": ""
}
```

> 校验规则：
> - 同一班级同一科目同一学期不能重复安排不同教师
> - 同一教师在同一学期对同一班级只能教授一门课（或按业务需求调整）

### 5.4 删除授课安排

**DELETE** `/teaching-assignments/{id}`

---

## 六、成绩录入模块 (`/scores`)

### 6.1 获取某班级学生成绩

**GET** `/scores/class/{classId}?semester=2025-2026-1`

**响应：**
```json
{
  "code": 200,
  "data": {
    "students": [
      {
        "id": 101,
        "studentNo": "2024001",
        "name": "张三",
        "gender": "男",
        "scores": {
          "语文": 92,
          "数学": 88,
          "英语": 85,
          "物理": 78,
          "化学": 90,
          "生物": 82
        }
      }
    ],
    "stats": {
      "count": 50,
      "avg": 86.5,
      "max": 98,
      "min": 52,
      "failCount": 3
    }
  }
}
```

### 6.2 录入/修改单个学生成绩

**POST** `/scores`

**请求体：**
```json
{
  "studentId": 101,
  "classId": 2,
  "semester": "2025-2026-1",
  "scores": {
    "语文": 92,
    "math": 88,
    "english": 85
  }
}
```

> 说明：传入的 scores 字段为需要更新的科目标准分。如果某个科目值为 null，则表示清空该科成绩。

**响应：**
```json
{ "code": 200, "message": "成绩保存成功", "data": null }
```

### 6.3 批量保存成绩

**POST** `/scores/batch`

**请求体：**
```json
[
  {
    "studentId": 101,
    "classId": 2,
    "semester": "2025-2026-1",
    "scores": { "语文": 92, "数学": 88 }
  },
  {
    "studentId": 102,
    "classId": 2,
    "semester": "2025-2026-1",
    "scores": { "语文": 85, "数学": 95 }
  }
]
```

**响应：**
```json
{
  "code": 200,
  "message": "批量保存完成",
  "data": {
    "successCount": 48,
    "failCount": 2
  }
}
```

### 6.4 导出成绩单

**GET** `/scores/export?classId={classId}&semester=xxx`

**响应**: 文件流 (Excel 格式)
```
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="成绩单_高三(1)班_2025-2026-1.xlsx"
```

导出字段：序号、学号、姓名、语文、数学、英语、物理、化学、生物、总分、平均分、排名

---

## 七、成绩统计模块 (`/statistics`)

### 7.1 获取班级统计数据（含排名）

**GET** `/statistics/class/{classId}?semester=xxx&sortField=avgScore&sortOrder=desc`

**查询参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| semester | string | 是 | - | 学期标识 |
| sortField | string | 否 | avgScore | 排序字段：totalScore/avgScore/语文/数学... |
| sortOrder | string | 否 | desc | 排序方向：asc/desc |

**响应：**
```json
{
  "code": 200,
  "data": {
    "overview": {
      "totalCount": 50,
      "classAvg": 86.5,
      "highestScore": 585.0,
      "lowestScore": 320.0,
      "failCount": 3,
      "passRate": 94.0,
      "excellentRate": 28.0
    },
    "distribution": {
      "excellent": 14,
      "good": 22,
      "pass": 11,
      "fail": 3,
      "totalCount": 50
    },
    "subjectAvg": [
      { "subject": "语文", "avg": 84.2 },
      { "subject": "数学", "avg": 89.5 },
      { "subject": "英语", "avg": 81.0 },
      { "subject": "物理", "avg": 87.3 },
      { "subject": "化学", "avg": 83.8 },
      { "subject": "生物", "avg": 79.2 }
    ],
    "students": [
      {
        "id": 101,
        "studentNo": "2024001",
        "name": "张三",
        "gender": "男",
        "语文": 92,
        "数学": 98,
        "英语": 88,
        "物理": 95,
        "化学": 91,
        "生物": 86,
        "totalScore": 550,
        "avgScore": 91.67
      }
    ]
  }
}
```

### 7.2 导出统计报告

**GET** `/statistics/export/report?classId={classId}&semester=xxx`

**响应**: Excel 文件流，包含：
- 班级基本信息与总体统计
- 各分数段人数分布
- 各科目平均分对比
- 学生排名明细

### 7.3 导出排序后成绩单

**GET** `/statistics/export/excel?classId={classId}&semester=xxx&sortField=avgScore&sortOrder=desc`

**响应**: Excel 文件流，按指定字段排序的成绩明细

---

## 八、数据字典

### 性别
| 值 | 说明 |
|----|------|
| 男 | 男性 |
| 女 | 女性 |

### 职称
| 值 | 说明 |
|----|------|
| 教授 | 正高级职称 |
| 副教授 | 副高级职称 |
| 讲师 | 中级职称 |
| 助教 | 初级职称 |

### 授课科目
| 值 | 说明 |
|----|------|
| 语文 | 语文 |
| 数学 | 数学 |
| 英语 | 英语 |
| 物理 | 物理 |
| 化学 | 化学 |
| 生物 | 生物 |
| 历史 | 历史 |
| 地理 | 地理 |
| 政治 | 政治 |

### 学期格式
- 格式：`{起始学年}-{结束学年}-{学期}`
- 示例：`2025-2026-1` 表示 2025-2026学年第一学期

### 成绩范围
- 最小值：`0`
- 最大值：`100`
- 支持小数精度：`0.5`
- 缺考/未录入：`null`

### 分数段划分标准
| 分数段 | 标签 | 范围 |
|--------|------|------|
| excellent | 优秀 | >= 90 |
| good | 良好 | >= 75 且 < 90 |
| pass | 及格 | >= 60 且 < 75 |
| fail | 不及格 | < 60 |

---

## 九、前端文件清单

```
webapp/
├── index.jsp                          # 入口页（重定向到登录）
├── css/
│   └── pub/
│       └── common.css                 # 全局公共样式
├── js/
│   └── pub/
│       └── common.js                  # 公共JS工具库（API封装/Toast/Modal等）
├── html/
│   ├── pub/
│   │   └── login/
│   │       └── index.html             # 登录页面
│   └── personal/
│       ├── admin/
│       │   ├── students.html          # 管理员 - 学生管理（增删改查）
│       │   ├── classes.html           # 管理员 - 班级管理（增删改查）
│       │   ├── teachers.html          # 管理员 - 教师管理（增删改查 + 批量导入）
│       │   ├── teaching-assign.html   # 管理员 - 授课安排
│       │   └── statistics.html        # 管理员 - 成绩统计（含导出功能）
│       └── teach/
│           └── scores.html            # 教师 - 成绩录入/修改（按班级）
└── WEB-INF/
    └── web.xml                        # Web配置
```

---

## 十、技术要求

### 后端实现建议
- **框架**: Spring Boot 2.x / 3.x
- **数据库**: MySQL 8.0+
- **ORM**: MyBatis-Plus / JPA
- **权限**: Spring Security + JWT
- **文件处理**: Apache POI (Excel 导入导出)
- **文档**: Swagger/OpenAPI 3.0

### 数据库核心表设计建议

```sql
-- 学生表 t_student
CREATE TABLE t_student (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_no VARCHAR(20) UNIQUE NOT NULL COMMENT '学号',
  name VARCHAR(30) NOT NULL COMMENT '姓名',
  gender CHAR(1) NOT NULL COMMENT '性别',
  class_id BIGINT COMMENT '班级ID',
  enroll_date DATE COMMENT '入学日期',
  phone VARCHAR(20) COMMENT '电话',
  address VARCHAR(200) COMMENT '地址',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 班级表 t_class
CREATE TABLE t_class (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_name VARCHAR(50) NOT NULL COMMENT '班级名',
  grade VARCHAR(20) COMMENT '年级',
  head_teacher VARCHAR(30) COMMENT '班主任',
  room_location VARCHAR(50) COMMENT '教室位置',
  remark VARCHAR(200) COMMENT '备注',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 教师表 t_teacher
CREATE TABLE t_teacher (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_no VARCHAR(20) UNIQUE NOT NULL COMMENT '工号',
  name VARCHAR(30) NOT NULL COMMENT '姓名',
  gender CHAR(1) NOT NULL COMMENT '性别',
  title VARCHAR(20) COMMENT '职称',
  subject VARCHAR(20) NOT NULL COMMENT '授课科目',
  phone VARCHAR(20) COMMENT '电话',
  email VARCHAR(80) COMMENT '邮箱',
  status TINYINT DEFAULT 1 COMMENT '状态 1在职 0离职'
);

-- 授课安排表 t_teaching_assignment
CREATE TABLE t_teaching_assignment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  subject VARCHAR(20) NOT NULL,
  semester VARCHAR(20) NOT NULL,
  remark VARCHAR(200),
  UNIQUE KEY uk_class_sub_sem(class_id, subject, semester)
);

-- 成绩表 t_score
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
  UNIQUE KEY uk_stu_class_sem(student_id, class_id, semester),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```
