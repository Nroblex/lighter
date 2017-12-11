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

    private Map<Integer, Device> dbConfiguredDevices = new HashMap<Integer, Device>();

    public Scheduler() {

        iLog.info("Starting Scheduler");
        configTimer.scheduleAtFixedRate(timerTaskReadConfiguration, 0, 10000); //read every tenth second.
        executeTimer.scheduleAtFixedRate(timerTaskcheckIfExecute, 0, 1000);

        iLog.info("Timers were started!");

        if (dbConfiguredDevices.size() > 0){
            iLog.info("There are " + dbConfiguredDevices.size() + " configured devices.");
        }


    }

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
            } catch (InvocationTargetException e) {
                iLog.error(e);
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

    private void executeIfOnTime() {
        Iterator iterator = dbConfiguredDevices.entrySet().iterator();

        while (iterator.hasNext()) {
            //Map.Entry<Integer, SchemaDevice> actualDevice = (Map.Entry) iterator.next();
            Map.Entry<Integer, Device> actualDevice = (Map.Entry)iterator.next();
            for (SchemaDevice schemaDevice : actualDevice.getValue().getSchemaDevices()) {
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
                iLog.info("line: " + s);
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

            for (SchemaDevice schemaDevice : actualDevice.getValue().getSchemaDevices()) {

                Integer secDiff = schemaDevice.getTimePoint().getSecondOfDay() - DateTime.now().getSecondOfDay();

                if (secDiff < 60) {

                    Util.printMessage(String.format("Device {%s} to be executed {%s} in %s secs",
                            String.valueOf(actualDevice.getValue().getName()),
                            schemaDevice.getAction(),
                            String.valueOf(secDiff)));
                } else {

                    Integer mins = schemaDevice.getTimePoint().getMinuteOfDay() - DateTime.now().getMinuteOfDay();


                    Util.printMessage(String.format("Device {%s} to be executed {%s} in %s mins timepoint will be = {%s}",
                            String.valueOf(actualDevice.getValue().getName()),
                            schemaDevice.getAction(),
                            //String.valueOf(hours),
                            String.valueOf(mins),
                            schemaDevice.getTimePoint().toLocalTime().toString()));

                    iLog.debug(Util.getPrintMessage());

                }

            }

        }

        if (valueExists)
            System.out.println("\n");
    }
}