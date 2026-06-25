package org.example.system1.entity;

public class Score {
    private Long id;
    private Long studentId;
    private Long classId;
    private String semester;
    private Double chinese;
    private Double math;
    private Double english;
    private Double physics;
    private Double chemistry;
    private Double biology;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public Double getChinese() { return chinese; }
    public void setChinese(Double chinese) { this.chinese = chinese; }
    public Double getMath() { return math; }
    public void setMath(Double math) { this.math = math; }
    public Double getEnglish() { return english; }
    public void setEnglish(Double english) { this.english = english; }
    public Double getPhysics() { return physics; }
    public void setPhysics(Double physics) { this.physics = physics; }
    public Double getChemistry() { return chemistry; }
    public void setChemistry(Double chemistry) { this.chemistry = chemistry; }
    public Double getBiology() { return biology; }
    public void setBiology(Double biology) { this.biology = biology; }
}
