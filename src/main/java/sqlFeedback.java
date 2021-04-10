public class sqlFeedback {
    // very simple pojo to receive feedback
    private int code = -1;
    private String dep;
    private int cred = 0;
    
    public sqlFeedback(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getDep() {
        return dep;
    }
    
    public void setDep(String dep) {
        this.dep = dep;
    }
    
    public int getCred() {
        return cred;
    }
    
    public void setCred(int cred) {
        this.cred = cred;
    }
}
