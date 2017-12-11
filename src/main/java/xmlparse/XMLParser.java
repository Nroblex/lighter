package xmlparse;

import device.Device;
import device.SchemaDevice;
import device.Util;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class XMLParser {
    private static String DEVICE_PATH="/devices/device";
    static Logger iLog = LogManager.getLogger(XMLParser.class);

    private static HashMap<Integer, String> deviceNameById = null;

    public static Map<Integer, Device> getScheduledDevicesLaterThanNowXML() {
        Map<Integer, Device> sortedDeviceSchemas = null;

        sortedDeviceSchemas = xmlFilesToObject();
        return sortedDeviceSchemas;
    }

    private static Map<Integer, Device> xmlFilesToObject(){

        File schemaDeviceFile = new File(Util.getSchemaDevice());
        //File deviceFile = new File("db/Device.xml");
        File deviceFile = new File(Util.getDevice());

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;

        DateTime timePoint=null;
        String action =null;

        Integer dayOfWeek = DateTime.now().getDayOfWeek();

        String deviceName = null;
        String id = null;

        SchemaDevice dbSchemaDevice=null;
        Device device=null;

        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");


        Map<Integer, Device> deviceMap = new HashMap<Integer, Device>();

        int actualDeviceId = 0;

        try {


            dBuilder = dbFactory.newDocumentBuilder();

            Document docDeviceConfig = dBuilder.parse(deviceFile);
            Document docSchemaConfig = dBuilder.parse(schemaDeviceFile);

            XPath xPathDevice = XPathFactory.newInstance().newXPath();
            XPath xPathSchema = XPathFactory.newInstance().newXPath();

            ///*/item[id/@isInStock='true']/category/text()
            // String sql = "/devices[device/@weekday='torsdag']";

            NodeList deviceList = (NodeList) xPathDevice.compile("/Devices/DeviceConfig").evaluate(docDeviceConfig, XPathConstants.NODESET);
            NodeList schemaList = (NodeList) xPathSchema.compile(DEVICE_PATH).evaluate(docSchemaConfig, XPathConstants.NODESET);


            for (int n = 0; n <deviceList.getLength(); n++){

                List<SchemaDevice> schemaDeviceList = new ArrayList<>();
                Node deviceNode = deviceList.item(n);

                if (deviceNode.getNodeType() == Node.ELEMENT_NODE){
                    //Vi har hittat ett deviceName

                    actualDeviceId=Integer.parseInt(xPathDevice.compile("./DeviceId").evaluate(deviceNode));

                    device = new Device();

                    device.setName(xPathDevice.compile("./DeviceName").evaluate(deviceNode));
                    device.setId( actualDeviceId );
                    device.setComment(xPathDevice.compile("./Comment").evaluate(deviceNode));

                    String deviceId=null;
                    String weekDay = null;
                    //Nu måste vi hitta ALLA dessa konfigurationer i schemadevice.xml

                    for (int nCount=0; nCount < schemaList.getLength(); nCount++){

                        Node schemaNode = schemaList.item(nCount);

                        if (schemaNode.getNodeType() == Node.ELEMENT_NODE){
                            if (schemaNode.getNodeName().compareTo("device")==0) {

                                if (schemaNode.hasAttributes()){
                                    NamedNodeMap namedNodeMap=schemaNode.getAttributes();
                                    for (int p = 0; p<namedNodeMap.getLength(); p++){
                                        if (namedNodeMap.item(p).getNodeName().compareTo("deviceId") == 0)
                                            deviceId = namedNodeMap.item(p).getNodeValue(); //
                                        else
                                            weekDay =  namedNodeMap.item(p).getNodeValue().toLowerCase();


                                        if (weekDay==null || deviceId == null) continue;

                                        if (Integer.parseInt(deviceId) == actualDeviceId) {
                                            //Vi har hittat vad som hör ihop med detta deviceId.
                                            SchemaDevice schemaDevice = new SchemaDevice();

                                            String dtTimePoint =  xPathSchema.compile("./timepoint").evaluate(schemaNode);

                                            if (dtTimePoint.length() == 5)
                                                dtTimePoint = dtTimePoint.concat(":00");


                                            timePoint = formatter.parseDateTime(dtTimePoint);
                                            action = xPathSchema.compile("./action").evaluate(schemaNode);
                                            id = xPathSchema.compile("./id").evaluate(schemaNode);

                                            if (WeekDays.valueOf(weekDay).getWeekDay() == dayOfWeek) {
                                                //Är tiden senare än nu ?
                                                if (timePoint.toLocalTime().getMillisOfDay() > DateTime.now().getMillisOfDay()) {

                                                    dbSchemaDevice = new SchemaDevice().setDeviceID(Integer.parseInt(deviceId))
                                                            .setTimePoint(timePoint)
                                                            .setAction(action);

                                                    //deviceMap.put(Integer.parseInt(id), dbSchemaDevice);
                                                    schemaDeviceList.add(dbSchemaDevice);
                                                    weekDay=null; deviceId=null;

                                                }
                                            }

                                        }

                                    }
                                }
                            }
                        }
                    }

                    //if (schemaDeviceList.size() > 0)
                    device.setSchemaDevices(schemaDeviceList);

                    Collections.sort(schemaDeviceList, new Comparator<SchemaDevice>() {
                        @Override
                        public int compare(SchemaDevice o1, SchemaDevice o2) {
                            return o1.getTimePoint().compareTo(o2.getTimePoint());
                        }
                    });

                    deviceMap.put(actualDeviceId, device);

                    //Sort this?

                }


            }

        } catch (ParserConfigurationException e) {
            iLog.error(e);
        } catch (SAXException e) {
            iLog.error(e);
        } catch (IOException e) {
            iLog.error(e);
        } catch (XPathExpressionException e) {
            iLog.error(e);
        }


        return deviceMap;

    }

    //Läser in deviceconfig till hashmap.
    public static void readXMLDeviceConfig(){
        String deviceName = "";
        String deviceId = "";

        //File xFile = new File(Util.getSetting("deviceconfig"));
        File xFile = new File("db/Device.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;

        deviceNameById = null;
        deviceNameById = new HashMap<Integer, String>();

        try {

            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xFile);
            XPath xPath = XPathFactory.newInstance().newXPath();

            NodeList deviceList = (NodeList) xPath.compile(DEVICE_PATH).evaluate(doc, XPathConstants.NODESET);

            for (int nCount = 0; nCount<deviceList.getLength(); nCount++) {
                Node deviceNode = deviceList.item(nCount);

                if (deviceNode.getNodeType() == Node.ELEMENT_NODE) {

                    if (deviceNode.getNodeName().compareTo("device") == 0) {
                        deviceName =  xPath.compile("./name").evaluate(deviceNode);
                        deviceId = xPath.compile("./id").evaluate(deviceNode);

                        deviceNameById.put(Integer.parseInt(deviceId), deviceName);

                    }
                }

            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }


    }

    private static String getDeviceNameFromDeviceId(Integer deviceId) {
        return deviceNameById.get(deviceId);
    }
}
