package device;

import java.io.*;
import java.util.*;

public class Util {

    private static String configFile;

    static {
        try {
            configFile = new File(".").getCanonicalPath().concat("/config/settings.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String m_printMessage = "";
    public static void printMessage( String msg ){
        m_printMessage=msg;
        System.out.println(msg);

    }
    public static void print(String msg){
        System.out.print(msg);
    }

    public static String getPrintMessage(){
        return m_printMessage;
    }

    public static void printSettings()  {

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(configFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String key : prop.stringPropertyNames()) {
            String value = prop.getProperty(key);
            System.out.println("setting " + key + " is: " + value);
        }
    }

    private static String parseSetting(String setting) {

        Properties prop = new Properties();
        try {
            InputStream in = new FileInputStream(configFile);
            prop.load(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return prop.getProperty(setting);
    }

    public static String getSchemaDevice(){
        return parseSetting("schemadevice");
    }
    public static String getDevice(){
        return parseSetting("deviceconfig");
    }

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo( o2.getValue() );
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
