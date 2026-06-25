package org.example.system1.entity;

public class ClassInfo {
    private Long id;
    private String className;
    private String grade;
    private String headTeacher;
    private String roomLocation;
    private String remark;
    private String createdAt;
    private Integer studentCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getHeadTeacher() { return headTeacher; }
    public void setHeadTeacher(String headTeacher) { this.headTeacher = headTeacher; }
    public String getRoomLocation() { return roomLocation; }
    public void setRoomLocation(String roomLocation) { this.roomLocation = roomLocation; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
}
