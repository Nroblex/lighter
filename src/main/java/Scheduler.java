import device.Device;
import device.SchemaDevice;
import device.Util;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import xmlparse.XMLParser;

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Scheduler {

    private static Logger iLog = LogManager.getLogger(Scheduler.class);

    private Timer configTimer = new Timer("Read Config");
    private Timer executeTimer = new Timer("Execute Lighter");
    private Timer randomTimer = new Timer("Random Executer");

    private SchemaDevice randomDevice = new SchemaDevice();

    private Map<Integer, Device> dbConfiguredDevices = new HashMap<Integer, Device>();

    private int deviceToRun = -1;
    private boolean isTestRunDevice = false;

    public Scheduler(boolean testRunDevice) {
        isTestRunDevice=testRunDevice;
    }


    public void startScheduler(){

        if (!isTestRunDevice){

            iLog.info("Starting Scheduler");

            //Read complete database-file directly
            dbConfiguredDevices = XMLParser.getScheduledDevicesLaterThanNowXML();

            configTimer.scheduleAtFixedRate(timerTaskReadConfiguration, 60000, 60000); //Wait one minute, then read every minute
            executeTimer.scheduleAtFixedRate(timerTaskcheckIfExecute, 1000, 1000); // wait ten seconds then read every half second

            iLog.info("Timers were started!");

            if (dbConfiguredDevices.size() > 0){
                iLog.info("There are " + dbConfiguredDevices.size() + " configured devices.");
            }


        } else { //Running specified device...

            randomDevice.setDeviceID(deviceToRun);
            randomTimer.scheduleAtFixedRate(randomExecuter, 1000, 2000); //Every fifth second.

        }

    }


    public void setTestDevcieId(int id){
        deviceToRun=id;
    }

    TimerTask randomExecuter = new TimerTask() {
        @Override
        public void run() {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {

                        String action = "";

                        Util.printMessage("Executing hardcoded device > " + deviceToRun);

                        if (randomDevice.getAction() == null){
                            randomDevice.setAction("ON");
                        }

                        if (randomDevice.getAction().compareTo("ON") == 0){
                            action = "-on";
                            randomDevice.setAction("OFF");
                        } else {
                            action = "-of";
                            randomDevice.setAction("ON");
                        }


                        Util.printMessage("Executing, mode = " + action);
                        executeLighter(randomDevice);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    };

    TimerTask timerTaskReadConfiguration = new TimerTask() {

        @Override
        public void run() {

            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        dbConfiguredDevices = XMLParser.getScheduledDevicesLaterThanNowXML();
                        if (dbConfiguredDevices.size() > 0 && dbConfiguredDevices != null) {
                            logInformation();
                        }
                    }

                });
            } catch (InterruptedException e) {
                iLog.error(e);
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                iLog.error(e);
                e.printStackTrace();
            }

        }
    };

    TimerTask timerTaskcheckIfExecute = new TimerTask() {
        @Override
        public void run() {
            //Execute this shit every second!
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        executeIfOnTime();
                    }
                });
            } catch (InterruptedException e) {
                iLog.error(e);
            } catch (InvocationTargetException e) {
                iLog.error(e);
            }

        }
    };



    private synchronized void executeIfOnTime() {

        Iterator iterator = dbConfiguredDevices.entrySet().iterator();

        while (iterator.hasNext()) {
            //Map.Entry<Integer, SchemaDevice> actualDevice = (Map.Entry) iterator.next();
            Map.Entry<Integer, Device> actualDevice = (Map.Entry)iterator.next();

            if (actualDevice.getValue().getSchemaDevices()  == null)
                continue;


            /*
            List<SchemaDevice> schemaDevices = actualDevice.getValue().getSchemaDevices();

            SchemaDevice deviceToExecute
                    = schemaDevices
                    .stream()
                    .filter(p -> p.getTimePoint().getSecondOfDay() == DateTime.now().getSecondOfDay())
                    .findFirst()
                    .get();

            if (deviceToExecute != null){
                System.out.println("Executing for device = " + deviceToExecute.getDeviceID());
            }
            */



            for (SchemaDevice schemaDevice : actualDevice.getValue().getSchemaDevices()) {

                //System.out.println("executeIfOnTime: " +
                //        schemaDevice.getTimePoint().getSecondOfDay() + " timsecondNow = " +
                //        DateTime.now().getSecondOfDay());

                //Integer min = schemaDevice.getTimePoint().getMinuteOfDay();

                long diffTime = schemaDevice.getTimePoint().getMinuteOfDay() - DateTime.now().getMinuteOfDay();
                Util.printMessage("Device = " + schemaDevice.getDeviceID() + " TimePoint = " + schemaDevice.getTimePoint().toLocalTime().toString() + " wait-minutes, to exec. = " + diffTime);


                if (schemaDevice.getTimePoint().getSecondOfDay() == DateTime.now().getSecondOfDay()){

                    iLog.info("====EXECUTING==== for device = > " + actualDevice.getValue().getName());
                    if (schemaDevice.getTimePoint().getSecondOfDay() == DateTime.now().getSecondOfDay()) {
                        executeLighter(schemaDevice);
                    }
                    iLog.info("====END EXECUTING====");
                }
            }

        }
    }

    //This must now call telldus via java
    private void executeLighter(SchemaDevice schemaDevice) {

        iLog.info("Executing lighter, SchemaDevice => " + schemaDevice.getID());
        System.out.println("Lighter is executing system command!");

        String action = "";
        if (schemaDevice.getAction().compareTo("ON") == 0){
            action = "-on";
        } else
            action = "-of";

        action = action.concat( " ".concat(schemaDevice.getDeviceID().toString()));

        String s ;
        Process proc;
        try{

            iLog.info(String.format(("Trying to send shellcommand %s for deviceId = %s"), action, schemaDevice.getDeviceID()));
            System.out.println(String.format(("Trying to send shellcommand %s for deviceId = %s"), action, schemaDevice.getDeviceID()));

            proc = Runtime.getRuntime().exec("tdtool ".concat(action));

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()));
            while ((s = br.readLine()) != null) {
                System.out.println("line: " + s);
            }
            proc.waitFor();
            System.out.println ("exit: " + proc.exitValue());
            proc.destroy();

        } catch (Exception ep){
            iLog.error(ep);
        }


    }

    private void logInformation() {

        //Util.printMessage(String.format("Det finns %s konfigurerade tider.", String.valueOf(dbConfiguredDevices.size())));

        //dbConfiguredDevices.forEach((k, v)->System.out.println("Key = " + k + " Value = " + v);
        int idPreviousDeviceId = 0;
        boolean valueExists = false;

        try {
            final String osName = System.getProperty("os.name");
            if (osName.toLowerCase().contains("linux")) {
                Runtime.getRuntime().exec("clear");
            }

        } catch (IOException e) {
            iLog.error(e);
        }

        Iterator iterator = dbConfiguredDevices.entrySet().iterator();

        while (iterator.hasNext()) {
            valueExists=true;
            Map.Entry<Integer, Device> actualDevice = (Map.Entry) iterator.next();

            if (actualDevice.getValue().getSchemaDevices() == null)
                continue;

            for (SchemaDevice schemaDevice : actualDevice.getValue().getSchemaDevices()) {

                Integer secDiff = schemaDevice.getTimePoint().getSecondOfDay() - DateTime.now().getSecondOfDay();

                if (idPreviousDeviceId == 0) {
                    idPreviousDeviceId = schemaDevice.getDeviceID();
                }
                if (idPreviousDeviceId != schemaDevice.getDeviceID()){
                    idPreviousDeviceId=0;
                    System.out.println("\n");
                }
                if (secDiff < 60) {

                    Util.printMessage(String.format("Just a seconds left for: {%s} to be executed {%s} in %s secs",
                            String.valueOf(actualDevice.getValue().getName()),
                            schemaDevice.getAction(),
                            String.valueOf(secDiff)));
                    System.out.println("\n");

                } else {

                    Integer mins = schemaDevice.getTimePoint().getMinuteOfDay() - DateTime.now().getMinuteOfDay();

                    Util.printMessage(String.format("Device {%s} to be executed {%s} in %s mins timepoint will be = {%s}",
                            String.valueOf(actualDevice.getValue().getName()),
                            schemaDevice.getAction(),
                            //String.valueOf(hours),
                            String.valueOf(mins),
                            schemaDevice.getTimePoint().toLocalTime().toString()));


                }

            }

        }

        if (valueExists)
            System.out.println("\n");
    }

    public void setDeviceToRun(int i) {
        deviceToRun = i;
    }
}