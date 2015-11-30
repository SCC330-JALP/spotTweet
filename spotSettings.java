public class spotSettings{
    
    private boolean alive;
    private int battery;
    private String name;
    private String task;
    private int zone;

    public spotSettings(){};

    public boolean getAlive(){
        return alive;
    }
    
    public int getBattery(){
        return battery;
    }
    
    public String getName(){
        return name;
    }
    
    public String getTask(){
        return task;
    }
    
    public int getZone(){
        return zone;
    }
}