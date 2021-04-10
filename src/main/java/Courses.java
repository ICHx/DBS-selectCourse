public class Courses {
    
    private String courseId;
    private String courseName;
    private String preq;
    private String weekday;
    private String start;
    private String end;
    private String cred;
    private String qta;
    private String remarks;
    
    
    public String getCourseId() {
        return courseId;
    }
    
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
    
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    
    public String getPreq() {
        return preq;
    }
    
    public void setPreq(String preq) {
        this.preq = preq;
    }
    
    
    public String getWeekday() {
        return weekday;
    }
    
    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }
    
    
    public String getStart() {
        return start;
    }
    
    public void setStart(String start) {
        this.start = start;
    }
    
    
    public String getEnd() {
        return end;
    }
    
    public void setEnd(String end) {
        this.end = end;
    }
    
    
    public String getCred() {
        return cred;
    }
    
    public void setCred(String cred) {
        this.cred = cred;
    }
    
    
    public String getQta() {
        return qta;
    }
    
    public void setQta(String qta) {
        this.qta = qta;
    }
    
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    @Override
    public String toString() {
        return "Courses [courseId=" + courseId + ", courseName=" + courseName + ", cred=" + cred + ", end=" + end
            + ", preq=" + preq + ", qta=" + qta + ", remarks=" + remarks + ", start=" + start + ", weekday=" + weekday
            + "]";
    }
    
}
