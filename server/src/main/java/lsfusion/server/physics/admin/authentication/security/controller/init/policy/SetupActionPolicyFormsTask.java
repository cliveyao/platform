package lsfusion.server.physics.admin.authentication.security.controller.init.policy;

import lsfusion.server.language.property.LP;
import lsfusion.server.logics.action.Action;
import lsfusion.server.logics.form.struct.FormEntity;
import lsfusion.server.logics.property.oraction.ActionOrProperty;

public class SetupActionPolicyFormsTask extends SetupActionOrPropertyPolicyFormsTask {

    public String getCaption() {
        return "Setting up action policy";
    }

    @Override
    protected FormEntity getForm() {
        return getBL().securityLM.actionPolicyForm;
    }

    @Override
    protected LP getCanonicalName() {
        return getBL().reflectionLM.actionCanonicalName;
    }

    @Override
    protected void runTask(ActionOrProperty property) {
        if(property instanceof Action)
            getBL().setupPropertyPolicyForms(setupPolicyByCN, property, true);
    }
}
