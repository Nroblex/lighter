import device.Util;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;
import java.io.IOException;

public class ProgramLighter {

    static Logger iLog = LogManager.getLogger(ProgramLighter.class);

    public static void main(String[] args) {
        System.out.println("Starting lighter app...");

        setupLog4J();

        iLog.info("Log-System configured.");

        if (args.length == 0){
            printInfo();
            return;
        }

        if (args[0].compareTo("-c") == 0){
            printConfig();
            return;

        } else if (args[0].compareTo("-r") == 0){
            System.out.println("Startar!");
            iLog.info("Entering runmode r.");
            new Scheduler(false).startScheduler();

        } else if (args[0].compareTo("-x") == 0) {
            //Running random...
            System.out.println("Enter configured device to run test on: ");
            String testDevice = System.console().readLine();

            try{
                Integer.parseInt(testDevice);
            } catch (NumberFormatException e){
                System.out.println(e.getMessage());
                return;
            }

            Scheduler scheduler = new Scheduler(true);
            scheduler.setDeviceToRun(Integer.parseInt(testDevice));
            scheduler.startScheduler();


        }
        else {
            printInfo();
        }




    }

    private static void runRandom() {



    }

    private static void printConfig() {
        Util.printMessage("Configuration!");
        Util.printSettings();

        try {
            System.out.println(String.format("CnonicalPath = %s", new File(".").getCanonicalPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void printInfo() {
        Util.printMessage("Programmet ges argument");
        Util.printMessage("-c visa config");
        Util.printMessage("-x random...");
        Util.printMessage("-r = run");
    }

    static void setupLog4J(){

        try {

            String logPath = new File(".").getCanonicalPath().concat("/config/log4j.xml");
            DOMConfigurator.configure(logPath);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
