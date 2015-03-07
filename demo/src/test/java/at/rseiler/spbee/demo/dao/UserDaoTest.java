package at.rseiler.spbee.demo.dao;

import at.rseiler.spbee.core.exception.MultipleObjectsReturned;
import at.rseiler.spbee.core.exception.ObjectDoesNotExist;
import at.rseiler.spbee.demo.entity.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


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
        assertThat(user.getPermissions().size(), is(0));
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
    public void testSaveUser() throws Exception {
        userDao.saveUser(5, "saved-user");
        assertThat(userDao.getUser(5).getName(), is("saved-user"));
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