package vn.iotstar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class CustomLoginSecurityTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("TEST 1: Trang /login hiển thị thành công form đăng nhập")
    void testLoginPageDisplay() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Username hoặc Email")))
                .andExpect(content().string(containsString("Mật khẩu")))
                .andExpect(content().string(containsString("Đăng nhập")));
    }

    @Test
    @DisplayName("TEST 2: Login thành công bằng USERNAME (admin)")
    void testLoginWithUsernameSuccess() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("admin")
                        .password("123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername("admin"));
    }

    @Test
    @DisplayName("TEST 3: Logout thành công, session bị hủy, redirect về /login?logout=true")
    void testLogoutSuccess() throws Exception {
        mockMvc.perform(logout("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("TEST 4: Login thành công bằng EMAIL (admin@gmail.com)")
    void testLoginWithEmailSuccess() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("admin@gmail.com")
                        .password("123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername("admin"));
    }

    @Test
    @DisplayName("TEST 4b: Login thành công bằng EMAIL của USER (user01@gmail.com)")
    void testLoginWithUserEmailSuccess() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("user01@gmail.com")
                        .password("123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername("user01"));
    }

    @Test
    @DisplayName("TEST 6: Sai password -> Đăng nhập thất bại, redirect /login?error=true")
    void testLoginWrongPassword() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("admin")
                        .password("wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("TEST 7: Sai username/email -> Đăng nhập thất bại, redirect /login?error=true")
    void testLoginWrongUsernameOrEmail() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("nonexistent_user@gmail.com")
                        .password("123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("TEST 8: USER không được phép truy cập /admin/** (bị chặn 403 / Access Denied)")
    @WithMockUser(username = "user01", roles = {"USER"})
    void testUserAccessAdminForbidden() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TEST 9: ADMIN được phép truy cập /admin/** (200 OK)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminAccessAdminSuccess() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TEST 5: Khi chưa login, Header chỉ hiển thị nút Đăng nhập")
    void testHeaderWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Đăng nhập")));
    }

    @Test
    @DisplayName("TEST 5b: Khi đã đăng nhập, Header hiển thị đủ Avatar, Fullname, Username, Email, Role và nút Đăng xuất")
    void testHeaderWhenAuthenticatedWithCustomUserDetails() throws Exception {
        vn.iotstar.security.CustomUserDetails userDetails = new vn.iotstar.security.CustomUserDetails(
                1L,
                "admin",
                "admin@gmail.com",
                "123456",
                "Administrator",
                "/images/avatar-default.png",
                "ROLE_ADMIN",
                true
        );
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

        mockMvc.perform(get("/admin").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Administrator")))
                .andExpect(content().string(containsString("(@admin)")))
                .andExpect(content().string(containsString("admin@gmail.com")))
                .andExpect(content().string(containsString("ROLE_ADMIN")))
                .andExpect(content().string(containsString("avatar-default.png")))
                .andExpect(content().string(containsString("Đăng xuất")));
    }
}
