package lsfusion.server.language.proxy;

import lsfusion.server.logics.form.interactive.design.object.TreeGroupView;

public class TreeGroupViewProxy extends ComponentViewProxy<TreeGroupView> {
    public TreeGroupViewProxy(TreeGroupView target) {
        super(target);
    }

    public void setAutoSize(boolean autoSize) {
        target.width = autoSize ? -1 : -2;
        target.height = autoSize ? -1 : -2;
    }

    public void setBoxed(boolean boxed) {
        target.boxed = boxed;
    }

    public void setExpandOnClick(boolean expandOnClick) {
        target.expandOnClick = expandOnClick;
    }

    public void setHeaderHeight(int headerHeight) {
        target.headerHeight = headerHeight;
    }

    public void setResizeOverflow(boolean resizeOverflow) {
        target.resizeOverflow = resizeOverflow;
    }

    public void setLineHeight(int lines) {
        target.lineHeight = lines;
    }

    public void setLineWidth(int lines) {
        target.lineWidth = lines;
    }
}
