package pers.lyl232.jakgd.exception;

import pers.lyl232.jakgd.util.BriefJSONResponse;

public class ForbiddenException extends ExceptionWithBriefJSONResponse {
    public ForbiddenException() {
        super(BriefJSONResponse.Code.FORBIDDEN);
    }
}
