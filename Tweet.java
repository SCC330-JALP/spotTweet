import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.util.*;
import java.text.*;
import java.util.concurrent.*;

class Tweet implements Runnable{
    private Thread t;
    private String threadName;
    private String msg;

    Tweet(String name, String m){
        threadName = name;
        msg = m;
        System.out.println("Creating " + threadName);
    }

    public void run(){
        System.out.println("Running " + threadName);
        post(msg);
        System.out.println("Thread " +  threadName + " exiting.");
    }
    


    public void start()
    {
        System.out.println("Starting " + threadName);
        if(t == null){
            t = new Thread(this, threadName);
            t.start();
        }
    }

    //Post message to Twitter
    //@param {String} msg - Message that you want to tweet.
    public void post(String msg){
        try{
          // The factory instance is re-useable and thread safe.
          Twitter twitter = TwitterFactory.getSingleton();
          Status status = twitter.updateStatus(msg);
          System.out.println("Successfully updated the status to [" + status.getText() + "].");

        }catch(TwitterException e){
          System.out.println(e);
        }
        
    }

}