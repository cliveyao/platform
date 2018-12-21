package lsfusion.server.classes;

import com.hexiong.jdbf.JDBFException;
import lsfusion.base.ExtInt;
import lsfusion.interop.Data;
import lsfusion.server.data.expr.query.Stat;
import lsfusion.server.data.query.TypeEnvironment;
import lsfusion.server.data.sql.SQLSyntax;
import lsfusion.server.data.type.ParseException;
import lsfusion.server.form.view.report.ReportDrawField;
import lsfusion.server.logics.i18n.LocalizedString;
import lsfusion.server.logics.property.actions.integration.exporting.plain.dbf.OverJDBField;
import lsfusion.server.logics.property.actions.integration.exporting.plain.xls.ExportXLSWriter;
import lsfusion.server.logics.property.actions.integration.importing.plain.dbf.CustomDbfRecord;
import net.sf.jasperreports.engine.type.HorizontalAlignEnum;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.json.JSONException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LogicalClass extends DataClass<Boolean> {

    public static final LogicalClass instance = new LogicalClass();

    static {
        DataClass.storeClass(instance);
    }

    private LogicalClass() { super(LocalizedString.create("{classes.logical}"));}

    public int getReportPreferredWidth() { return 50; }

    public Class getReportJavaClass() {
        return Boolean.class;
    }

    public void fillReportDrawField(ReportDrawField reportField) {
        super.fillReportDrawField(reportField);

        reportField.alignment = HorizontalAlignEnum.CENTER.getValue();
    }

    public byte getTypeID() {
        return Data.LOGICAL;
    }

    public DataClass getCompatible(DataClass compClass, boolean or) {
        return compClass instanceof LogicalClass ?this:null;
    }

    public Boolean getDefaultValue() {
        return true;
    }

    public String getDB(SQLSyntax syntax, TypeEnvironment typeEnv) {
        return syntax.getBitType();
    }

    public String getDotNetType(SQLSyntax syntax, TypeEnvironment typeEnv) {
        return "SqlInt32";
    }

    public String getDotNetRead(String reader) {
        return reader + ".ReadInt32()";
    }
    public String getDotNetWrite(String writer, String value) {
        return writer + ".Write(" + value + ");";
    }

    @Override
    public int getBaseDotNetSize() {
        return 4;
    }
    public int getSQL(SQLSyntax syntax) {
        return syntax.getBitSQL();
    }

    public Boolean read(Object value) {
        if(value instanceof Boolean)
            return (Boolean)value ? true : null;
        if(value!=null) return true;
        return null;
    }

    @Override
    public Boolean read(ResultSet set, SQLSyntax syntax, String name) throws SQLException {
        return super.read(set, syntax, name);//set.getBoolean(name);
    }

    public boolean isSafeString(Object value) {
        return true;
    }
    public String getString(Object value, SQLSyntax syntax) {
        assert (Boolean)value;
        return syntax.getBitString(true);
    }

    public void writeParam(PreparedStatement statement, int num, Object value, SQLSyntax syntax) throws SQLException {
        assert (Boolean)value;
        statement.setByte(num, (byte)1);
    }

/*    public boolean isSafeString(Object value) {
        return true;
    }
    public String getString(Object value, SQLSyntax syntax) {
        return syntax.getBitString((Boolean) value);
    }

    public void writeParam(PreparedStatement statement, int num, Object value) throws SQLException {
        statement.setByte(num, (byte) ((Boolean)value?1:0));
    }
  */

    @Override
    public ExtInt getCharLength() {
        return new ExtInt(1);
    }

    @Override
    public Object getInfiniteValue(boolean min) {
        return true;
    }

    public Boolean shiftValue(Boolean object, boolean back) {
        return object==null?true:null;
    }

    @Override
    public Boolean parseDBF(CustomDbfRecord dbfRecord, String fieldName, String charset) throws ParseException, java.text.ParseException {
        return readDBF(dbfRecord.getBoolean(fieldName));
    }
    @Override
    public Boolean parseJSON(Object value) throws JSONException {
        return readJSON(value);
    }
    @Override
    public Boolean parseXLS(Cell cell, CellValue formulaValue) throws ParseException {
        if(formulaValue.getCellTypeEnum().equals(CellType.BOOLEAN))
            return readXLS(formulaValue.getBooleanValue());
        return super.parseXLS(cell, formulaValue);
    }

    @Override
    public Object formatJSON(Boolean object) {
        return object != null;
    }

    @Override
    public void formatXLS(Boolean object, Cell cell, ExportXLSWriter.Styles styles) {
        cell.setCellValue(object != null);
    }

    public Boolean parseString(String s) throws ParseException {
        try {
            boolean b = Boolean.parseBoolean(s);
            if(b)
                return true;
            return null;
        } catch (Exception e) {
            throw new ParseException("error parsing boolean", e);
        }
    }

    @Override
    public String formatString(Boolean value) {
        return value == null ? null : String.valueOf(value);
    }

    public String getSID() {
        return "BOOLEAN";
    }

    @Override
    public Stat getTypeStat() {
        return Stat.ONE;
    }

    @Override
    public OverJDBField formatDBF(String fieldName) throws JDBFException {
        return new OverJDBField(fieldName, 'L', 1, 0);
    }

    public boolean calculateStat() {
        return false;
    }
}
