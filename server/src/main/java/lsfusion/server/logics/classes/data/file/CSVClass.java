package lsfusion.server.logics.classes.data.file;

import lsfusion.base.file.RawFileData;
import lsfusion.interop.classes.DataType;
import lsfusion.server.logics.classes.data.DataClass;
import lsfusion.server.logics.form.stat.struct.FormIntegrationType;

import java.util.ArrayList;
import java.util.Collection;

public class CSVClass extends HumanReadableFileClass {

    protected String getFileSID() {
        return "CSVFILE";
    }

    private static Collection<CSVClass> instances = new ArrayList<>();

    public static CSVClass get() {
        return get(false, false);
    }
    
    public static CSVClass get(boolean multiple, boolean storeName) {
        for (CSVClass instance : instances)
            if (instance.multiple == multiple && instance.storeName == storeName)
                return instance;

        CSVClass instance = new CSVClass(multiple, storeName);
        instances.add(instance);
        DataClass.storeClass(instance);
        return instance;
    }

    private CSVClass(boolean multiple, boolean storeName) {
        super(multiple, storeName);
    }

    public byte getTypeID() {
        return DataType.CSV;
    }

    @Override
    public String getExtension() {
        return "csv";
    }

    @Override
    public FormIntegrationType getIntegrationType() {
        return FormIntegrationType.CSV;
    }
}