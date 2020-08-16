package pers.lyl232.jakgd.exception;

import pers.lyl232.jakgd.util.BriefJSONResponse;

public class ParameterMissingException extends ExceptionWithBriefJSONResponse {
    public ParameterMissingException(String param) {
        super(BriefJSONResponse.Code.PARAMETER_MISSING, param);
    }
}
