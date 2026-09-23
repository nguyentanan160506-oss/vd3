package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.iotstar.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username) OR LOWER(u.email) = LOWER(:email)")
    Optional<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    default Optional<User> findByUsernameOrEmail(String login) {
        return findByUsernameOrEmail(login, login);
    }

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    @Query("""
            SELECT u FROM User u
            JOIN FETCH u.role
            WHERE LOWER(u.email) = LOWER(:email)
            """)
    Optional<User> findByEmailWithRole(@Param("email") String email);

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%'))
               OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%'))
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :kw, '%'))
            """)
    Page<User> searchUsers(@Param("kw") String keyword, Pageable pageable);

    long countByRoleId(Long roleId);
}
