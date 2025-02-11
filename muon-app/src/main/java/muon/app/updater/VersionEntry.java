package muon.app.updater;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VersionEntry implements Comparable<VersionEntry> {
    @JsonProperty("tag_name")
    private String tagName;
    private int value;

    public VersionEntry() {
        // TODO Auto-generated constructor stub
    }

    public VersionEntry(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public int compareTo(VersionEntry o) {
        int v1 = getNumericValue();
        int v2 = o.getNumericValue();
        return v1 - v2;
    }

    public final int getNumericValue() {
        String[] arr = tagName.substring(1).split("\\.");
        int value = 0;
        int multiplier = 1;
        for (int i = arr.length - 1; i >= 0; i--) {
            value += Integer.parseInt(arr[i]) * multiplier;
            multiplier *= 10;
        }
        return value;
    }

    @Override
    public String toString() {
        return "VersionEntry [tag_name=" + tagName + " value=" + getNumericValue() + "]";
    }
}
