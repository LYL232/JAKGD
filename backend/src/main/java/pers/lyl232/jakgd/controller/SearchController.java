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
import java.util.regex.Pattern;

@RequestMapping("/api/search")
@RestController
public class SearchController extends BaseController {

    final private SearchService service;

    final private Pattern validPattern;

    public SearchController(SearchService service,
                            Pattern validPattern) {
        this.service = service;
        this.validPattern = validPattern;
    }

    @GetMapping("")
    JSONObject getDefault(
            @RequestParam(required = false, defaultValue = "1000") Integer limit) {
        return service.getDefault(Math.min(1000, limit));
    }

    @PostMapping("")
    JSONObject search(
            @RequestBody Map<String, Object> body) throws ExceptionWithBriefJSONResponse {
        try {
            String key = (String) body.get("key");
            if (key == null) {
                throw new ParameterMissingException("key: String");
            }
            Integer limit = (Integer) body.get("limit");
            if (limit == null) {
                limit = 1000;
            }
            Set<String> properties = new HashSet<>();
            String queryPropertiesListString = (String) body.get("queryProperties");
            JSONArray queryPropertiesList;
            if (queryPropertiesListString == null) {
                queryPropertiesList = new JSONArray();
            } else {
                queryPropertiesList = JSONObject.parseArray(queryPropertiesListString);
            }
            for (Object propertyObj : queryPropertiesList) {
                if (!(propertyObj instanceof String)) {
                    throw new ExceptionWithBriefJSONResponse(BriefJSONResponse.Code.JSON_ERROR);
                }
                properties.add((String) propertyObj);
            }
            for (String property : properties) {
                checkIfStringMatchesPattern(validPattern, property);
            }
            return service.getSearchResult(key, properties, limit);
        } catch (ClassCastException exception) {
            throw new InvalidParameterException(
                    body.get("queryProperties").toString());
        }
    }

}
