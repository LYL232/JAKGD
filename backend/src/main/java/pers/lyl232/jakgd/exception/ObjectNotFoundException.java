package pers.lyl232.jakgd.exception;

import pers.lyl232.jakgd.util.BriefJSONResponse;

public class ObjectNotFoundException extends ExceptionWithBriefJSONResponse {
    public ObjectNotFoundException(String appendMsg) {
        super(BriefJSONResponse.Code.OBJECT_NOT_FOUND, appendMsg);
    }
}
