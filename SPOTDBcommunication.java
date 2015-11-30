import com.firebase.client.*;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.util.*;
import java.text.*;
import java.util.concurrent.*;


public class SPOTDBcommunication{

    public static Firebase ref = new Firebase("https://sunsspot.firebaseio.com/");
    public Firebase spotSettings = null;


    public SPOTDBcommunication(){

      spotSettings = ref.child("spotSettings");

    }

    //Monitor if battery level is less than 50%, if so, tweet it.
    public void monitorBatteries(){
        spotSettings.addValueEventListener(new ValueEventListener() {
              
              @Override
              public void onDataChange(DataSnapshot snapshot) {
                  for (DataSnapshot entrySnapshot: snapshot.getChildren()) {
                    spotSettings entry = entrySnapshot.getValue(spotSettings.class);

                    //Check if battery percentage is less than 50
                    if(entry.getBattery() < 50){
                      post("Sensor '" + entry.getName() + "' battery is low. Current percentage: " + entry.getBattery() + "%");
                    }
                  }
              }

              @Override
              public void onCancelled(FirebaseError firebaseError) {
                  System.out.println("The read failed: " + firebaseError.getMessage());
              }
          });
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

    //Return the current time value in correct format.
    //@return - Formatted current date.
    public String currentTime(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
