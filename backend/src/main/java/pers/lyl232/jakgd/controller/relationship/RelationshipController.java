package pers.lyl232.jakgd.controller.relationship;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pers.lyl232.jakgd.controller.BaseController;
import pers.lyl232.jakgd.entity.result.RelationshipData;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.service.node.UserService;
import pers.lyl232.jakgd.service.relationship.RelationshipService;
import pers.lyl232.jakgd.util.BriefJSONResponse;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RequestMapping("/api/relationship")
@RestController
public class RelationshipController extends BaseController {

    final private Pattern validLabelPattern;

    final private RelationshipService relationshipService;

    final private UserService userService;

    public RelationshipController(
            Pattern validLabelPattern,
            UserService userService,
            RelationshipService relationshipService) {
        this.validLabelPattern = validLabelPattern;
        this.userService = userService;
        this.relationshipService = relationshipService;
    }

    @GetMapping("/{id}")
    public RelationshipData get(@PathVariable Long id)
            throws ExceptionWithBriefJSONResponse {
        return relationshipService.getNotNull(id);
    }

    @GetMapping("/detail/{id}")
    public JSONObject getDetail(@PathVariable Long id)
            throws ExceptionWithBriefJSONResponse {
        return relationshipService.getDetail(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> setProperties(
            @PathVariable Long id,
            @RequestBody Map<String, String> properties)
            throws ExceptionWithBriefJSONResponse {
        RelationshipData rel = relationshipService.getNotNull(id);
        if (!userService.hasNodesModifyPermission(
                getAuthenticatedUsername(), rel.startNode, rel.endNode)) {
            return forbiddenResponse();
        }
        relationshipService.setProperties(id, properties);
        return createdResponse();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id) throws ExceptionWithBriefJSONResponse {
        RelationshipData rel = relationshipService.getNotNull(id);
        if (!userService.hasNodesModifyPermission(getAuthenticatedUsername(),
                rel.startNode, rel.endNode)) {
            return forbiddenResponse();
        }
        relationshipService.delete(id);
        return createdResponse();
    }

    @PostMapping("/{startNodeId}/{relType}/{endNodeId}")
    public ResponseEntity<Object> create(
            @PathVariable Long startNodeId,
            @PathVariable String relType,
            @PathVariable Long endNodeId,
            @RequestBody(required = false) Map<String, Object> body
    ) throws ExceptionWithBriefJSONResponse {
        if (relType == null || relType.isEmpty()) {
            return parameterMissingResponse("type: String");
        }
        if (!userService.hasNodesModifyPermission(getAuthenticatedUsername(),
                startNodeId, endNodeId)) {
            return forbiddenResponse();
        }
        checkIfStringMatchesPattern(validLabelPattern, relType);

        if (relationshipService.nodesHasRelationshipByType(startNodeId, endNodeId, relType)) {
            return new BriefJSONResponse(BriefJSONResponse.Code.DUPLICATE_REFER).responseEntity;
        }

        relationshipService.create(startNodeId, endNodeId, relType, body);
        return createdResponse();
    }

    @DeleteMapping("/{startNodeId}/{relType}/{endNodeId}")
    public ResponseEntity<Object> deleteByType(
            @PathVariable Long startNodeId,
            @PathVariable String relType,
            @PathVariable Long endNodeId
    ) throws ExceptionWithBriefJSONResponse {
        if (relType == null || relType.isEmpty()) {
            return parameterMissingResponse("type: String");
        }
        if (!userService.hasNodesModifyPermission(getAuthenticatedUsername(),
                startNodeId, endNodeId)) {
            return forbiddenResponse();
        }
        relationshipService.delete(startNodeId, endNodeId, relType);
        return noContentResponse();
    }

    @GetMapping("/{startNodeId}}/{endNodeId}")
    public List<RelationshipData> getRelationshipsBetweenNodes(
            @PathVariable Long startNodeId, @PathVariable Long endNodeId)
            throws ExceptionWithBriefJSONResponse {
        return relationshipService.getRelationshipBetweenNodes(startNodeId, endNodeId);
    }
}
