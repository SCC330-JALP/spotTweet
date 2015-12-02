public class Notification{

    private long timestamp;
    private String msg;

    public Notification() {}

    public Notification(String msg){
        this.msg = msg;
        this.timestamp = System.currentTimeMillis();
    }
    public String getMsg(){
        return msg;
    }

    public long getTimestamp(){
        return timestamp;
    }

}