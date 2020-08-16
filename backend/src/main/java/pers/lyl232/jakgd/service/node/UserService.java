package pers.lyl232.jakgd.service.node;

import org.neo4j.driver.exceptions.ClientException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.lyl232.jakgd.entity.node.Permission;
import pers.lyl232.jakgd.entity.node.User;
import pers.lyl232.jakgd.exception.ExceptionWithBriefJSONResponse;
import pers.lyl232.jakgd.exception.ObjectNotFoundException;
import pers.lyl232.jakgd.repository.node.NodeRepository;
import pers.lyl232.jakgd.repository.node.PermissionRepository;
import pers.lyl232.jakgd.repository.node.UserRepository;
import pers.lyl232.jakgd.util.BriefJSONResponse;

import java.util.List;
import java.util.Set;

@Service
public class UserService {
    final private UserRepository userRepository;

    final private PermissionRepository permissionRepository;

    final private NodeRepository nodeRepository;

    public UserService(
            UserRepository userRepository,
            NodeRepository nodeRepository,
            PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.nodeRepository = nodeRepository;
    }

    /**
     * 注册用户服务
     *
     * @param username 用户名
     * @param password 密码(已经加密)
     * @throws ExceptionWithBriefJSONResponse 用户名被占用异常
     */
    public void signUp(String username, String password)
            throws ExceptionWithBriefJSONResponse {
        try {
            userRepository.createUserNode(username, password);
        } catch (ClientException | DataIntegrityViolationException exception) {
            if (exception.getMessage() != null
                    && exception.getMessage().contains("already exists")) {
                throw new ExceptionWithBriefJSONResponse(BriefJSONResponse.Code.USERNAME_OCCUPIED,
                        username);
            }
            throw exception;
        }
    }

    /**
     * 通过用户名查找用户服务, 找不到则返回null
     *
     * @param username 用户名
     * @return 用户对象
     */
    public User findByUsernameNotNull(String username) throws ObjectNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ObjectNotFoundException("user username: " + username);
        }
        return userRepository.findByUsername(username);
    }

    /**
     * 更新用户
     *
     * @param user 用户对象
     */
    public void update(User user) {
        userRepository.save(user);
    }

    public Set<Permission> getPermissions(String username) {
        return permissionRepository.getUserPermissions(username);
    }

    /**
     * 检查用户是否有某权限
     *
     * @param username   用户名
     * @param permission 权限
     * @return 是否有
     */
    public boolean hasPermission(String username, String permission) {
        return permissionRepository.userHasPermission(username, permission);
    }

    /**
     * 查用户是否拥有一节点的修改权限
     *
     * @param username 用户名
     * @param nodeId   节点id
     * @return 是否有修改权限
     */
    @Transactional(readOnly = true)
    public boolean hasNodeModifyPermission(String username, Long nodeId) {
        Set<Permission> userPermissions = permissionRepository.getUserPermissions(username);
        if (userPermissions.contains(Permission.MODIFY_ALL_NODE)) {
            return true;
        }
        if (userPermissions.contains(Permission.MODIFY_OWN_NODE)) {
            String author = nodeRepository.getNodeAuthor(nodeId);
            if (author == null || !author.equals(username)) {
                return false;
            }
        }
        return false;
    }

    /**
     * 查用户是否拥有一节点的删除权限
     *
     * @param username 用户名
     * @param nodeId   节点id
     * @return 是否有删除权限
     */
    @Transactional(readOnly = true)
    public boolean hasNodeDeletePermission(String username, Long nodeId) {
        Set<Permission> userPermissions = permissionRepository.getUserPermissions(username);
        if (userPermissions.contains(Permission.DELETE_ALL_NODE)) {
            return true;
        }
        if (userPermissions.contains(Permission.DELETE_OWN_NODE)) {
            String author = nodeRepository.getNodeAuthor(nodeId);
            if (author == null || !author.equals(username)) {
                return false;
            }
        }
        return false;
    }

    /**
     * 查用户是否拥有一系列节点的修改权限
     *
     * @param username 用户名
     * @param idList   一系列节点id
     * @return 是否有修改权限
     */
    @Transactional(readOnly = true)
    public boolean hasNodesModifyPermission(String username, Long... idList) {
        if (idList.length < 1) {
            return false;
        }
        Set<Permission> userPermissions = permissionRepository.getUserPermissions(username);
        if (userPermissions.contains(Permission.MODIFY_ALL_NODE)) {
            return true;
        }
        if (userPermissions.contains(Permission.MODIFY_OWN_NODE)) {
            List<String> authors = nodeRepository.getNodesAuthor(idList);
            if (authors == null) {
                return false;
            }
            for (String author : authors) {
                if (author == null || !author.equals(username)) {
                    return false;
                }
            }
        }
        return false;
    }

}
