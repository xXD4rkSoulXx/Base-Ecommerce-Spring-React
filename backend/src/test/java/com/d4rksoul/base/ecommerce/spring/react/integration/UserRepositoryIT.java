package com.d4rksoul.base.ecommerce.spring.react.integration;

import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.UserSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User addUser(String email, String firstName, String lastName, UserRole role) {
        User user = TestData.user();

        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);

        return persist(user);
    }

    private User addUser(String email, UserRole role) {
        return addUser(email, "First", "Last", role);
    }

    private void setCreatedAt(User user, Instant createdAt) {
        jdbcTemplate.update(
            "update users set created_at = ? where id = ?",
            Timestamp.from(createdAt),
            user.getId()
        );
    }

    private static List<Long> idsOf(List<User> users) {
        return users.stream().map(User::getId).toList();
    }

    private List<Long> search(Specification<User> specification) {
        return idsOf(userRepository.findAll(specification));
    }

    @Test
    @DisplayName("by email: exists and find match the exact email only")
    void existsAndFindByEmail() {
        User user = addUser("ana@mail.com", UserRole.CUSTOMER);
        flushAndClear();

        assertThat(userRepository.existsByEmail("ana@mail.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@mail.com")).isFalse();
        assertThat(userRepository.findByEmail("ana@mail.com")).get().extracting(User::getId)
            .isEqualTo(user.getId());
        assertThat(userRepository.findByEmail("other@mail.com")).isEmpty();
    }

    @Test
    @DisplayName("find by id and role: the user must have that role")
    void findByIdAndRole() {
        User support = addUser("support@mail.com", UserRole.SUPPORT);
        flushAndClear();

        assertThat(userRepository.findByIdAndRole(support.getId(), UserRole.SUPPORT)).isPresent();
        assertThat(userRepository.findByIdAndRole(support.getId(), UserRole.CUSTOMER)).isEmpty();
        assertThat(userRepository.findByIdAndRole(-1L, UserRole.SUPPORT)).isEmpty();
    }

    @Test
    @DisplayName("find by role not: leaves out that role and sorts by role, then email")
    void findByRoleNotOrderByRoleAscEmailAsc() {
        User supportB = addUser("b@mail.com", UserRole.SUPPORT);
        User adminZ = addUser("z@mail.com", UserRole.ADMIN);
        User adminA = addUser("a@mail.com", UserRole.ADMIN);
        addUser("customer@mail.com", UserRole.CUSTOMER);
        flushAndClear();

        List<User> found = userRepository.findByRoleNotOrderByRoleAscEmailAsc(UserRole.CUSTOMER);

        assertThat(idsOf(found)).containsExactly(adminA.getId(), adminZ.getId(), supportB.getId());
    }

    @Test
    @DisplayName("spec search: matches the email ignoring case")
    void searchMatchesEmail() {
        User ana = addUser("ana@mail.com", "Ana", "Costa", UserRole.CUSTOMER);
        addUser("rui@mail.com", "Rui", "Lopes", UserRole.CUSTOMER);
        flushAndClear();

        assertThat(search(UserSpecification.containsNameOrEmail(" ANA@ "))).containsExactly(ana.getId());
    }

    @Test
    @DisplayName("spec search: matches the full name, across first and last name")
    void searchMatchesFullName() {
        addUser("ana@mail.com", "Ana", "Costa", UserRole.CUSTOMER);
        User rui = addUser("rui@mail.com", "Rui", "Lopes", UserRole.CUSTOMER);
        flushAndClear();

        assertThat(search(UserSpecification.containsNameOrEmail("rui lo"))).containsExactly(rui.getId());
    }

    @Test
    @DisplayName("spec search: a blank text does not filter")
    void blankSearchDoesNotFilter() {
        addUser("ana@mail.com", UserRole.CUSTOMER);
        addUser("rui@mail.com", UserRole.CUSTOMER);
        flushAndClear();

        assertThat(search(UserSpecification.containsNameOrEmail("  "))).hasSize(2);
        assertThat(search(UserSpecification.containsNameOrEmail(null))).hasSize(2);
    }

    @Test
    @DisplayName("spec hasRole: only the users with that role")
    void hasRole() {
        addUser("customer@mail.com", UserRole.CUSTOMER);
        User manager = addUser("manager@mail.com", UserRole.MANAGER);
        flushAndClear();

        assertThat(search(UserSpecification.hasRole(UserRole.MANAGER))).containsExactly(manager.getId());
    }

    @Test
    @DisplayName("spec orderByRecentDesc: newest user first")
    void orderByRecentDesc() {
        Instant now = Instant.now();
        User oldest = addUser("a@mail.com", UserRole.CUSTOMER);
        User newest = addUser("b@mail.com", UserRole.CUSTOMER);
        User middle = addUser("c@mail.com", UserRole.CUSTOMER);
        setCreatedAt(oldest, now.minus(3, ChronoUnit.DAYS));
        setCreatedAt(newest, now.minus(1, ChronoUnit.DAYS));
        setCreatedAt(middle, now.minus(2, ChronoUnit.DAYS));
        flushAndClear();

        List<Long> found = search(UserSpecification.orderByRecentDesc());

        assertThat(found).containsExactly(newest.getId(), middle.getId(), oldest.getId());
    }
}
