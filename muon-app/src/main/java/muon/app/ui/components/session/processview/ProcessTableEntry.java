package muon.app.ui.components.session.processview;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProcessTableEntry {
    private String name;
    private String user;
    private String time;
    private String tty;
    private String args;
    private float cpu;
    private float memory;
    private int pid;
    private int ppid;
    private int nice;

}
