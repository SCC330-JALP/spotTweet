import com.firebase.client.*;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.*;
import java.text.*;


public class Driver {

        public static void main(String[] args) throws InterruptedException, TwitterException {
            
            SPOTDBcommunication infolab = new SPOTDBcommunication();
            
            while(true){
                System.out.println("-------------------------");
                System.out.println("Monitoring... ");
                System.out.println("- Lab Activities");
                System.out.println("  - Motion");
                System.out.println("  - Door sensor");
                System.out.println("  - First person entered");
                infolab.monitorActivities();
                
                System.out.println("- Sensor Batteries");
                infolab.monitorBatteries();     
                
                System.out.println("- Zone 1 temperature and light");
                infolab.monitorZone("zone1");
                
                System.out.println("- Zone 2 temperature and light");
                infolab.monitorZone("zone2");   
                
                System.out.println("- Zone 3 temperature and light");
                infolab.monitorZone("zone3");


                System.out.println("- If lab is empty");
                System.out.println("-------------------------");
                infolab.monitorIsLabEmpty();
                Thread.sleep(3000);

                System.out.println("-------------------------");
                System.out.println("Clearing Hashmaps");
                infolab.clear();
                Thread.sleep(1000);

                System.out.println("-------------------------");
                System.out.println("Completed @ " + infolab.currentTime());
                System.out.println("Next scan in 2 minutes...");

                Thread.sleep(120000); //2 mins
            }
            
    }

}