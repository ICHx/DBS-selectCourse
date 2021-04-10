public class History implements RecordEntry {
    
    private long RecNo;
    private String netid;
    private String courseId;
    
    
    public String getNetid() {
        return netid;
    }
    
    public void setNetid(String netid) {
        this.netid = netid;
    }
    
    
    public String getCourseId() {
        return courseId;
    }
    
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
    
    public long getRecNo() {
        return RecNo;
    }
    
    public void setRecNo(long recNo) {
        RecNo = recNo;
    }
    
}
