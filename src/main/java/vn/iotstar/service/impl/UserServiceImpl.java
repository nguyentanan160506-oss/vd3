package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public Page<UserDTO> findAll(String keyword, Pageable pageable) {
        Page<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.searchUsers(keyword.trim(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(user -> {
            UserDTO dto = userMapper.toDTO(user);
            dto.setProductCount(productRepository.countByUserId(user.getId()));
            return dto;
        });
    }

    @Override
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với ID: " + id));
        UserDTO dto = userMapper.toDTO(user);
        dto.setProductCount(productRepository.countByUserId(user.getId()));
        return dto;
    }

    @Override
    public UserDTO findByUsernameOrEmail(String login) {
        User user = userRepository.findByUsernameOrEmail(login, login)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + login));
        UserDTO dto = userMapper.toDTO(user);
        dto.setProductCount(productRepository.countByUserId(user.getId()));
        return dto;
    }

    @Override
    @Transactional
    public void update(UserDTO dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với ID: " + dto.getId()));

        user.setFullName(dto.getFullName());

        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role không hợp lệ"));
            user.setRole(role);
        }

        if (dto.getAvatarFile() != null && !dto.getAvatarFile().isEmpty()) {
            String newAvatar = cloudinaryService.uploadImage(dto.getAvatarFile());
            user.setImages(newAvatar);
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changeRole(Long id, Long roleId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy role"));
        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public long countUsers() {
        return userRepository.count();
    }

    @Override
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }
}
