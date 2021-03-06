import com.firebase.client.*;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.util.*;
import java.text.*;
import java.util.concurrent.*;


public class SPOTDBcommunication{

    public static Firebase ref = new Firebase("https://sunsspot.firebaseio.com/");
    public static Firebase refZone2 = new Firebase("https://sunsspot.firebaseio.com/zone2");
    public static Firebase notiRef = new Firebase("https://sunsspot.firebaseio.com/notification");
    public static Firebase kettleTimer = new Firebase("https://sunsspot.firebaseio.com/kettle/autoTimer");
    public static Firebase kettleBoilRef = new Firebase("https://sunsspot.firebaseio.com/kettle/boil");
    public static Firebase lampRef = new Firebase("https://sunsspot.firebaseio.com/lamps/task");

    public Notification newEntry = null;
    public Boil boil = null;

    public Firebase spotSettings = null;
    public Firebase spotReadings = null;

    public Firebase spot = null;
    public Firebase spotZone = null;
    public int zoneNumber = 0;

    public int autoTimer = 0;

    public long todays7am; //in milliseconds
    public boolean firstPersonEntered = false;

    int numOfSp = 0;
    int numOfInactiveSp = 0;
    Map<Integer, Boolean> motionSensors = new HashMap<Integer, Boolean>();
    Map<Integer, Boolean> soundSensors = new HashMap<Integer, Boolean>();
    Map<Integer, Boolean> doorSensors = new HashMap<Integer, Boolean>();

    public SPOTDBcommunication(){

      spotSettings = ref.child("spotSettings");
      spotReadings = ref.child("spotReadings");

      try{
        todays7am = getTodays7am();
      }catch(ParseException e){
        System.out.println(e);
      }

    }

    //Get Today's 7am in milliseconds
    public long getTodays7am() throws ParseException{

      SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
    
      Calendar calendar = Calendar.getInstance();
      int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
      String dayOfMonthStr = String.valueOf(dayOfMonth);

      int monthOfYear = calendar.get(Calendar.MONTH)+1;
      String monthOfYearStr = String.valueOf(monthOfYear);

      int year = calendar.get(Calendar.YEAR);
      String yearStr = String.valueOf(year);

      String dateInString = dayOfMonthStr + "-" + monthOfYearStr + "-" + year + " 07:00:00";
      Date date = sdf.parse(dateInString);

      calendar.setTime(date);
      return calendar.getTimeInMillis();
    }

