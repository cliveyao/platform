package lsfusion.gwt.client.base;

import com.google.gwt.user.client.rpc.IsSerializable;
import lsfusion.gwt.client.GFormChanges;
import lsfusion.gwt.client.form.object.GGroupObjectValue;

import java.io.Serializable;

public class GAsync implements IsSerializable, Serializable {
    public Serializable displayString; // String or GStringWithFiles
    public String rawString;

    public GGroupObjectValue key;

    public static final GAsync RECHECK = new GAsync("RECHECK", "RECHECK", null);
    public static final GAsync CANCELED = new GAsync("CANCELED", "CANCELED", null);

    public GAsync() {
    }

    public GAsync(Serializable displayString, String rawString, GGroupObjectValue key) {
        this.displayString = displayString;
        this.rawString = rawString;

        this.key = key;
    }

    public String getDisplayString() {
        return (String) GFormChanges.remapValue(displayString);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof GAsync && displayString.equals(((GAsync) o).displayString) && rawString.equals(((GAsync) o).rawString) && GwtClientUtils.nullEquals(key, ((GAsync) o).key);
    }

    @Override
    public int hashCode() {
        return 31 * (displayString.hashCode() * 31 + rawString.hashCode()) + GwtClientUtils.nullHash(key);
    }
}
