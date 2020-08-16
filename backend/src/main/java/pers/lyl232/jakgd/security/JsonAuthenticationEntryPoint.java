package pers.lyl232.jakgd.security;

import com.fasterxml.jackson.core.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import pers.lyl232.jakgd.util.BriefJSONResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException e) throws
            IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/json;charset=utf-8");
        if (e.getCause() instanceof JsonParseException) {
            BriefJSONResponse res = new BriefJSONResponse(
                    BriefJSONResponse.Code.JSON_ERROR);
            response.setStatus(res.responseEntity.getStatusCodeValue());
            response.getWriter().print(res.responseEntity.getBody());
        } else if (e instanceof InsufficientAuthenticationException) {
            BriefJSONResponse res = new BriefJSONResponse(
                    BriefJSONResponse.Code.UNAUTHORIZED,
                    "authorize failed");
            e.printStackTrace();
            response.setStatus(res.responseEntity.getStatusCodeValue());
            response.getWriter().print(res.responseEntity.getBody());
        } else {
            BriefJSONResponse res = new BriefJSONResponse(
                    BriefJSONResponse.Code.UNKNOWN);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setStatus(res.responseEntity.getStatusCodeValue());
            response.getWriter().print(res.responseEntity.getBody());
        }
    }
}
