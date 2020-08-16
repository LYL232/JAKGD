package pers.lyl232.jakgd.exception;

import pers.lyl232.jakgd.util.BriefJSONResponse;

public class ContentLimitException extends ExceptionWithBriefJSONResponse {
    public ContentLimitException() {
        super(BriefJSONResponse.Code.CONTENT_LIMIT_EXCEED);
    }
}
