package muon.app.ui.components.session.utilpage.services;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceEntry {
    private String name;
    private String unitStatus;
    private String desc;
    private String unitFileStatus;

    public ServiceEntry(String name, String unitStatus, String desc, String unitFileStatus) {
        this.name = name;
        this.unitStatus = unitStatus;
        this.desc = desc;
        this.unitFileStatus = unitFileStatus;
    }

    public ServiceEntry() {
    }

    @Override
    public String toString() {
        return "ServiceEntry{" +
                "name='" + name + '\'' +
                ", unitStatus='" + unitStatus + '\'' +
                ", desc='" + desc + '\'' +
                ", unitFileStatus='" + unitFileStatus + '\'' +
                '}';
    }
}
