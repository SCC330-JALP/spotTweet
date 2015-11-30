import com.firebase.client.*;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.util.*;
import java.text.*;
import java.util.concurrent.*;


public class SPOTDBcommunication{

    public static Firebase ref = new Firebase("https://sunsspot.firebaseio.com/");
    public Firebase spotSettings = null;
    public Firebase spotReadings = null;

    public Firebase spot = null;
    public Firebase spotZone = null;
    public int zoneNumber = 0;

    public SPOTDBcommunication(){

      spotSettings = ref.child("spotSettings");
      spotReadings = ref.child("spotReadings");

    }

    //Monitor
    public void monitor(){
        spotReadings.addValueEventListener(new ValueEventListener() {
              
              @Override
              public void onDataChange(DataSnapshot snapshot) {
                  for (DataSnapshot entrySnapshot: snapshot.getChildren()) {
                    spotReadings.child(entrySnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    

                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            // System.out.println(snapshot.getKey());
                            // System.out.println("---------------------");
                            spot = spotReadings.child(snapshot.getKey());
                            
                            
                            for (DataSnapshot entrySnapshot: snapshot.getChildren()) {
                                // System.out.println(entrySnapshot.getKey());

                                //Set it to Zone reference
                                spotZone = spot.child("zone");
                                com.firebase.client.Query queryRef = spotZone.limitToLast(1);
                                
                                queryRef.addValueEventListener(new ValueEventListener() {
                
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        for (DataSnapshot entrySnapshot: snapshot.getChildren()) {
                                            Sensor entry = entrySnapshot.getValue(Sensor.class);
                                            zoneNumber = (int) entry.getNewVal();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {
                                        System.out.println("The read failed: " + firebaseError.getMessage());
                                    }
                                });
                              





                              //MOTION SENSOR
                              if((entrySnapshot.getKey()).equals("motion")){

                                //Set it to Motion reference
                                spot = spot.child(entrySnapshot.getKey());

                                spot.addValueEventListener(new ValueEventListener() {
              
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        for (DataSnapshot entrySnapshot: snapshot.getChildren()) {
                                          Sensor entry = entrySnapshot.getValue(Sensor.class);
                                          
                                          //Within 5 mins
                                          if(System.currentTimeMillis() - entry.getTimestamp() < 300000){
                                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                            Date date = new Date(entry.getTimestamp());
                                            post("!! Zone: " + zoneNumber + " Motion Detected at " + dateFormat.format(date) + " !!");
                                          }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {
                                        System.out.println("The read failed: " + firebaseError.getMessage());
                                    }
                                });

                              }





                              //DOOR SENSOR
                              if((entrySnapshot.getKey()).equals("door")){

                                //Set it to door reference
                                spot = spot.child(entrySnapshot.getKey());

                                spot.addValueEventListener(new ValueEventListener() {
              
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        for (DataSnapshot entrySnapshot: snapshot.getChildren()) {
                                          Sensor entry = entrySnapshot.getValue(Sensor.class);
                                          
                                          //Within 5 mins
                                          if(System.currentTimeMillis() - entry.getTimestamp() < 300000){
                                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                            Date date = new Date(entry.getTimestamp());
                                            post(" Door Opened at " + dateFormat.format(date));
                                          }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {
                                        System.out.println("The read failed: " + firebaseError.getMessage());
                                    }
                                });

                              }

                            }
                            
                            // System.out.println("---------------------");
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            System.out.println("The read failed: " + firebaseError.getMessage());
                        }
                    });
                  }
              }

              @Override
              public void onCancelled(FirebaseError firebaseError) {
                  System.out.println("The read failed: " + firebaseError.getMessage());
              }
          });
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
