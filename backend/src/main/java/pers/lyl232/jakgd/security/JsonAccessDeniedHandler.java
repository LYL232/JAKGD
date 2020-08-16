package pers.lyl232.jakgd.security;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import pers.lyl232.jakgd.util.BriefJSONResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsonAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException e)
            throws IOException {
        if (!response.isCommitted()) {

            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/json;charset=utf-8");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write(JSONObject.toJSONString(
                    new BriefJSONResponse(BriefJSONResponse.Code.FORBIDDEN)
            ));
        }
    }
}
