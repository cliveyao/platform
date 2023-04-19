package lsfusion.client.form.property.cell;

import com.google.common.base.Throwables;
import lsfusion.base.BaseUtils;
import lsfusion.client.form.ClientForm;
import lsfusion.client.form.object.ClientGroupObjectValue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;

public class ClientAsync implements Serializable {
    public final Serializable displayValue;
    public final Serializable rawValue;

    public final ClientGroupObjectValue key;

    public static final ClientAsync RECHECK = new ClientAsync("RECHECK", "RECHECK", null);
    public static final ClientAsync CANCELED = new ClientAsync("CANCELED", "CANCELED", null);

    private ClientAsync(String displayValue, String rawValue, ClientGroupObjectValue key) {
        this.displayValue = displayValue;
        this.rawValue = rawValue;
        this.key = key;
    }

    public String getReplacementString() {
        return rawValue.toString();
    }

    public ClientAsync(DataInputStream inStream, ClientForm form) throws IOException {
        displayValue = (Serializable) BaseUtils.deserializeObject(inStream);
        rawValue = (Serializable) BaseUtils.deserializeObject(inStream);
        if(inStream.readBoolean())
            key = new ClientGroupObjectValue(inStream, form);
        else
            key = null;
    }

    public static ClientAsync[] deserialize(byte[] asyncs, ClientForm form) {
        if(asyncs == null)
            return null;

        DataInputStream inStream = new DataInputStream(new ByteArrayInputStream(asyncs));
        try {
            ClientAsync[] result = new ClientAsync[inStream.readInt()];
            for(int i=0;i<result.length;i++)
                result[i] = new ClientAsync(inStream, form);
            return result;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof ClientAsync && displayValue.equals(((ClientAsync) o).displayValue) && rawValue.equals(((ClientAsync) o).rawValue) && BaseUtils.nullEquals(key, ((ClientAsync) o).key);
    }

    @Override
    public int hashCode() {
        return 31 * (displayValue.hashCode() * 31 + rawValue.hashCode()) + BaseUtils.nullHash(key);
    }

    @Override
    public String toString() {
        return "<html>" + displayValue + "</html>";
    }
}
