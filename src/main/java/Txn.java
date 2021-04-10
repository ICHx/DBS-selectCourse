public class Txn implements RecordEntry {
    
    private long txnNo;
    private String netid;
    private String courseId;
    
    
    public long getTxnNo() {
        return txnNo;
    }
    
    public void setTxnNo(long txnNo) {
        this.txnNo = txnNo;
    }
    
    
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
    
    @Override
    public String toString() {
        return "Txn [courseId=" + courseId + ", netid=" + netid + ", txnNo=" + txnNo + "]";
    }
    
}
