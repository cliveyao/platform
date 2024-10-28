package lsfusion.server.data.sql.exception;

public class SQLConflictException extends SQLHandledException {

    public final boolean updateConflict;
    public SQLConflictException(boolean updateConflict) {
        this.updateConflict = updateConflict;
    }

    @Override
    public String getMessage() {
        return updateConflict ? "UPDATE_CONFLICT" : "DEAD_LOCK";
    }

    public static String UPDATECONFLICT = "cn";

    @Override
    public String getDescription(boolean wholeTransaction) {
        return updateConflict ? UPDATECONFLICT : "dd";
    }
}
