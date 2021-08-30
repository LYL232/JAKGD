package pers.lyl232.jakgd.controller.node;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pers.lyl232.jakgd.controller.BaseController;
import pers.lyl232.jakgd.entity.result.NodeData;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.exception.ObjectNotFoundException;
import pers.lyl232.jakgd.service.node.NodeService;
import pers.lyl232.jakgd.service.node.UserService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@RequestMapping("/api/node")
@RestController
public class NodeController extends BaseController {

    final private NodeService nodeService;

    final private UserService userService;

    final private Pattern validLabelPattern;

    NodeController(
            NodeService nodeService,
            UserService userService,
            Pattern validLabelPattern) {
        this.validLabelPattern = validLabelPattern;
        this.nodeService = nodeService;
        this.userService = userService;
    }

    @PostMapping("/")
    public Long create(@RequestBody Map<String, Object> body)
            throws ExceptionWithBriefJSONResponse {
        Set<String> labels = getLabelsFromBodyAndRemove(body, validLabelPattern);
        Map<String, String> properties = getBodyProperties(body, validLabelPattern);
        return nodeService.create(getAuthenticatedUsername(), labels, properties);
    }

    @GetMapping("/{id}")
    public NodeData get(@PathVariable Long id) throws ObjectNotFoundException {
        return nodeService.getNotNull(id);
    }

    @GetMapping("/find")
    public List<NodeData> getList(@RequestParam List<Long> idList) {
        return nodeService.getNodesById(idList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id, @RequestBody Map<String, Object> body)
            throws ExceptionWithBriefJSONResponse {
        if (!userService.hasNodeModifyPermission(getAuthenticatedUsername(), id)) {
            return forbiddenResponse();
        }
        Set<String> labels = getLabelsFromBodyAndRemove(body, validLabelPattern);
        Map<String, String> properties = getBodyProperties(body, validLabelPattern);
        NodeData node = nodeService.getNotNull(id);
        // 内部节点不允许默认的删除方法
        for (String label : node.labels) {
            if (label.startsWith("__")) {
                return wrongRequestResponse("please try specific method");
            }
        }
        nodeService.update(node.id, properties, labels);
        return noContentResponse();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id)
            throws ExceptionWithBriefJSONResponse {
        NodeData node = nodeService.getNotNull(id);
        if (!userService.hasNodeDeletePermission(getAuthenticatedUsername(), id)) {
            return forbiddenResponse();
        }
        // 某些节点由于特殊的结构不允许默认的删除方法
        for (String label : node.labels) {
            if (label.equals("__document")) {
                return wrongRequestResponse("please try specific method");
            }
        }
        nodeService.delete(node);
        return noContentResponse();
    }

    @GetMapping("/{id}/neighborhood")
    public JSONObject getNeighborhood(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1000") Long limit)
            throws ObjectNotFoundException {
        return nodeService.getNeighbourhood(id, Math.min(limit, 1000), false);
    }

    @GetMapping("/{id}/neighborhood/direct")
    public JSONObject getDirectNeighborhood(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1000") Long limit)
            throws ObjectNotFoundException {
        return nodeService.getNeighbourhood(id, Math.min(limit, 1000), true);
    }
}
