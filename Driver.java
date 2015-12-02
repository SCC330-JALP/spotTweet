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
                System.out.println("Monitoring Lab Activities");
                System.out.println("-------------------------");
                infolab.monitorActivities();
                Thread.sleep(1000);
                

                System.out.println("Monitoring Batteries");
                System.out.println("-------------------------");
                infolab.monitorBatteries();
                Thread.sleep(1000);                

                System.out.println("Monitoring Zone 1");
                System.out.println("-------------------------");
                infolab.monitorZone("zone1");
                Thread.sleep(1000);
                

                System.out.println("Monitoring Zone 2");
                System.out.println("-------------------------");
                infolab.monitorZone("zone2");
                Thread.sleep(1000);
                

                System.out.println("Monitoring Zone 3");
                System.out.println("-------------------------");
                infolab.monitorZone("zone3");
                Thread.sleep(1000);


                System.out.println("Monitoring if the lab is empty");
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