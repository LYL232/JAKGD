package pers.lyl232.jakgd.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.exception.ForbiddenException;
import pers.lyl232.jakgd.util.BriefJSONResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
public class FileService extends BaseService {
    final static private Logger logger = LoggerFactory.getLogger(FileService.class);

    @Value("${upload.path}")
    private String uploadDir;

    /**
     * 处理并保存上传文件, 保存的文件名为文件的MD5值, 使用多重哈希避免重复值
     *
     * @param file     文件对象
     * @param username 用户名
     * @return 相对路径url
     * @throws ExceptionWithBriefJSONResponse 空文件或者无法创建文件
     */
    public String upload(MultipartFile file, String username)
            throws ExceptionWithBriefJSONResponse {
        try {
            if (file.isEmpty() || file.getOriginalFilename() == null ||
                    file.getOriginalFilename().isEmpty()) {
                throw new ExceptionWithBriefJSONResponse(BriefJSONResponse.Code.EMPTY_FILE);
            }

            String originalFilename = file.getOriginalFilename(),
                    suffixName = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String dirPath = String.format("%s/%s/%s",
                    uploadDir, username, getFormatNowDateStringByMonth());

            File dir = new File(dirPath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    logger.error("mkdirs failed when uploading file: " + dir.getPath());
                    throw new ExceptionWithBriefJSONResponse(BriefJSONResponse.Code.FILE_HANDLE_ERROR);
                }
            }

            String filename = DigestUtils.md5DigestAsHex(
                    (dir + file.getOriginalFilename()).getBytes()) + suffixName;
            File toSave = new File(dir, filename);
            while (toSave.exists()) {
                filename = DigestUtils.md5DigestAsHex(filename.getBytes()) + suffixName;
                toSave = new File(dir, filename);
            }
            file.transferTo(toSave);
            return (dirPath + "/" + filename).replace("\\", "/")
                    .replace(uploadDir, "");
        } catch (IOException exception) {
            throw new ExceptionWithBriefJSONResponse(BriefJSONResponse.Code.FILE_HANDLE_ERROR);
        }
    }

    /**
     * 删除用户上传的指定路径的文件, 要求文件在用户的目录下
     *
     * @param username 用户
     * @param path 需要删除的路径
     * @throws ExceptionWithBriefJSONResponse 文件找不到, 文件处理失败带响应异常
     */
    public void delete(String username, String path)
            throws ExceptionWithBriefJSONResponse {
        if (!path.startsWith("/" + username) && !path.startsWith(username)) {
            throw new ForbiddenException();
        }
        File file = new File(String.format("%s/%s", uploadDir, path));
        if (!file.exists()) {
            throw new ExceptionWithBriefJSONResponse(BriefJSONResponse.Code.FILE_NOT_EXIST);
        }
        if (!file.delete()) {
            logger.error("file deletion failed: " + file.getPath());
            throw new ExceptionWithBriefJSONResponse(
                    BriefJSONResponse.Code.FILE_HANDLE_ERROR
            );
        }
        // 如果父目录已经为空, 则删除父目录
        File dir = file.getParentFile();
        File[] files = dir.listFiles();
        if (files == null || files.length == 0 || files[0].equals(file)) {
            if (!dir.delete()) {
                logger.error("empty dir deletion failed: " + dir.getPath());
            }
        }
    }

    /**
     * 获取当前登录用户所有的文件路径
     *
     * @param username 用户名
     * @return 所有该用户已经上传的文件路径
     */
    @GetMapping("")
    public List<String> getAllFiles(String username) {
        File dir = new File(String.format("%s/%s", uploadDir, username));
        List<String> filePaths = new LinkedList<>();
        List<File> files = getDirAllFiles(dir);
        for (File file : files) {
            String path = file.getPath().replace("\\", "/");
            if (!path.startsWith(uploadDir)) {
                continue;
            }
            path = path.replace(uploadDir, "");
            filePaths.add(path);
        }
        return filePaths;
    }

    /**
     * 获取目标目录下所有的文件
     *
     * @param root 目标根目录
     * @return 目标根目录下所有的文件列表
     */
    private List<File> getDirAllFiles(File root) {
        List<File> res = new ArrayList<>();
        if (!root.exists()) {
            return res;
        }
        // BFS
        Queue<File> dirs = new LinkedList<>();
        dirs.add(root);
        while (!dirs.isEmpty()) {
            File file = dirs.poll();
            if (!file.exists()) {
                continue;
            }
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                continue;
            }
            for (File eachFile : files) {
                if (eachFile.isDirectory()) {
                    dirs.add(eachFile);
                } else {
                    res.add(eachFile);
                }
            }
        }
        return res;
    }

}
