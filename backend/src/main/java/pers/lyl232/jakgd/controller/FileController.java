package pers.lyl232.jakgd.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.service.FileService;

import java.util.List;

@RequestMapping("/api/file")
@RestController
public class FileController extends BaseController {
    final private FileService service;

    public FileController(FileService service) {
        this.service = service;
    }

    /**
     * 上传文件接口, multipart file, http头为 multipart/form-data
     *
     * @param file 文件对象
     * @return 上传成功的文件路径
     * @throws ExceptionWithBriefJSONResponse 认证失败, 文件处理失败等带响应异常
     */
    @PostMapping("")
    public ResponseEntity<String> upload(@RequestParam() MultipartFile file)
            throws ExceptionWithBriefJSONResponse {
        return new ResponseEntity<>(
                service.upload(file, getAuthenticatedUsername()),
                HttpStatus.CREATED
        );
    }

    /**
     * 获取当前登录用户所有的文件路径
     *
     * @return 所有该用户已经上传的文件路径
     * @throws ExceptionWithBriefJSONResponse 认证失败
     */
    @GetMapping("")
    public List<String> getAllFiles() throws ExceptionWithBriefJSONResponse {
        return service.getAllFiles(getAuthenticatedUsername());
    }

    /**
     * 删除指定路径的文件, 要求文件在用户的目录下
     *
     * @param path 需要删除的路径
     * @return 空body OK
     * @throws ExceptionWithBriefJSONResponse 认证失败, 文件找不到, 文件处理失败带响应异常
     */
    @DeleteMapping("")
    public ResponseEntity<Object> delete(@RequestParam() String path)
            throws ExceptionWithBriefJSONResponse {
        service.delete(getAuthenticatedUsername(), path);
        return noContentResponse();
    }

}
