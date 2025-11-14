
package uruhingore.ua.dto;

public class SubjectGrade {
    private String subjectName;
    private double score;

    public SubjectGrade() {
    }

    public SubjectGrade(String subjectName, double score) {
        this.subjectName = subjectName;
        this.score = score;
    }

    // Getters and Setters
    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}