package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Role;

import java.util.List;

public interface UserService {
    Page<UserDTO> findAll(String keyword, Pageable pageable);
    UserDTO findById(Long id);
    UserDTO findByUsernameOrEmail(String login);
    void update(UserDTO dto);
    void toggleStatus(Long id);
    void changeRole(Long id, Long roleId);
    void delete(Long id);
    long countUsers();
    List<Role> findAllRoles();
}
