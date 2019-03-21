package lsfusion.gwt.client.form.property.cell.classes.controller;

import lsfusion.gwt.shared.form.property.GPropertyDraw;
import lsfusion.gwt.shared.classes.data.GIntegerType;
import lsfusion.gwt.client.form.property.cell.controller.EditManager;

public class IntegerGridCellEditor extends IntegralGridCellEditor {
    public IntegerGridCellEditor(EditManager editManager, GPropertyDraw property) {
        super(GIntegerType.instance, editManager, property);
    }
}
