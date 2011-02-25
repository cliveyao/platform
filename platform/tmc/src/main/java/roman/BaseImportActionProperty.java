package roman;

import platform.interop.ClassViewType;
import platform.server.classes.DataClass;
import platform.server.classes.FileActionClass;
import platform.server.classes.ValueClass;
import platform.server.form.entity.FormEntity;
import platform.server.form.entity.PropertyDrawEntity;
import platform.server.logics.property.ActionProperty;
import platform.server.logics.property.ClassPropertyInterface;

import java.util.Iterator;

/**
 * User: DAle
 * Date: 25.02.11
 * Time: 15:40
 */

public abstract class BaseImportActionProperty extends ActionProperty {
    protected final ClassPropertyInterface supplierInterface;
    protected String extensions;
    protected RomanBusinessLogics BL;

    public BaseImportActionProperty(RomanBusinessLogics BL, String caption, ValueClass supplierClass) {
        this(BL, caption, supplierClass, "xls");
    }

    public BaseImportActionProperty(RomanBusinessLogics BL, String caption, ValueClass supplierClass, String extensions) {
        super(BL.genSID(), caption, new ValueClass[]{supplierClass});

        Iterator<ClassPropertyInterface> i = interfaces.iterator();
        supplierInterface = i.next();
        this.extensions = extensions;
        this.BL = BL;
    }

    @Override
    public void proceedDefaultDraw(PropertyDrawEntity<ClassPropertyInterface> entity, FormEntity form) {
        super.proceedDefaultDraw(entity, form);
        entity.shouldBeLast = true;
        entity.forceViewType = ClassViewType.PANEL;
    }

    @Override
    protected DataClass getValueClass() {
        return FileActionClass.getInstance("Файлы таблиц", extensions);
    }
}
