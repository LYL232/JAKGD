package pers.lyl232.jakgd.exception;

import pers.lyl232.jakgd.util.BriefJSONResponse;

/**
 * 带有返回信息的异常信息, 可以直接获取返回体
 */
public class ExceptionWithBriefJSONResponse extends Exception {
    final public BriefJSONResponse response;

    public ExceptionWithBriefJSONResponse(BriefJSONResponse.Code code) {
        super("with BriefJSONResponse. code: " + code);
        response = new BriefJSONResponse(code);
    }

    public ExceptionWithBriefJSONResponse(BriefJSONResponse.Code code, String appendMsg) {
        super(String.format("with BriefJSONResponse. code: %s. appendMsg: %s.",
                code, appendMsg));
        response = new BriefJSONResponse(code, appendMsg);
    }

    public ExceptionWithBriefJSONResponse(BriefJSONResponse.Code code, Exception exception) {
        super(String.format("with BriefJSONResponse. code: %s. exception: %s.",
                code, exception));
        response = new BriefJSONResponse(code, exception);
    }
}
