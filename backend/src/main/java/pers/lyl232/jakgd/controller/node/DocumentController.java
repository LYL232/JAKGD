package pers.lyl232.jakgd.controller.node;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pers.lyl232.jakgd.controller.BaseController;
import pers.lyl232.jakgd.entity.node.Document;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.exception.InvalidParameterException;
import pers.lyl232.jakgd.exception.ObjectNotFoundException;
import pers.lyl232.jakgd.exception.ParameterMissingException;
import pers.lyl232.jakgd.service.node.DocumentService;
import pers.lyl232.jakgd.service.node.UserService;

import java.util.List;
import java.util.Map;

@RequestMapping("/api/document")
@RestController
public class DocumentController extends BaseController {

    final private DocumentService documentService;

    final private UserService userService;

    public DocumentController(
            DocumentService documentService,
            UserService userService) {
        this.documentService = documentService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public Document get(@PathVariable Long id) throws ObjectNotFoundException {
        return documentService.getNotNull(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id, @RequestBody Map<String, Object> body)
            throws ExceptionWithBriefJSONResponse {
        Document document = documentService.getNotNull(id);
        if (!userService.hasNodeModifyPermission(getAuthenticatedUsername(), id)) {
            return forbiddenResponse();
        }
        Object contentObject = body.get("content"),
                nameObject = body.get("name");
        if (nameObject == null && contentObject == null) {
            return noContentResponse();
        }
        if (contentObject != null) {
            String content = (String) contentObject;
            if (content.isEmpty()) {
                return invalidParameterResponse("content: String not empty");
            }
            document.setContent(content);
        }
        if (nameObject != null) {
            String name = (String) nameObject;
            if (name.isEmpty()) {
                return invalidParameterResponse("name: String not empty");
            }
            document.setName(name);
        }
        documentService.update(document);
        return noContentResponse();
    }

    @DeleteMapping("/{dId}")
    public ResponseEntity<Object> delete(@PathVariable Long dId)
            throws ExceptionWithBriefJSONResponse {
        Document document = documentService.getNotNull(dId);
        if (!userService.hasNodeDeletePermission(getAuthenticatedUsername(), dId)) {
            return forbiddenResponse();
        }
        documentService.delete(document);
        return noContentResponse();
    }

    @GetMapping("/partOf/{nodeId}")
    public List<Document> getNodeDocuments(@PathVariable Long nodeId) throws ObjectNotFoundException {
        return documentService.getNodeDocuments(nodeId);
    }

    @PostMapping("/{type}/{docName}/partOf/{nodeId}")
    public ResponseEntity<Object> attachNewDocument(
            @PathVariable String type,
            @PathVariable String docName,
            @PathVariable Long nodeId,
            @RequestBody Map<String, Object> body
    ) throws ExceptionWithBriefJSONResponse {
        if (docName == null || docName.isEmpty()) {
            return invalidParameterResponse("docName: String not empty");
        }
        String content = (String) body.get("content"),
                username = getAuthenticatedUsername();
        if (!userService.hasNodeModifyPermission(username, nodeId)) {
            return forbiddenResponse();
        }
        if (type.equals("markdown")) {
            if (content.isEmpty()) {
                return notAllowEmptyBodyResponse();
            }
            return new ResponseEntity<>(documentService.create(nodeId, username, docName, content), HttpStatus.CREATED);
        } else {
            return invalidParameterResponse("unknown Document type: " + type);
        }
    }

    @PostMapping("/{dId}/partOf/{nodeId}/switch")
    public ResponseEntity<Object> switchDocument(
            @PathVariable Long dId,
            @PathVariable Long nodeId,
            @RequestParam String type)
            throws ExceptionWithBriefJSONResponse {
        if (!userService.hasNodesModifyPermission(
                getAuthenticatedUsername(), dId, nodeId)) {
            return forbiddenResponse();
        }
        switch (type) {
            case "downward": {
                documentService.switchDocumentDownward(nodeId, dId);
                break;
            }
            case "upward": {
                documentService.switchDocumentUpward(nodeId, dId);
                break;
            }
            default: {
                return invalidParameterResponse("unknown Document type: " + type);
            }
        }
        return noContentResponse();
    }

    @GetMapping("/mine/contain")
    Long searchCountInMineDocumentContains(@RequestParam String key) throws ExceptionWithBriefJSONResponse {
        try {
            if (key == null || key.isEmpty()) {
                throw new ParameterMissingException("key: String cannot be empty");
            }
            return documentService.getUserDocumentContain(getAuthenticatedUsername(), key);
        } catch (ClassCastException exception) {
            throw new InvalidParameterException(exception.toString());
        }
    }

}
