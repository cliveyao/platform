package lsfusion.server.physics.admin.drilldown.form;

import lsfusion.server.base.version.Version;
import lsfusion.server.logics.BaseLogicsModule;
import lsfusion.server.logics.action.session.changed.OldProperty;
import lsfusion.server.logics.form.interactive.design.ContainerView;
import lsfusion.server.logics.form.interactive.design.FormView;
import lsfusion.server.logics.form.interactive.design.auto.DefaultFormView;
import lsfusion.server.logics.form.struct.property.PropertyDrawEntity;
import lsfusion.server.logics.property.classes.ClassPropertyInterface;
import lsfusion.server.physics.dev.i18n.LocalizedString;

public class OldDrillDownFormEntity extends DrillDownFormEntity<ClassPropertyInterface, OldProperty<ClassPropertyInterface>> {

    private PropertyDrawEntity propertyDraw;
    private PropertyDrawEntity oldPropertyDraw;

    public OldDrillDownFormEntity(LocalizedString caption, OldProperty property, BaseLogicsModule LM) {
        super(caption, property, LM);
    }

    @Override
    protected void setupDrillDownForm() {
        propertyDraw = addPropertyDraw(property, interfaceObjects);
        oldPropertyDraw = addPropertyDraw(property.property, interfaceObjects);
    }

    @Override
    public FormView createDefaultRichDesign(Version version) {
        DefaultFormView design = (DefaultFormView) super.createDefaultRichDesign(version);

        valueContainer.add(design.get(propertyDraw), version);
        ContainerView oldValueContainer = design.createContainer(LocalizedString.create("{logics.property.drilldown.form.old.value}"), version);
        oldValueContainer.add(design.get(oldPropertyDraw), version);

        design.mainContainer.addAfter(oldValueContainer, valueContainer, version);

        return design;
    }
}
