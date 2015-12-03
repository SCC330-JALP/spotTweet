import com.firebase.client.*;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.util.*;
import java.text.*;
import java.util.concurrent.*;

class Tweet implements Runnable{
    private Thread t;
    private String threadName;
    private String msg;

    public static Firebase notiRef = new Firebase("https://sunsspot.firebaseio.com/notification");
    public Notification newEntry = null;

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
          update(msg);

        }catch(TwitterException e){
          System.out.println(e);
        }catch(InterruptedException e){
            System.out.println(e);
        }
        
    }

    public void update(String msg) throws InterruptedException{

        final CountDownLatch done = new CountDownLatch(1);

        newEntry = new Notification(msg);

        notiRef.push().setValue(newEntry, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                System.out.println("done");
                done.countDown();
            }
        });
        done.await();
    }

}