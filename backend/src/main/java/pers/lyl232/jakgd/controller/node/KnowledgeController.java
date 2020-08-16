package pers.lyl232.jakgd.controller.node;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pers.lyl232.jakgd.controller.BaseController;
import pers.lyl232.jakgd.entity.node.Knowledge;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.exception.ObjectNotFoundException;
import pers.lyl232.jakgd.service.node.KnowledgeService;
import pers.lyl232.jakgd.service.node.NodeService;
import pers.lyl232.jakgd.service.node.UserService;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@RequestMapping("/api/knowledge")
@RestController
public class KnowledgeController extends BaseController {

    final private KnowledgeService knowledgeService;

    final private UserService userService;

    final private NodeService nodeService;

    final private Pattern validLabelPattern;

    public KnowledgeController(
            KnowledgeService knowledgeService,
            UserService userService,
            NodeService nodeService,
            Pattern validLabelPattern) {
        this.knowledgeService = knowledgeService;
        this.userService = userService;
        this.nodeService = nodeService;
        this.validLabelPattern = validLabelPattern;
    }

    @GetMapping("/{id}")
    public Object get(@PathVariable Long id) throws ObjectNotFoundException {
        return knowledgeService.getNotNull(id);
    }

    @PostMapping("")
    public Long create(@RequestBody Map<String, Object> body)
            throws ExceptionWithBriefJSONResponse {
        Set<String> labels = getLabelsFromBodyAndRemove(body, validLabelPattern);
        Map<String, String> properties = getBodyProperties(body, validLabelPattern);
        requireBodyNotEmptyParameters(properties, "name", "summary");
        labels.add("__knowledge");
        return nodeService.create(getAuthenticatedUsername(),
                labels, properties, false);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body)
            throws ExceptionWithBriefJSONResponse {
        Set<String> labels = getLabelsFromBodyAndRemove(body, validLabelPattern);
        Map<String, String> properties = getBodyProperties(body, validLabelPattern);
        requireBodyNotEmptyParameters(properties, "name", "summary");
        Knowledge knowledge = knowledgeService.getNotNull(id);
        if (!userService.hasNodeModifyPermission(getAuthenticatedUsername(), id)) {
            return forbiddenResponse();
        }
        labels.add("__knowledge");
        nodeService.update(knowledge.getId(), properties, labels, false);
        return noContentResponse();
    }
}
