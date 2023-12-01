package lsfusion.gwt.client.form.design;

import lsfusion.gwt.client.base.size.GSize;
import lsfusion.gwt.client.base.jsni.NativeHashMap;
import lsfusion.gwt.client.base.view.GFlexAlignment;
import lsfusion.gwt.client.form.controller.GFormController;
import lsfusion.gwt.client.form.object.GGroupObjectValue;
import lsfusion.gwt.client.form.property.GPropertyReader;
import lsfusion.gwt.client.form.property.PValue;
import lsfusion.gwt.client.form.property.cell.classes.ColorDTO;
import lsfusion.gwt.client.form.property.cell.view.RendererType;

import java.io.Serializable;

public class GComponent implements Serializable {
    public int ID;
    public String sID;
    public GContainer container;
    public boolean defaultComponent;

    public String elementClass;
    public String elementAttr;

    public int width = -1;
    public int height = -1;
    
    public int span = 1;

    protected double flex = 0;
    protected GFlexAlignment alignment;
    public boolean shrink;
    public boolean alignShrink;
    public boolean shrinkOverflowVisible;
    public Boolean alignCaption;

    public ColorDTO background;
    public ColorDTO foreground;

    public String getBackground() {
        return background != null ? background.toString() : null;
    }

    public String getForeground() {
        return foreground != null ? foreground.toString() : null;
    }

    public GFont font;
    public GFont captionFont;

    public GSize getWidth() {
        int size = width;
        if(size == -2)
            return getDefaultWidth();
        if (size == -1 || size == -3)
            return null;
        return GSize.getComponentSize(size);
    }
    public GSize getHeight() {
        int size = height;
        if(size == -2)
            return getDefaultHeight();
        if (size == -1 || size == -3)
            return null;
        return GSize.getComponentSize(size);
    }

    protected GSize getDefaultWidth() {
        throw new UnsupportedOperationException();
    }

    protected GSize getDefaultHeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        String className = this.getClass().getName();
        className = className.substring(className.lastIndexOf('.') + 1);
        return className + "{" +
               "sID='" + sID + '\'' +
               ", defaultComponent=" + defaultComponent +
               '}';
    }

    public boolean isTab() {
        return container != null && container.tabbed;
    }

    public boolean isFlex() {
        return flex > 0;
    }
    public double getFlex(RendererType rendererType) {
        return flex;
    }

    public void setFlex(double flex) {
        this.flex = flex;
    }

    public GFlexAlignment getAlignment() {
        return alignment;
    }

    public boolean isShrink() {
        return shrink;
    }

    public boolean isAlignShrink() {
        return alignShrink;
    }

    public boolean isShrinkOverflowVisible() {
        return shrinkOverflowVisible;
    }

    public void setAlignment(GFlexAlignment alignment) {
        this.alignment = alignment;
    }

    public boolean isAlignCaption() {
        if(alignCaption != null)
            return alignCaption;

        return isDefautAlignCaption();
    }

    public boolean isDefautAlignCaption() {
        return false;
    }

    public int getSpan() {
        return span;
    }

    private class GShowIfReader implements GPropertyReader {
        private String sID;

        public GShowIfReader() {
        }

        @Override
        public void update(GFormController controller, NativeHashMap<GGroupObjectValue, PValue> values, boolean updateKeys) {
            controller.getFormLayout().setShowIfVisible(GComponent.this, !PValue.getBooleanValue(values.get(GGroupObjectValue.EMPTY)));
        }

        @Override
        public String getNativeSID() {
            if(sID == null) {
                sID = "_COMPONENT_" + "SHOWIFREADER" + "_" + GComponent.this.sID;
            }
            return sID;
        }
    }
    public final GPropertyReader showIfReader = new GShowIfReader();

    private class GElementClassAttrReader implements GPropertyReader {
        private String sID;

        @SuppressWarnings("unused")
        public GElementClassAttrReader() {
        }

        private boolean attr;
        public GElementClassAttrReader(boolean attr) {
            this.attr = attr;
        }

        @Override
        public void update(GFormController controller, NativeHashMap<GGroupObjectValue, PValue> values, boolean updateKeys) {
            if(attr) {
                controller.getFormLayout().setElementAttr(GComponent.this, PValue.getClassStringValue(values.get(GGroupObjectValue.EMPTY)));
            } else {
                controller.getFormLayout().setElementClass(GComponent.this, PValue.getClassStringValue(values.get(GGroupObjectValue.EMPTY)));
            }
        }

        @Override
        public String getNativeSID() {
            if(sID == null) {
                sID = "_COMPONENT_" + (attr ? "ELEMENTATTRREADER" : "ELEMENTCLASSREADER") + "_" + GComponent.this.sID;
            }
            return sID;
        }
    }

    public final GPropertyReader elementClassReader = new GElementClassAttrReader(false);
    public final GPropertyReader elementAttrReader = new GElementClassAttrReader(true);
}