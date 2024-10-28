package lsfusion.gwt.client.classes.data.link;

import lsfusion.gwt.client.ClientMessages;
import lsfusion.gwt.client.form.property.GPropertyDraw;
import lsfusion.gwt.client.form.property.cell.classes.view.link.ImageLinkCellRenderer;
import lsfusion.gwt.client.form.property.cell.view.CellRenderer;

public class GImageLinkType extends GRenderedLinkType {
    @Override
    public String getExtension() {
        return "jpg";
    }

    @Override
    public String toString() {
        return ClientMessages.Instance.get().typeImageLinkCaption();
    }
}