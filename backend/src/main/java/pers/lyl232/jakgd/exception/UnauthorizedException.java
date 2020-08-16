package pers.lyl232.jakgd.exception;

import pers.lyl232.jakgd.util.BriefJSONResponse;

public class UnauthorizedException extends ExceptionWithBriefJSONResponse {
    public UnauthorizedException(String msg) {
        super(BriefJSONResponse.Code.UNAUTHORIZED, msg);
    }

}
