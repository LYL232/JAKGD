package pers.lyl232.jakgd.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import pers.lyl232.jakgd.exception.*;
import pers.lyl232.jakgd.util.BriefJSONResponse;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class BaseController {
    final static private Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Value("${max-properties-count}")
    private int maxPropertiesCount;

    @Value("${max-property-length}")
    private int maxPropertyLength;

    /**
     * 检查body内的属性长度是否超过限制
     *
     * @param body body
     * @throws ContentLimitException 超过限制异常
     */
    protected Map<String, String> getBodyProperties(
            Map<String, Object> body, Pattern validPattern)
            throws ExceptionWithBriefJSONResponse {
        if (body.size() > maxPropertiesCount) {
            throw new ContentLimitException();
        }
        Map<String, String> res = new HashMap<>();
        for (Map.Entry<String, Object> entry : body.entrySet()) {
            String key = entry.getKey();
            checkIfStringMatchesPattern(validPattern, key);
            if (entry.getValue() instanceof String) {
                if (((String) entry.getValue()).length() > maxPropertyLength) {
                    throw new ContentLimitException();
                }
                res.put(key, (String) entry.getValue());
            } else {
                throw new InvalidParameterException(key);
            }
        }
        return res;
    }

    /**
     * 获取已认证的用户名
     *
     * @return 已认证用户名
     * @throws UnauthorizedException 游客登录时的异常响应
     */
    static protected String getAuthenticatedUsername() throws UnauthorizedException {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (username.equals("anonymousUser")) {
            throw new UnauthorizedException("anonymousUser");
        }
        return username;
    }

    /**
     * 检查label是否是有效的
     *
     * @param pattern 匹配模式
     * @param label   label list
     * @throws ExceptionWithBriefJSONResponse 非法
     */
    static protected void checkIfStringMatchesPattern(Pattern pattern, String label)
            throws ExceptionWithBriefJSONResponse {
        Matcher matcher = pattern.matcher(label);
        if (!matcher.matches()) {
            throw new ExceptionWithBriefJSONResponse(
                    BriefJSONResponse.Code.INVALID_LABEL, label);
        }
    }

    /**
     * 从body中的labels建获取有效的labels, 并会去除body中的labels键
     *
     * @param body         body map
     * @param validPattern 有效的label 模式
     * @return 有效的label集合
     * @throws ExceptionWithBriefJSONResponse labels解析错误, labels格式错误
     */
    static protected Set<String> getLabelsFromBodyAndRemove(
            Map<String, Object> body, Pattern validPattern)
            throws ExceptionWithBriefJSONResponse {
        Object object = body.get("labels");
        logger.info("object class" + object.getClass());
        if (!(object instanceof List<?>)) {
            return new HashSet<>();
        }
        body.remove("labels");
        Set<String> labels = new HashSet<>();
        try {
            for (Object labelObj : (List<?>) object) {
                String label = (String) labelObj;
                checkIfStringMatchesPattern(validPattern, label);
                labels.add(label);
            }
        } catch (ClassCastException exception) {
            throw new InvalidParameterException("labels");
        }
        return labels;

    }

    /**
     * 检查body中是否拥有所要求的非空字符串参数
     *
     * @param body    map
     * @param require 所需的参数
     * @throws ParameterMissingException 参数找不到或者参数为空
     */
    static protected void requireBodyNotEmptyParameters(
            Map<String, String> body, String... require)
            throws ParameterMissingException {
        for (String param : require) {
            String value = body.get(param);
            if (value == null) {
                throw new ParameterMissingException(param);
            }
            value = value.trim();
            if (value.isEmpty()) {
                throw new ParameterMissingException(param);
            }
            body.put(param, value);
        }
    }

    static protected ResponseEntity<Object> createdResponse() {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    static protected ResponseEntity<Object> internalErrorResponse(String msg) {
        logger.error("msg");
        return new ResponseEntity<>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    static protected ResponseEntity<Object> noContentResponse() {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    static protected ResponseEntity<Object> invalidParameterResponse(String msg) {
        return new BriefJSONResponse(BriefJSONResponse.Code.INVALID_PARAMETER, msg).responseEntity;
    }

    static protected ResponseEntity<Object> notAllowEmptyBodyResponse() {
        return new BriefJSONResponse(BriefJSONResponse.Code.NOT_ALLOWED_EMPTY_BODY).responseEntity;
    }

    static protected ResponseEntity<Object> parameterMissingResponse(String msg) {
        return new BriefJSONResponse(BriefJSONResponse.Code.PARAMETER_MISSING, msg).responseEntity;
    }

    static protected ResponseEntity<Object> forbiddenResponse() {
        return new BriefJSONResponse(BriefJSONResponse.Code.FORBIDDEN).responseEntity;
    }

    static protected ResponseEntity<Object> forbiddenResponse(String msg) {
        return new BriefJSONResponse(BriefJSONResponse.Code.FORBIDDEN, msg).responseEntity;
    }

    static protected ResponseEntity<Object> wrongRequestResponse(String msg) {
        return new BriefJSONResponse(BriefJSONResponse.Code.WRONG_REQUEST, msg).responseEntity;
    }
}
