package platform.server.data.sql;

import platform.base.BaseUtils;
import platform.base.IOUtils;
import platform.server.data.type.Type;
import platform.server.logics.BusinessLogics;
import platform.server.logics.ServerResourceBundle;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;


public class PostgreDataAdapter extends DataAdapter {

    // Для debuga конструктор
    public PostgreDataAdapter() {
    }

    public PostgreDataAdapter(String dataBase, String server, String userID, String password) throws Exception, SQLException, InstantiationException, IllegalAccessException {
        this(dataBase, server, userID, password, false);
    }

    public PostgreDataAdapter(String dataBase, String server, String userID, String password, boolean cleanDB) throws Exception, SQLException, InstantiationException, IllegalAccessException {
        super(dataBase, server, userID, password);
        internalEnsureDB(cleanDB);
    }

    private void internalEnsureDB(boolean cleanDB) throws Exception, SQLException, InstantiationException, IllegalAccessException {
        Connection connect = DriverManager.getConnection("jdbc:postgresql://" + server + "/postgres?user=" + userID + "&password=" + password);
        if (cleanDB) {
            try {
                connect.createStatement().execute("DROP DATABASE " + dataBase);
            } catch (SQLException e) {
                logger.error(ServerResourceBundle.getString("data.sql.error.creating.database"), e);
            }
        }

        try {
            // обязательно нужно создавать на основе template0, так как иначе у template1 может быть другая кодировка и ошибка
            connect.createStatement().execute("CREATE DATABASE " + dataBase + " WITH TEMPLATE template0 ENCODING='UTF8' ");
        } catch (SQLException e) {
            logger.info(ServerResourceBundle.getString("data.sql.error.creating.database"), e);
        }
        connect.close();

        connect = startConnection();
        connect.createStatement().execute(IOUtils.readStreamToString(BusinessLogics.class.getResourceAsStream("/sqlaggr/getAnyNotNull.sc")));
        connect.createStatement().execute(IOUtils.readStreamToString(BusinessLogics.class.getResourceAsStream("/sqlfun/jumpWorkdays.sc")));
        connect.createStatement().execute(IOUtils.readStreamToString(BusinessLogics.class.getResourceAsStream("/sqlfun/completeBarcode.sc")));
        connect.createStatement().execute(IOUtils.readStreamToString(BusinessLogics.class.getResourceAsStream("/sqlaggr/aggf.sc")));
        connect.close();
    }

    public void ensureDB() throws Exception, SQLException, InstantiationException, IllegalAccessException {
        //всё делаем в internalEnsureDB
    }

    @Override
    public String getLongType() {
        return "int8";
    }

    @Override
    public int getLongSQL() {
        return Types.BIGINT;
    }

    public boolean allowViews() {
        return true;
    }

    public String getUpdate(String tableString, String setString, String fromString, String whereString) {
        return tableString + setString + " FROM " + fromString + whereString;
    }

    public String getClassName() {
        return "org.postgresql.Driver";
    }

    public Connection startConnection() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        return DriverManager.getConnection("jdbc:postgresql://" + server + "/" + dataBase + "?user=" + userID + "&password=" + password);
    }

    public String getCommandEnd() {
        return ";";
    }

    public String getClustered() {
        return "";
    }

    // у SQL сервера что-то гдючит ISNULL (а значит скорее всего и COALESCE) когда в подзапросе просто число указывается
    public boolean isNullSafe() {
        return false;
    }

    public String isNULL(String expr1, String expr2, boolean notSafe) {
//        return "(CASE WHEN "+Expr1+" IS NULL THEN "+Expr2+" ELSE "+Expr1+" END)";
        return "COALESCE(" + expr1 + "," + expr2 + ")";
    }

    public String getSelect(String from, String exprs, String where, String orderBy, String groupBy, String having, String top) {
        return "SELECT " + exprs + " FROM " + from + BaseUtils.clause("WHERE", where) + BaseUtils.clause("GROUP BY", groupBy) + BaseUtils.clause("HAVING", having) + BaseUtils.clause("ORDER BY", orderBy) + BaseUtils.clause("LIMIT", top);
    }

    public String getUnionOrder(String union, String orderBy, String top) {
        return union + BaseUtils.clause("ORDER BY", orderBy) + BaseUtils.clause("LIMIT", top);
    }

    public String getByteArrayType() {
        return "bytea";
    }
    @Override
    public int getByteArraySQL() {
        return Types.VARBINARY;
    }

    @Override
    public String getOrderDirection(boolean descending, boolean notNull) {
        return (descending?"DESC":"ASC")  + (!notNull? " NULLS " + (descending?"LAST":"FIRST"): "");
    }

    @Override
    public boolean isBinaryString() {
        return true;
    }

    @Override
    public String getBinaryType(int length) {
//        return "bit(" + length * 8 + ")";
        return getStringType(length);
    }
    @Override
    public int getBinarySQL() {
        return getStringSQL();
    }

    @Override
    public String getBinaryConcatenate() {
        return "||";
    }

    @Override
    public boolean useFJ() {
        return false;
    }

    @Override
    public boolean orderUnion() {
        return true;
    }

    @Override
    public boolean nullUnionTrouble() {
        return true;
    }

    @Override
    public boolean inlineTrouble() {
        return true;
    }

    @Override
    public String typeConvertSuffix(Type oldType, Type newType, String name) {
        return "USING " + name + "::" + newType.getDB(this);
    }

    @Override
    public String getInsensitiveLike() {
        return "ILIKE";
    }

    public boolean supportGroupNumbers() {
        return true;
    }

    public boolean orderTopTrouble() {
        return true;
    }
    
    @Override
    public boolean backupDB(String binPath, String dumpDir) throws IOException, InterruptedException {
        String path = "\"" + ((binPath == null) ? "" : binPath) + "pg_dump.exe" + "\"";
        String host = "", port = "5432";
        if (server.contains(":")) {
            host = server.substring(0, server.lastIndexOf(':'));
            port = server.substring(server.lastIndexOf(':') + 1);
        } else {
            host = server;
        }

        String execute = "cmd SET PGPASSWORD=" + password + " /c start \"\" /B " + path + " --host " + host + " --port " + port + " --username " +
                userID + " --format tar --blobs --verbose --file \"" + dumpDir + "\" " +
                dataBase + " 2>> \"" + dumpDir + "log.txt\"";
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(execute);
        return p.waitFor()==0;
    }

    @Override
    public String getAnalyze() {
        return "VACUUM ANALYZE";
    }
}
