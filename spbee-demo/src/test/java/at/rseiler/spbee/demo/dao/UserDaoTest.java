package at.rseiler.spbee.demo.dao;

import at.rseiler.spbee.core.exception.MultipleObjectsReturned;
import at.rseiler.spbee.core.exception.ObjectDoesNotExist;
import at.rseiler.spbee.demo.entity.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;


public class UserDaoTest extends AbstractTest {

    @Autowired
    public UserDao userDao;

    @Test
    public void testGetUsers() throws Exception {
        assertThat(userDao.getUsers().size(), is(5));
    }

    @Test
    public void testGetUser() throws Exception {
        User user = userDao.getUser(2);
        assertThat(user.getId(), is(2));
        assertThat(user.getName(), is("user2"));
        assertThat(user.getCreated(), is(LocalDateTime.of(2016, 4, 24, 17, 30, 15)));
        assertThat(user.getPermissions().size(), is(0));
    }

    @Test
    public void testGetUserOptional() throws Exception {
        User user = userDao.getUserOptional(2).get();
        assertThat(user.getId(), is(2));
        assertThat(user.getName(), is("user2"));
        assertThat(user.getCreated(), is(LocalDateTime.of(2016, 4, 24, 17, 30, 15)));
        assertThat(user.getPermissions().size(), is(0));
    }

    @Test
    public void testGetUserOptionalEmpty() throws Exception {
        Optional<User> user = userDao.getUserOptional(-1);
        assertFalse(user.isPresent());
    }

    @Test
    public void testGetSimpleUser() throws Exception {
        User user = userDao.getSimpleUserMappingConstructor(2);
        assertThat(user.getId(), is(2));
        assertThat(user.getName(), is("-"));
        assertThat(user.getPermissions().size(), is(0));
    }

    @Test
    public void testGetUserWithPermissions() throws Exception {
        User user = userDao.getUserWithPermissions(1).getUser().get();
        assertThat(user.getId(), is(1));
        assertThat(user.getName(), is("not loaded"));
        assertThat(user.getPermissions().size(), is(2));
    }

    @Test
    public void testGetSimpleUsersWithMappingConstructor() throws Exception {
        List<User> users = userDao.getSimpleUsersWithMappingConstructor();
        assertThat(users.size(), is(5));
        assertThat(users.get(0).getId(), is(1));
        assertThat(users.get(0).getName(), is("-"));
    }

    @Test
    @DirtiesContext
    public void testSaveUser() throws Exception {
        userDao.saveUser(5, "saved-user", new Timestamp(System.currentTimeMillis()));
        assertThat(userDao.getUser(5).getName(), is("saved-user"));

        List<User> users = userDao.getSimpleUsersWithMappingConstructor();
        assertThat(users.size(), is(6));
    }

    @Test(expected = ObjectDoesNotExist.class)
    public void testObjectDoesNotExist() {
        userDao.getUser(-1);
    }

    @Test(expected = MultipleObjectsReturned.class)
    public void testMultipleObjectsReturned() {
        userDao.getUser(4);
    }

}