package roman;

import jxl.read.biff.BiffException;
import platform.server.integration.ImportField;
import platform.server.integration.ImportInputTable;
import platform.server.integration.SingleSheetImporter;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * User: DAle
 * Date: 16.06.11
 * Time: 12:54
 */

public class WomenSecretImportInvoiceActionProperty extends ImportBoxInvoiceActionProperty {
    public WomenSecretImportInvoiceActionProperty(RomanLogicsModule LM) {
        super(LM, LM.womenSecretSupplier, "xls xlsx");
    }

    @Override
    protected SingleSheetImporter createImporter(ImportInputTable inputTable) {
        return new WomenSecretInvoiceImporter(inputTable, new Object[] {sidField, null, null, compositionField,
                new ImportField[] {customCodeField, customCode6Field}, countryField, unitPriceField, null,
                null, unitNetWeightField, null, null, invoiceSIDField, dateInvoiceField, boxNumberField, null, colorCodeField, null, sizeField, unitQuantityField,
                null, originalNameField, barCodeField, numberSkuField, new ImportField[] {colorNameField, RRPField, seasonField, themeCodeField, themeNameField, genderField, sidDestinationDataSupplierBoxField}});
    }

    @Override
    protected ImportInputTable createTable(ByteArrayInputStream inFile) throws BiffException, IOException {
        return new WomenSecretInputTable(inFile);
    }


}
