package pers.lyl232.jakgd.exception;


import pers.lyl232.jakgd.util.BriefJSONResponse;

public class UnknownException extends ExceptionWithBriefJSONResponse {
    public UnknownException(String appendMsg) {
        super(BriefJSONResponse.Code.UNAUTHORIZED, appendMsg);
    }
}
