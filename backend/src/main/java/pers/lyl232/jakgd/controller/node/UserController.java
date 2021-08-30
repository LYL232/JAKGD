package pers.lyl232.jakgd.controller.node;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pers.lyl232.jakgd.controller.BaseController;
import pers.lyl232.jakgd.entity.node.Permission;
import pers.lyl232.jakgd.entity.node.User;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.exception.UnauthorizedException;
import pers.lyl232.jakgd.service.node.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RequestMapping("/api/user")
@RestController
public class UserController extends BaseController {
    final private UserService service;

    final private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${enable-sign-up}")
    private boolean enableSignUp;

    public UserController(UserService service, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.service = service;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @GetMapping("/")
    public User get() throws ExceptionWithBriefJSONResponse {
        return service.findByUsernameNotNull(getAuthenticatedUsername());
    }

    @PostMapping("")
    public ResponseEntity<Object> signUp(
            @RequestBody Map<String, String> body)
            throws ExceptionWithBriefJSONResponse {
        if (!enableSignUp) {
            return forbiddenResponse("sign up disabled");
        }
        String username = body.get("username"),
                password = body.get("password");
        if (username == null || password == null
                || username.isEmpty() || password.isEmpty()) {
            return parameterMissingResponse("username: String, password: String");
        }
        service.signUp(username, bCryptPasswordEncoder.encode(password));
        return createdResponse();
    }

    @PostMapping("/password")
    public ResponseEntity<Object> changePassword(
            @RequestBody Map<String, String> body
    ) throws ExceptionWithBriefJSONResponse {
        String password = body.get("password");
        if (password == null) {
            return parameterMissingResponse("password: String");
        }
        if (password.length() < 6) {
            return invalidParameterResponse("invalid password");
        }
        User user = service.findByUsernameNotNull(getAuthenticatedUsername());
        user.setPassword(bCryptPasswordEncoder.encode(password));
        service.update(user);
        return noContentResponse();
    }

    @GetMapping("/permission")
    public List<Permission> getPermissions() throws UnauthorizedException {
        return new ArrayList<>(service.getPermissions(getAuthenticatedUsername()));
    }

    @GetMapping("/permission/{permission}")
    public boolean hasPermission(@PathVariable String permission)
            throws UnauthorizedException {
        return service.hasPermission(getAuthenticatedUsername(), permission);
    }
}
