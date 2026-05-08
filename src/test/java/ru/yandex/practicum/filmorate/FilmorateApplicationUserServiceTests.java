package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.expection.DatabaseException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(UserService.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmorateApplicationUserServiceTests {

    @Autowired
    private final UserService userService;

    @Test
    public void testGetUserById_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userService.createUser(user);

        User foundUser = userService.getUserById(createdUser.getId());

        assertThat(foundUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", createdUser.getId())
                .hasFieldOrPropertyWithValue("email", "test@example.com")
                .hasFieldOrPropertyWithValue("login", "testlogin")
                .hasFieldOrPropertyWithValue("name", "Test Name")
                .extracting(User::getBirthday)
                .isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    public void testGetUserById_UserNotFound_ThrowsDatabaseException() {
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Такого юзера нет в списке!");
    }

    @Test
    public void testMakeFriendship_Success() {
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");

        User updatedUser = userService.makeFriendship(user1.getId(), user2.getId());

        assertThat(updatedUser.getFriends())
                .contains(user2.getId());
    }

    @Test
    public void testMakeFriendship_UserNotFound_ThrowsNotFoundException() {
        User user = createTestUser("test@example.com", "test", "Test User");


        assertThatThrownBy(() -> userService.makeFriendship(999L, user.getId()))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Такого юзера нет в списке!");
    }

    @Test
    public void testMakeFriendship_FriendNotFound_ThrowsNotFoundException() {
        User user = createTestUser("test@example.com", "test", "Test User");

        assertThatThrownBy(() -> userService.makeFriendship(user.getId(), 999L))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Такого юзера нет в списке!");
    }

    @Test
    public void testDeleteFriendship_Success() {
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");
        userService.makeFriendship(user1.getId(), user2.getId());

        User updatedUser = userService.deleteFriendship(user1.getId(), user2.getId());

        assertThat(updatedUser.getFriends())
                .doesNotContain(user2.getId());
    }

    @Test
    public void testDeleteFriendship_UserNotFound_ThrowsNotFoundException() {
        User user = createTestUser("test@example.com", "test", "Test User");

        assertThatThrownBy(() -> userService.deleteFriendship(999L, user.getId()))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Такого юзера нет в списке!");
    }

    @Test
    public void testDeleteFriendship_FriendNotFound_ThrowsNotFoundException() {
        User user = createTestUser("test@example.com", "test", "Test User");

        assertThatThrownBy(() -> userService.deleteFriendship(user.getId(), 999L))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Такого юзера нет в списке!");
    }

    @Test
    public void testListOfFriends_Success() {
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");
        userService.makeFriendship(user1.getId(), user2.getId());

        Collection<User> friends = userService.listOfFriends(user1.getId());

        assertThat(friends)
                .isNotEmpty()
                .extracting("id")
                .containsExactly(user2.getId());
    }

    @Test
    public void testListOfFriends_UserNotFound_ThrowsDatabaseException() {
        assertThatThrownBy(() -> userService.listOfFriends(999L))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Такого юзера нет в списке!");
    }

    @Test
    public void testListOfCommonFriends_Success() {
        User user1 = createTestUser("user1@example.com", "user1", "User One");
        User user2 = createTestUser("user2@example.com", "user2", "User Two");
        User commonFriend = createTestUser("friend@example.com", "friend", "Common Friend");

        userService.makeFriendship(user1.getId(), commonFriend.getId());
        userService.makeFriendship(user2.getId(), commonFriend.getId());

        Collection<User> commonFriends = userService.listOfCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends)
                .isNotEmpty()
                .extracting("id")
                .containsExactly(commonFriend.getId());
    }

    @Test
    public void testListOfCommonFriends_UserNotFound_ThrowsNotFoundException() {
        User user = createTestUser("test@example.com", "test", "Test User");

        assertThatThrownBy(() -> userService.listOfCommonFriends(999L, user.getId()))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Такого юзера нет в списке!");
    }

    @Test
    public void testGetAllUsers_Success() {
        createTestUser("user1@example.com", "user1", "User One");
        createTestUser("user2@example.com", "user2", "User Two");

        Collection<User> allUsers = userService.getAllUsers();
        assertThat(allUsers)
                .isNotEmpty()
                .hasSize(2)
                .extracting("email")
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    @Test
    public void testCreateUser_Success() {
        User user = new User();
        user.setEmail("newuser@example.com");
        user.setLogin("newlogin");
        user.setName("New User");
        user.setBirthday(LocalDate.of(1995, 5, 15));

        User createdUser = userService.createUser(user);

        assertThat(createdUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("email", "newuser@example.com")
                .hasFieldOrPropertyWithValue("login", "newlogin")
                .hasFieldOrPropertyWithValue("name", "New User")
                .extracting("birthday")
                .isEqualTo(LocalDate.of(1995, 5, 15));

        assertThat(createdUser.getId()).isNotNull();
    }

    @Test
    public void testUpdateUser_Success() {
        User user = createTestUser("update@example.com", "update", "Update User");

        user.setName("Updated Name");
        user.setEmail("updated@example.com");

        User updatedUser = userService.updateUser(user);

        assertThat(updatedUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", user.getId())
                .hasFieldOrPropertyWithValue("name", "Updated Name")
                .hasFieldOrPropertyWithValue("email", "updated@example.com");
    }

    private User createTestUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userService.createUser(user);
    }
}
