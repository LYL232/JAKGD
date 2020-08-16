package pers.lyl232.jakgd.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 简要的JSON响应类
 */
public class BriefJSONResponse {
    final static private DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    final public ResponseEntity<Object> responseEntity;

    public enum Code {
        UNKNOWN, // 未知异常 0
        UNAUTHORIZED, // 未认证 1
        FORBIDDEN,  // 无权限 2
        USERNAME_OCCUPIED,  // 用户名已被占用 3
        JSON_ERROR,  // 非json格式 4
        EMPTY_FILE,  // 上传空文件请求 5
        FILE_HANDLE_ERROR,  // 文件处理错误 6
        FILE_NOT_EXIST,  // 文件不存在 7
        OBJECT_NOT_FOUND,  // 找不到对象 8
        DATABASE_ACCESS_FAILED,  // 无法连接数据库 9
        PARAMETER_MISSING,  // 参数缺失 10
        INVALID_LABEL,  // 不允许的label 11
        CONTENT_LIMIT_EXCEED,  // 内容过长 12
        INVALID_PARAMETER,  // 无效的参数 13
        NOT_ALLOWED_EMPTY_BODY, // 空body 14
        DELETE_NODE_WITH_RELATIONSHIP, // 删除仍有关系的节点 15
        SELF_REFER, // 自引用 16
        DUPLICATE_REFER, // 重复的引用关系边 17
        QUERY_EXECUTE_ERROR, // 查询执行失败 18
        WRONG_REQUEST, // 请求了错误的方法 19
        MEDIA_TYPE_NOT_SUPPORT, // 错误的请求media type 20
        DUPLICATE_RELATIONSHIP, // 同样关系的多重边 21
    }

    final private static Map<Code, String> codeMessage = new HashMap<>();
    final private static Map<Code, HttpStatus> codeStatus = new HashMap<>();

    static {
        codeMessage.put(Code.UNKNOWN,
                "Unknown error, please contact LYL232: linyongliang232@hotmail.com, " +
                        "github: https://github/LYL232 with this whole information");
        codeStatus.put(Code.UNKNOWN, HttpStatus.INTERNAL_SERVER_ERROR);

        codeMessage.put(Code.UNAUTHORIZED, "Unauthorized");
        codeStatus.put(Code.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);

        codeMessage.put(Code.FORBIDDEN, "Access denied");
        codeStatus.put(Code.FORBIDDEN, HttpStatus.FORBIDDEN);

        codeMessage.put(Code.USERNAME_OCCUPIED, "Username is already occupied");
        codeStatus.put(Code.USERNAME_OCCUPIED, HttpStatus.OK);

        codeMessage.put(Code.JSON_ERROR, "Error json format");
        codeStatus.put(Code.JSON_ERROR, HttpStatus.BAD_REQUEST);

        codeMessage.put(Code.EMPTY_FILE, "Upload file empty");
        codeStatus.put(Code.EMPTY_FILE, HttpStatus.BAD_REQUEST);

        codeMessage.put(Code.FILE_HANDLE_ERROR, "Error occurs " +
                "while handling uploading file. Please try again or contact " +
                "LYL232: linyongliang232@hotmail.com, " +
                "github: https://github/LYL232 with this whole information");
        codeStatus.put(Code.FILE_HANDLE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

        codeMessage.put(Code.FILE_NOT_EXIST, "File not exists");
        codeStatus.put(Code.FILE_NOT_EXIST, HttpStatus.OK);

        codeMessage.put(Code.OBJECT_NOT_FOUND, "Object not found");
        codeStatus.put(Code.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND);

        codeMessage.put(Code.DATABASE_ACCESS_FAILED, "Database access failed");
        codeStatus.put(Code.DATABASE_ACCESS_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);

        codeMessage.put(Code.PARAMETER_MISSING, "Parameter missing");
        codeStatus.put(Code.PARAMETER_MISSING, HttpStatus.BAD_REQUEST);

        codeMessage.put(Code.INVALID_LABEL, "Invalid label");
        codeStatus.put(Code.INVALID_LABEL, HttpStatus.BAD_REQUEST);

        codeMessage.put(Code.CONTENT_LIMIT_EXCEED, "Content limit exceed");
        codeStatus.put(Code.CONTENT_LIMIT_EXCEED, HttpStatus.BAD_REQUEST);

        codeMessage.put(Code.INVALID_PARAMETER, "Invalid parameter");
        codeStatus.put(Code.INVALID_PARAMETER, HttpStatus.BAD_REQUEST);

        codeMessage.put(Code.NOT_ALLOWED_EMPTY_BODY, "Empty body is not allowed");
        codeStatus.put(Code.NOT_ALLOWED_EMPTY_BODY, HttpStatus.BAD_REQUEST);

        codeMessage.put(Code.DELETE_NODE_WITH_RELATIONSHIP, "Can not delete node with relationship");
        codeStatus.put(Code.DELETE_NODE_WITH_RELATIONSHIP, HttpStatus.BAD_REQUEST);

        codeMessage.put(Code.SELF_REFER,
                "Can not refer knowledge itself");
        codeStatus.put(Code.SELF_REFER, HttpStatus.BAD_REQUEST);

        codeMessage.put(Code.DUPLICATE_REFER, "Duplicate reference");
        codeStatus.put(Code.DUPLICATE_REFER, HttpStatus.BAD_REQUEST);

        codeMessage.put(Code.QUERY_EXECUTE_ERROR, "Query statement execute failed");
        codeStatus.put(Code.QUERY_EXECUTE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

        codeMessage.put(Code.WRONG_REQUEST, "Wrong request, please try correct way");
        codeStatus.put(Code.WRONG_REQUEST, HttpStatus.BAD_REQUEST);

        codeMessage.put(Code.MEDIA_TYPE_NOT_SUPPORT, "Wrong request media type");
        codeStatus.put(Code.MEDIA_TYPE_NOT_SUPPORT, HttpStatus.BAD_REQUEST);

        codeMessage.put(Code.DUPLICATE_RELATIONSHIP,
                "Duplicate relationship between the nodes");
        codeStatus.put(Code.DUPLICATE_RELATIONSHIP, HttpStatus.BAD_REQUEST);

    }

    public BriefJSONResponse(Code code) {
        JSONObject body = new JSONObject();
        body.put("code", code.ordinal());
        body.put("message", codeMessage.get(code));
        responseEntity = new ResponseEntity<>(body.toJSONString(), codeStatus.get(code));
    }

    public BriefJSONResponse(Code code, String appendMsg) {
        JSONObject body = new JSONObject();
        body.put("code", code.ordinal());
        body.put("message", codeMessage.get(code) +
                ((!appendMsg.isEmpty()) ? ": " + appendMsg : ""));
        responseEntity = new ResponseEntity<>(body.toJSONString(), codeStatus.get(code));
    }

    /**
     * 异常时需要的构造器
     *
     * @param code      异常代码
     * @param exception 异常对象
     */
    public BriefJSONResponse(Code code, Exception exception) {
        JSONObject body = new JSONObject();
        body.put("code", code.ordinal());
        body.put("message", codeMessage.get(code) + ": " + exception);
        body.put("when", dateFormatter.format(ZonedDateTime.now()));
        responseEntity = new ResponseEntity<>(body.toJSONString(), codeStatus.get(code));
    }


}
