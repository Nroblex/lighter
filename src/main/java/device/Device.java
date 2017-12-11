package device;

import java.util.List;

public class Device {
    private int Id;
    private String name;
    private String comment;
    private List<SchemaDevice> schemaDevices;


    public List<SchemaDevice> getSchemaDevices() {
        return schemaDevices;
    }

    public void setSchemaDevices(List<SchemaDevice> schemaDevices) {
        this.schemaDevices = schemaDevices;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
