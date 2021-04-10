public class Response {
    private int success;
    private String details; //error details
    
    //! success(1), controlled error(0), exception(-ve)
    
    public Response(int success, String details) {
        this.success = success;
        this.details = details;
    }
    
    public int getSuccess() {
        return success;
    }
    
    public void setSuccess(int success) {
        this.success = success;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
}