    //Monitor
    public void monitorActivities(){
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

                                //Get zone number
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
                              if(entrySnapshot.getKey().equals("motion")){

                                //Set it to Motion reference
                                spot = spot.child(entrySnapshot.getKey());

                                spot.addValueEventListener(new ValueEventListener() {
              
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        for (DataSnapshot entrySnapshot: snapshot.getChildren()) {
                                          Sensor entry = entrySnapshot.getValue(Sensor.class);
                                          
                                          //Within 5 mins
                                          if(System.currentTimeMillis() - entry.getTimestamp() < 600000){
                                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                            Date date = new Date(entry.getTimestamp());
                                            
                                            Tweet tweetMotion = new Tweet("Tweet-Motion", "!! Zone: " + zoneNumber + " Motion Detected at " + dateFormat.format(date) + " !!");
                                            tweetMotion.start();


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
                                          if(System.currentTimeMillis() - entry.getTimestamp() < 600000){
                                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                            Date date = new Date(entry.getTimestamp());
                                            Tweet tweetDoor = new Tweet("Thread-Door", " Door Opened at " + dateFormat.format(date));
                                            tweetDoor.start();
                                          }

                                          //First person enter the lab
                                          if(entry.getTimestamp() > todays7am && firstPersonEntered == false){
                                            firstPersonEntered = true;
                                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                            Date date = new Date(entry.getTimestamp());
                                            Tweet tweetFirstPerson = new Tweet("Thread-First-Person", "First person entered at " + dateFormat.format(date) + ", the kettle has started boiling  for you :)");
                                            tweetFirstPerson.start();
                                            try{
                                              boil();
                                            }catch(InterruptedException e){
                                              System.out.println(e);
                                            }
                                            
                                            
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

    //Monitor if battery level is less than 10%, if so, tweet it.
    public void monitorBatteries(){
        spotSettings.addValueEventListener(new ValueEventListener() {
              
              @Override
              public void onDataChange(DataSnapshot snapshot) {
                  for (DataSnapshot entrySnapshot: snapshot.getChildren()) {
                    spotSettings entry = entrySnapshot.getValue(spotSettings.class);

                    //Check if battery percentage is less than 10
                    if(entry.getBattery() < 10){
                      String msg = "Sensor '" + entry.getName() + "' battery is low. Current percentage: " + entry.getBattery() + "%";
                      Tweet tweetBattery = new Tweet(entry.getName(), msg);
                      tweetBattery.start();
                      // update(msg)
                    }
                  }
              }

              @Override
              public void onCancelled(FirebaseError firebaseError) {
                  System.out.println("The read failed: " + firebaseError.getMessage());
              }
          });
    }


    //Monitor light is higher than ??? or less than ???
    public void monitorZone(final String zoneName){

        com.firebase.client.Query queryRef = ref.child(zoneName).limitToLast(30);


        queryRef.addValueEventListener(new ValueEventListener() {
              
              @Override
              public void onDataChange(DataSnapshot snapshot) {
                  for (DataSnapshot entrySnapshot: snapshot.getChildren()) {
                    Zone entry = entrySnapshot.getValue(Zone.class);
                    
                    //If latest temperature is more than 35
                    if(entry.getTemp() > 35){
                      Tweet tweetTemp = new Tweet("Thread-Temp-hot", "It's getting hot in " + zoneName + "! - " + entry.getTemp() + " degrees celsius");
                      tweetTemp.start();
                    }

                    //If latest temperature is more than 35
                    if(entry.getTemp() < 20){
                      Tweet tweetTemp2 = new Tweet("Thread-Temp-cold", "It's getting cold in " + zoneName + "!  - " + entry.getTemp() + " degrees celsius");
                      tweetTemp2.start();
                    }

                    //If light is greater than 100
                    if(entry.getLight() > 100){
                      Tweet tweetLight = new Tweet("Thread-Light-bright", "So bright in " + zoneName + "! - " + entry.getLight() + " ");
                      tweetLight.start();
                    }

                    //If light is less than 10
                    if(entry.getLight() < 10){
                      Tweet tweetLight2 = new Tweet("Thread-Light-dark", "So dark in " + zoneName + "! Turn on the light? - " + entry.getLight() + " ");
                      tweetLight2.start();
                    }

                  }
              }

              @Override
              public void onCancelled(FirebaseError firebaseError) {
                  System.out.println("The read failed: " + firebaseError.getMessage());
              }
          });
    }



    //Tweet if no one's in the lab
    public void scan() {
      
        spotSettings.addValueEventListener(new ValueEventListener() {
              
              @Override
              public void onDataChange(DataSnapshot snapshot) {

                  for (DataSnapshot entrySnapshot: snapshot.getChildren()) {
                    spotSettings entry = entrySnapshot.getValue(spotSettings.class);

                    // Count the number of spots called "sp"
                    if(entry.getTask().equals("sp")){
                      numOfSp++;
                      System.out.println(entry.getName() + " is " + entry.getAlive());
                      //Count the number of spots called "sp" and it's inactive
                      if(entry.getAlive().equals("false")){
                        numOfInactiveSp++;
                      }
                      //AND SPOT with task "sv" (sound detector), "sm" (motion) or "sd" (door) adds new entries to their readings
                    }

                    //MOTION SENSORS - Check if there's recent entries (within 10 minutes)
                    if(entry.getTask().equals("sm")){
                      String spotKey = entrySnapshot.getKey(); //etc. 0014 4F01 0000 76D3
                      spotReadings.child(spotKey).child("motion").addValueEventListener(new ValueEventListener(){
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            int i = 0;
                            for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                                  Sensor sensor = postSnapshot.getValue(Sensor.class);
                                  
                                  //within 10 minutes
                                  if(System.currentTimeMillis() - sensor.getTimestamp() < 600000){
                                    motionSensors.put(i, true);
                                    System.out.println("motion sensor " + i);
                                  }
                                  i++;
                            }
                        }
                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            System.out.println("The read failed: " + firebaseError.getMessage());
                        }
                      });
                    }





                    //SOUND SENSORS - Check if there's recent entries (within 10 minutes)
                    if(entry.getTask().equals("sv")){
                      String spotKey = entrySnapshot.getKey(); //etc. 0014 4F01 0000 76D3
                      spotReadings.child(spotKey).child("sound").addValueEventListener(new ValueEventListener(){
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            int i = 0;
                            for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                                  Sensor sensor = postSnapshot.getValue(Sensor.class);
                                  
                                  //within 10 minutes
                                  if(System.currentTimeMillis() - sensor.getTimestamp() < 600000){
                                    soundSensors.put(i, true);
                                    System.out.println("soundSensors " + i);
                                  }
                                  i++;
                            }
                        }
                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            System.out.println("The read failed: " + firebaseError.getMessage());
                        }
                      });
                    }


                  //DOOR SENSORS - Check if there's recent entries (within 10 minutes)
                  if(entry.getTask().equals("sd")){
                      String spotKey = entrySnapshot.getKey(); //etc. 0014 4F01 0000 76D3
                      spotReadings.child(spotKey).child("door").addValueEventListener(new ValueEventListener(){
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            int i = 0;
                            for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                                  Sensor sensor = postSnapshot.getValue(Sensor.class);
                                  
                                  //within 10 minutes
                                  if(System.currentTimeMillis() - sensor.getTimestamp() < 600000){
                                    doorSensors.put(i, true);
                                    System.out.println("doorSensors " + i);
                                  }
                                  i++;
                            }
                        }
                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            System.out.println("The read failed: " + firebaseError.getMessage());
                        }
                      });
                    }

                  }
                  
              }

              @Override
              public void onCancelled(FirebaseError firebaseError) {
                  System.out.println("The read failed: " + firebaseError.getMessage());
              }
          });
    }

    public void monitorIsLabEmpty() throws InterruptedException{
      scan();
      Thread.sleep(10000); //10 seconds - Make sure it has enough time to retrieve and analyze all data

      System.out.println("-----------------------------------------");
      System.out.println("Total SP: " + numOfSp);
      System.out.println("Total Inactive SP: " + numOfInactiveSp);
      System.out.println("motionSensors: " + motionSensors);
      System.out.println("soundSensors: " + soundSensors);
      System.out.println("doorSensors: " + doorSensors);

      //If all SPOTs with task "sp" have alive set to false
      Boolean allSpTrues = (numOfSp == numOfInactiveSp) ? true : false;

      //Check if there's new entries within 10 mins. 
      if(allSpTrues && allTrues(motionSensors) && allTrues(soundSensors) && allTrues(doorSensors)){
        Tweet tweetIsLabEmpty = new Tweet("Thread-lab-empty", "There's no one in the lab - " + currentTime());
        tweetIsLabEmpty.start();
      }else{
        System.out.println("# There's someone in the lab.");
      }
      
    }

    //Check if all the values in the map are true
    //@return - true/false
    public Boolean allTrues(Map<Integer, Boolean> map){

      //If the map is empty, that means there's no (motion, sound, door) entries within 10 mins
      if(map.isEmpty()){
        return true;
      }

      for(int i=0;i<map.size();i++){

        //FIX: where there's null value in the hashamp, replace null to 'true'
        if(map.get(i)==null){
          map.put(i, true);
        }

        //If there's new entries, that means there's motion, sound, door entries. Return false
        if(map.get(i)){
          return false;
        }

      }
      
      return true;
    }


    //Return the current time value in correct format.
    //@return - Formatted current date.
    public String currentTime(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    //Return the current time value in correct format.
    //@return - Formatted current date.
    public void clear(){
      motionSensors.clear();
      soundSensors.clear();
      doorSensors.clear();
      numOfSp = 0;
      numOfInactiveSp = 0;
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

    public void boil() throws InterruptedException{

        // final CountDownLatch done = new CountDownLatch(1);

        kettleBoilRef.setValue(new Boolean(true), new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                System.out.println("Boiling...");
                // done.countDown();
            }
        });
        // done.await();
    }

    public void brightUp() throws InterruptedException{

        // final CountDownLatch done = new CountDownLatch(1);

        lampRef.setValue(new String("white"), new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                System.out.println("Lamp on");
                // done.countDown();
            }
        });
        // done.await();
    }

    public void brightDown() throws InterruptedException{

        // final CountDownLatch done = new CountDownLatch(1);

        lampRef.setValue(new String("off"), new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                System.out.println("Lamp on");
                // done.countDown();
            }
        });
        // done.await();
    }

    public void monitorZone2(final String zoneName){
      com.firebase.client.Query queryRef = refZone2.limitToLast(1);

      queryRef.addChildEventListener(new ChildEventListener() {

          @Override
          public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
              Zone zone = snapshot.getValue(Zone.class);
              // System.out.println("Light " + zone.getLight());
              // System.out.println("Temp " + zone.getTemp());
              // System.out.println("AutoTimer " + autoTimer);

              //If latest temperature is more than 35
              if(zone.getTemp() > 35){
                Tweet tweetTemp = new Tweet("Thread-Temp-hot", "It's getting hot in " + zoneName + "! - " + zone.getTemp() + " degrees celsius");
                tweetTemp.start();
              }

              //If latest temperature less than 20
              if(zone.getTemp() < 20){
                Tweet tweetTemp2 = new Tweet("Thread-Temp-cold", "It's getting cold in " + zoneName + "!  - " + zone.getTemp() + " degrees celsius");
                tweetTemp2.start();
              }

              //If it's less than autoTimer -> Boil
              if(zone.getTemp() < autoTimer){
                System.out.println("AutoTimer: " + autoTimer);
                try{
                  boil();
                }catch(InterruptedException e){
                  System.out.println(e);
                }
              }

              //If light is greater than 100
              if(zone.getLight() > 100){
                try{
                  brightDown();
                  monitorZone2(zoneName);
                }catch(InterruptedException e){
                  System.out.println(e);
                }
                Tweet tweetLight = new Tweet("Thread-Light-bright", "So bright in " + zoneName + "! - " + zone.getLight() + " ");
                tweetLight.start();
              }

              //If light is less than 10
              if(zone.getLight() < 10){
                try{
                  brightUp();
                  monitorZone2(zoneName);
                }catch(InterruptedException e){
                  System.out.println(e);
                }
                Tweet tweetLight2 = new Tweet("Thread-Light-dark", "So dark in " + zoneName + "! Turn on the light? - " + zone.getLight() + " ");
                tweetLight2.start();

              }
          }

          @Override
          public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {}

          @Override
          public void onCancelled(FirebaseError firebaseError) {
              System.out.println("The read failed: " + firebaseError.getMessage());
          }

          @Override
          public void onChildRemoved(DataSnapshot snapshot) {}

          @Override
          public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {}
      });
    }

    public void monitorKettle(){
      // com.firebase.client.Query queryRef = refZone2.limitToLast(1);

      kettleTimer.addValueEventListener(new ValueEventListener() {

          @Override
          public void onDataChange(DataSnapshot snapshot) {
              Integer time = snapshot.getValue(Integer.class);
              autoTimer = time;
              System.out.println(autoTimer);
          }

          // @Override
          // public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {
          //   System.out.println("onChildMoved" + snapshot);
          // }

          @Override
          public void onCancelled(FirebaseError firebaseError) {
              System.out.println("The read failed: " + firebaseError.getMessage());
          }

          // @Override
          // public void onChildRemoved(DataSnapshot snapshot) {}

          // @Override
          // public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {
          //   System.out.println("onChildChanged" + snapshot);
          // }
      });
    }

}
