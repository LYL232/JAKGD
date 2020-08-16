package pers.lyl232.jakgd.exception;

import pers.lyl232.jakgd.util.BriefJSONResponse;

public class QueryExecuteException extends ExceptionWithBriefJSONResponse {
    public QueryExecuteException(String query) {
        super(BriefJSONResponse.Code.QUERY_EXECUTE_ERROR, query);
    }
}
