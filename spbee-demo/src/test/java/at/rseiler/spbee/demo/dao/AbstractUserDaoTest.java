package at.rseiler.spbee.demo.dao;

import at.rseiler.spbee.demo.entity.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

public class AbstractUserDaoTest extends AbstractTest {

    @Autowired
    AbstractUserDao abstractUserDao;

    @Test
    public void testGetUserWithPermissions() throws Exception {
        User user = abstractUserDao.getUserWithPermissions(3).get();
        assertThat(user.getId(), is(3));
        assertThat(user.getPermissions().size(), is(1));
    }

    @Test
    public void testGetUser() throws Exception {
        Optional<User> user = abstractUserDao.getUser(1);
        assertTrue(user.isPresent());

        user = abstractUserDao.getUser(-1);
        assertFalse(user.isPresent());
    }


    @Test
    public void testGetSimpleUsersWithOwnMapper() throws Exception {
        List<User> users = abstractUserDao.getSimpleUsersWithOwnMapper();
        assertThat(users.size(), is(5));
        assertThat(users.get(0).getId(), is(1));
        assertThat(users.get(0).getName(), is("not loaded"));
    }

    @Test
    public void testReturnNull() {
        assertNull(abstractUserDao.getUserPossibleNull(-1));
    }

    @Test
    public void testReturnNull2() {
        assertNotNull(abstractUserDao.getUserPossibleNull(1));
    }

    @Test
    public void testGetUsersByIds() throws Exception {
        List<User> users = abstractUserDao.getUsersByIds(new Integer[]{1, 3});
        assertThat(users.size(), is(2));
        assertThat(users.get(0).getId(), is(1));
        assertThat(users.get(1).getId(), is(3));
    }
}