package pers.lyl232.jakgd.controller.relationship;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pers.lyl232.jakgd.controller.BaseController;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.service.node.UserService;
import pers.lyl232.jakgd.service.relationship.ReferRelationshipService;
import pers.lyl232.jakgd.util.BriefJSONResponse;

@RequestMapping("/api/reference")
@RestController
public class ReferenceRelationshipController extends BaseController {

    final private ReferRelationshipService referRelationshipService;

    final private UserService userService;

    public ReferenceRelationshipController(
            ReferRelationshipService referRelationshipService,
            UserService userService) {
        this.referRelationshipService = referRelationshipService;
        this.userService = userService;
    }

    @PostMapping("/{id}/{referId}")
    public ResponseEntity<Object> create(
            @PathVariable Long id,
            @PathVariable Long referId
    ) throws ExceptionWithBriefJSONResponse {
        if (id.equals(referId)) {
            return new BriefJSONResponse(BriefJSONResponse.Code.SELF_REFER).responseEntity;
        }
        if (!userService.hasNodesModifyPermission(
                getAuthenticatedUsername(), id, referId)) {
            return forbiddenResponse();
        }
        referRelationshipService.create(id, referId);
        return noContentResponse();
    }
}
