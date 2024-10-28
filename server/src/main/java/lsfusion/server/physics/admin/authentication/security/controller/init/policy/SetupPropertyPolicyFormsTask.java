package lsfusion.server.physics.admin.authentication.security.controller.init.policy;

import lsfusion.server.language.property.LP;
import lsfusion.server.logics.form.struct.FormEntity;
import lsfusion.server.logics.property.Property;
import lsfusion.server.logics.property.oraction.ActionOrProperty;

public class SetupPropertyPolicyFormsTask extends SetupActionOrPropertyPolicyFormsTask {

    public String getCaption() {
        return "Setting up property policy";
    }

    @Override
    protected FormEntity getForm() {
        return getBL().securityLM.propertyPolicyForm;
    }

    @Override
    protected LP getCanonicalName() {
        return getBL().reflectionLM.propertyCanonicalName;
    }

    @Override
    protected void runTask(ActionOrProperty property) {
        if(property instanceof Property)
            getBL().setupPropertyPolicyForms(setupPolicyByCN, property, false);
    }
}
