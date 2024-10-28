package lsfusion.server.logics.classes.data.time;

import lsfusion.interop.classes.DataType;
import lsfusion.server.logics.classes.data.DataClass;
import lsfusion.server.logics.classes.data.integral.IntegerClass;
import lsfusion.server.logics.form.interactive.controller.remote.serialization.FormInstanceContext;
import lsfusion.server.physics.dev.i18n.LocalizedString;

import java.util.Calendar;

public class YearClass extends IntegerClass {

    public final static YearClass instance = new YearClass();

    static {
        DataClass.storeClass(instance);
    }

    private YearClass() { caption = LocalizedString.create("{classes.year}"); }

    @Override
    public Integer getDefaultValue() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    @Override
    public byte getTypeID() {
        return DataType.YEAR;
    }

    public String getSID() {
        return "YEAR";
    }

    @Override
    public String getInputType(FormInstanceContext context) {
        return "year";
    }
}
