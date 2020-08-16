package pers.lyl232.jakgd.controller;

import com.alibaba.fastjson.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.exception.UnknownException;
import pers.lyl232.jakgd.util.BriefJSONResponse;

@RestControllerAdvice
public class RestExceptionHandler {
    final static private Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler
    public ResponseEntity<Object> handle(Exception e) {
        try {
            // 个人比较喜欢try catch的语法
            throw e;
        } catch (JSONException exception) {
            return new BriefJSONResponse(BriefJSONResponse.Code.JSON_ERROR).responseEntity;
        } catch (UnknownException exception) {
            logger.error(exception.getMessage());
            return exception.response.responseEntity;
        } catch (ExceptionWithBriefJSONResponse exception) {
            return exception.response.responseEntity;
        } catch (DataAccessResourceFailureException exception) {
            logger.error("database access failed");
            return new BriefJSONResponse(BriefJSONResponse.Code.DATABASE_ACCESS_FAILED).responseEntity;
        } catch (MissingServletRequestParameterException
                | HttpMessageNotReadableException exception) {
            String msg = exception.getMessage();
            if (msg == null) {
                return new BriefJSONResponse(BriefJSONResponse.Code.PARAMETER_MISSING).responseEntity;
            }
            return new BriefJSONResponse(BriefJSONResponse.Code.PARAMETER_MISSING, msg).responseEntity;
        } catch (HttpRequestMethodNotSupportedException exception) {
            return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
        } catch (MethodArgumentTypeMismatchException exception) {
            return new BriefJSONResponse(BriefJSONResponse.Code.INVALID_PARAMETER).responseEntity;
        } catch (HttpMediaTypeNotSupportedException exception) {
            return new BriefJSONResponse(BriefJSONResponse.Code.MEDIA_TYPE_NOT_SUPPORT).responseEntity;
        } catch (Exception exception) {
            logger.error("unknown exception: " + e);
            return new BriefJSONResponse(BriefJSONResponse.Code.UNKNOWN, exception).responseEntity;
        }
    }
}
