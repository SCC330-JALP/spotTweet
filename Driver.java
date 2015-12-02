import com.firebase.client.*;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.*;
import java.text.*;


public class Driver {

        public static void main(String[] args) throws InterruptedException, TwitterException {
            
            SPOTDBcommunication infolab = new SPOTDBcommunication();
            
            while(true){
                infolab.monitorActivities();
                infolab.monitorBatteries();
                infolab.monitorIsLabEmpty();
                infolab.monitorZone("zone1");
                infolab.monitorZone("zone2");
                infolab.monitorZone("zone3");

                Thread.sleep(180000); //3 mins
            }
            
    }

}