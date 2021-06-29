package lsfusion.gwt.client.form.filter.user.view;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import lsfusion.gwt.client.ClientMessages;
import lsfusion.gwt.client.base.GwtClientUtils;
import lsfusion.gwt.client.base.Pair;
import lsfusion.gwt.client.base.view.FlexPanel;
import lsfusion.gwt.client.form.filter.user.GCompare;
import lsfusion.gwt.client.form.filter.user.GPropertyFilter;
import lsfusion.gwt.client.form.object.table.controller.GTableController;
import lsfusion.gwt.client.form.object.table.grid.user.toolbar.view.GToolbarButton;
import lsfusion.gwt.client.form.view.Column;
import lsfusion.gwt.client.view.StyleDefaults;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GFilterConditionView extends FlexPanel {
    private static final ClientMessages messages = ClientMessages.Instance.get();
    public interface UIHandler {
        void addEnterBinding(Widget widget);
        void conditionRemoved(GPropertyFilter condition);
        void applyFilters();
    }

    private static final String DELETE_ICON_PATH = "filtdel.png";
    private static final String SEPARATOR_ICON_PATH = "filtseparator.png";

    private GPropertyFilter condition;

    private Map<Column, String> columns = new HashMap<>();
    
    private Label propertyLabel;
    private GFilterOptionSelector<Column> propertyView;

    private Label compareLabel;
    private GFilterCompareSelector compareView;

    private GDataFilterValueView valueView;

    private GToolbarButton deleteButton;

    private Widget junctionSeparator;
    private GToolbarButton junctionView;

    public boolean allowNull = false;

    private boolean isLast = false;
    private boolean toolsVisible;
    
    private boolean focused = false;

    public GFilterConditionView(GPropertyFilter iCondition, GTableController logicsSupplier, final UIHandler handler, boolean toolsVisible) {
        this.condition = iCondition;
        this.toolsVisible = toolsVisible;

        List<Pair<Column, String>> selectedColumns = logicsSupplier.getSelectedColumns();
        for (Pair<Column, String> column : selectedColumns) {
            columns.put(column.first, column.second);
        }

        Column currentColumn = new Column(condition.property, condition.columnKey);
        String currentCaption = columns.get(currentColumn);
        
        propertyLabel = new Label(currentCaption);
        propertyLabel.addStyleName("userFilterLabel");
        addCentered(propertyLabel);

        propertyView = new GFilterOptionSelector<Column>(new Column[0]) {
            @Override
            public void valueChanged(Column column) {
                condition.property = column.property;
                condition.columnKey = column.columnKey;

                propertyLabel.setText(columns.get(column));

                propertyChanged();
            }
        };
        for (Pair<Column, String> column : selectedColumns) {
            propertyView.add(column.first, column.second);
        }
        propertyView.setSelectedValue(currentColumn, currentCaption);
        addCentered(propertyView);
        
        compareLabel = new Label((condition.negation ? "!" : "") + condition.compare);
        compareLabel.addStyleName("userFilterLabel");
        addCentered(compareLabel);

        compareView = new GFilterCompareSelector(condition) {
            @Override
            public void negationChanged(boolean value) {
                condition.negation = value;
                compareLabel.setText((value ? "!" : "") + condition.compare);
            }

            @Override
            public void allowNullChanged(boolean value) {
                allowNull = value;
            }

            @Override
            public void valueChanged(GCompare value) {
                super.valueChanged(value);
                condition.compare = value;
                compareLabel.setText((condition.negation ? "!" : "") + value);
            }
        };
        compareView.setSelectedValue(condition.compare);
        addCentered(compareView);

        valueView = new GDataFilterValueView(condition.value, condition.property, condition.columnKey, logicsSupplier) {
            @Override
            public void setFocused(boolean focused) {
                GFilterConditionView.this.focused = focused;
            }
        };
        handler.addEnterBinding(valueView.cell);
        addCentered(valueView);

        deleteButton = new GToolbarButton(DELETE_ICON_PATH, messages.formQueriesFilterRemoveCondition()) {
            @Override
            public ClickHandler getClickHandler() {
                return event -> handler.conditionRemoved(condition);
            }
        };
        deleteButton.addStyleName("userFilterButton");
        addCentered(deleteButton);

        junctionSeparator = GwtClientUtils.createVerticalSeparator(StyleDefaults.COMPONENT_HEIGHT);
        junctionSeparator.addStyleName("userFilterJunctionSeparator");
        addCentered(junctionSeparator);

        junctionView = new GToolbarButton(SEPARATOR_ICON_PATH, messages.formFilterConditionViewOr()) {
            @Override
            public ClickHandler getClickHandler() {
                return event -> {
                    condition.junction = !condition.junction;
                    showBackground(!condition.junction);        
                };
            }
        };
        junctionView.addStyleName("userFilterButton");
        junctionView.getElement().getStyle().setPaddingTop(0, Style.Unit.PX);
        junctionView.showBackground(!condition.junction);
        addCentered(junctionView);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        setToolsVisible(toolsVisible);
    }

    public void setLast(boolean isLast) {
        this.isLast = isLast;

        updateJunctionVisibility();
    }

    public void setToolsVisible(boolean visible) {
        toolsVisible = visible;
        deleteButton.setVisible(visible);

        propertyLabel.setVisible(!toolsVisible);
        propertyView.setVisible(toolsVisible);

        compareLabel.setVisible(!toolsVisible);
        compareView.setVisible(toolsVisible);

        updateJunctionVisibility();
    }
    
    private void updateJunctionVisibility() {
        junctionSeparator.setVisible(!toolsVisible && !isLast && !condition.junction);
        junctionView.setVisible(toolsVisible && !isLast);
    }
    
    public boolean isFocused() {
        return focused;
    }

    private void propertyChanged() {
        valueView.propertyChanged(condition);
        
        GCompare oldCompare = condition.compare;
        GCompare[] filterCompares = condition.property.getFilterCompares();
        compareView.set(filterCompares);
        if (Arrays.asList(filterCompares).contains(oldCompare)) {
            compareView.setSelectedValue(oldCompare);
        } else {
            GCompare defaultCompare = condition.property.getDefaultCompare();
            compareView.setSelectedValue(defaultCompare);
            condition.compare = defaultCompare;

            compareLabel.setText(defaultCompare.toString());
        }
    }

    public void focusOnValue() {
        valueView.focusOnValue();
    }

    public void startEditing(Event keyEvent) {
        valueView.startEditing(keyEvent);
    }
}
