package pers.lyl232.jakgd.exception;

import pers.lyl232.jakgd.util.BriefJSONResponse;

public class InvalidParameterException extends ExceptionWithBriefJSONResponse {
    public InvalidParameterException(String appendMsg) {
        super(BriefJSONResponse.Code.INVALID_PARAMETER, appendMsg);
    }
}
