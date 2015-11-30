import com.firebase.client.*;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.*;
import java.text.*;


public class Driver {

        public static void main(String[] args) throws InterruptedException, TwitterException {
            
            SPOTDBcommunication spotSettings = new SPOTDBcommunication("spotSettings");
            
            while(true){
                spotSettings.monitor();
                Thread.sleep(5000);
            }
            
    }

}