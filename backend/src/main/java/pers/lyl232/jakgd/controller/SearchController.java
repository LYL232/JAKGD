package pers.lyl232.jakgd.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.*;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.exception.InvalidParameterException;
import pers.lyl232.jakgd.exception.ParameterMissingException;
import pers.lyl232.jakgd.service.SearchService;
import pers.lyl232.jakgd.util.BriefJSONResponse;

import java.util.*;

@RequestMapping("/api/search")
@RestController
public class SearchController extends BaseController {

    final private SearchService service;

    public SearchController(SearchService service) {
        this.service = service;
    }

    @GetMapping("")
    JSONObject getDefault(@RequestParam(required = false, defaultValue = "1000") Long limit) {
        return service.getDefault(Math.min(1000, limit));
    }


    @PostMapping("/node/property")
    JSONObject searchInNodeProperty(
            @RequestBody Map<String, Object> body) throws ExceptionWithBriefJSONResponse {
        try {
            SearchParameter param = new SearchParameter(
                    body, true, true, true, true);
            return service.getSearchInNodePropertyResult(param.key, param.properties, param.skip, param.limit);
        } catch (ClassCastException exception) {
            throw new InvalidParameterException(exception.toString());
        }
    }

    @PostMapping("/node/property/count")
    long searchCountInNodeProperty(
            @RequestBody Map<String, Object> body) throws ExceptionWithBriefJSONResponse {
        try {
            SearchParameter param = new SearchParameter(
                    body, true, false, false, true);
            return service.getSearchInNodePropertyCount(param.key, param.properties);
        } catch (ClassCastException exception) {
            throw new InvalidParameterException(exception.toString());
        }
    }

    @PostMapping("/node/label")
    JSONObject searchInNodeLabel(
            @RequestBody Map<String, Object> body) throws ExceptionWithBriefJSONResponse {
        try {
            SearchParameter param = new SearchParameter(
                    body, true, true, true, false);
            return service.getSearchInNodeLabelResult(param.key, param.skip, param.limit);
        } catch (ClassCastException exception) {
            throw new InvalidParameterException(exception.toString());
        }
    }

    @PostMapping("/node/label/count")
    Long searchCountInNodeLabel(
            @RequestBody Map<String, Object> body) throws ExceptionWithBriefJSONResponse {
        try {
            SearchParameter param = new SearchParameter(
                    body, true, false, false, false);
            return service.getSearchInNodeLabelCount(param.key);
        } catch (ClassCastException exception) {
            throw new InvalidParameterException(exception.toString());
        }
    }

    @PostMapping("/relationship/property")
    JSONObject searchInRelationshipProperty(
            @RequestBody Map<String, Object> body) throws ExceptionWithBriefJSONResponse {
        try {
            SearchParameter param = new SearchParameter(
                    body, true, true, true, true);
            return service.getSearchInRelationshipPropertyResult(
                    param.key, param.properties, param.skip, param.limit);
        } catch (ClassCastException exception) {
            throw new InvalidParameterException(exception.toString());
        }
    }

    @PostMapping("/relationship/property/count")
    Long searchCountInRelationshipProperty(
            @RequestBody Map<String, Object> body) throws ExceptionWithBriefJSONResponse {
        try {
            SearchParameter param = new SearchParameter(
                    body, true, false, false, true);
            return service.getSearchInRelationshipPropertyCount(param.key, param.properties);
        } catch (ClassCastException exception) {
            throw new InvalidParameterException(exception.toString());
        }
    }

    @PostMapping("/relationship/type")
    JSONObject searchInRelationshipType(
            @RequestBody Map<String, Object> body) throws ExceptionWithBriefJSONResponse {
        try {
            SearchParameter param = new SearchParameter(
                    body, true, true, true, false);
            return service.getSearchInRelationshipTypeResult(param.key, param.skip, param.limit);
        } catch (ClassCastException exception) {
            throw new InvalidParameterException(exception.toString());
        }
    }

    @PostMapping("/relationship/type/count")
    Long searchCountInRelationshipType(
            @RequestBody Map<String, Object> body) throws ExceptionWithBriefJSONResponse {
        try {
            SearchParameter param = new SearchParameter(
                    body, true, false, false, false);
            return service.getSearchInRelationshipTypeCount(param.key);
        } catch (ClassCastException exception) {
            throw new InvalidParameterException(exception.toString());
        }
    }


    static class SearchParameter {
        final String key;
        final Long skip;
        final Long limit;
        final Set<String> properties;

        SearchParameter(
                Map<String, Object> body,
                boolean requireKey, boolean requireSkip,
                boolean requireLimit, boolean requireProperties
        ) throws ExceptionWithBriefJSONResponse {
            if (requireKey) {
                key = (String) body.get("key");
                if (key == null || key.isEmpty()) {
                    throw new ParameterMissingException("key: String cannot be empty");
                }
            } else {
                key = null;
            }

            if (requireSkip) {
                Integer skip = (Integer) body.get("skip");
                if (skip == null) {
                    throw new ParameterMissingException("skip: Long cannot be null");
                }
                this.skip = skip.longValue();
            } else {
                this.skip = 0L;
            }

            if (requireLimit) {
                Integer limit = (Integer) body.get("limit");
                if (limit == null) {
                    throw new ParameterMissingException("limit: Long cannot be null");
                }
                this.limit = limit.longValue();
            } else {
                this.limit = 1000L;
            }

            if (requireProperties) {
                Set<String> properties = new HashSet<>();
                String queryPropertiesListString = (String) body.get("queryProperties");
                JSONArray queryPropertiesList;
                if (queryPropertiesListString == null) {
                    queryPropertiesList = new JSONArray();
                } else {
                    queryPropertiesList = JSONObject.parseArray(
                            queryPropertiesListString.replace("\\", ""));
                }
                for (Object propertyObj : queryPropertiesList) {
                    if (!(propertyObj instanceof String)) {
                        throw new ExceptionWithBriefJSONResponse(BriefJSONResponse.Code.JSON_ERROR);
                    }
                    properties.add((String) propertyObj);
                }
                this.properties = properties;
            } else {
                this.properties = null;
            }
        }
    }

}
