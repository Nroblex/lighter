public class ReadTelldus {
    private static ReadTelldus ourInstance = new ReadTelldus();

    public static ReadTelldus getInstance() {
        return ourInstance;
    }

    private ReadTelldus() {

        executeExternCommand();

    }

    private void executeExternCommand() {

        String commandLine = "tdtool -l";

    }
}
