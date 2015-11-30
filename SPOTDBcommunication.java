import com.firebase.client.*;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.util.*;
import java.text.*;
import java.util.concurrent.*;


public class SPOTDBcommunication{

    public static Firebase ref = new Firebase("https://sunsspot.firebaseio.com/");
    public Firebase logRef = null;

    public SPOTDBcommunication(String childName){

      logRef = ref.child(childName);

    }

    public void monitor(){
        logRef.addValueEventListener(new ValueEventListener() {
              
              @Override
              public void onDataChange(DataSnapshot snapshot) {
                  for (DataSnapshot entrySnapshot: snapshot.getChildren()) {
                    spotSettings entry = entrySnapshot.getValue(spotSettings.class);

                    //Check Battery
                    if(entry.getBattery() < 50){
                      post("Sensor '" + entry.getName() + "' battery is low. Current value: " + entry.getBattery() + "%");
                    }
                  }
              }

              @Override
              public void onCancelled(FirebaseError firebaseError) {
                  System.out.println("The read failed: " + firebaseError.getMessage());
              }
          });
    }

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

    public String currentTime(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
