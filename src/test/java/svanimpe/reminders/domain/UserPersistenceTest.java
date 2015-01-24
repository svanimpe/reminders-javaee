package svanimpe.reminders.domain;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import svanimpe.reminders.ArchiveFactory;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class UserPersistenceTest
{
    @Deployment
    public static WebArchive createArchive()
    {
        return ArchiveFactory.createArchive();
    }
    
    @PersistenceContext
    private EntityManager em;
    
    @Test
    public void testUserTablesExist()
    {
        Query query = em.createNativeQuery("SHOW TABLES");
        List results = query.getResultList();
        assertTrue(results.contains("TBL_USER"));
        assertTrue(results.contains("USER_PASSWORD"));
        assertTrue(results.contains("USER_ROLES"));
    }
    
    @Test
    public void testUserPasswordColumnsExist()
    {
        Query query = em.createNativeQuery("SHOW COLUMNS FROM USER_PASSWORD");
        List results = query.getResultList();
        
        List columns = new ArrayList();
        for (Object result : results) {
            Object[] column = (Object[])result;
            columns.add(column[0]);
        }
        
        assertTrue(columns.contains("USERNAME"));
        assertTrue(columns.contains("PASSWORD"));
    }
    
    @Test
    public void testUserRolesColumnsExist()
    {
        Query query = em.createNativeQuery("SHOW COLUMNS FROM USER_ROLES");
        List results = query.getResultList();
        
        List columns = new ArrayList();
        for (Object result : results) {
            Object[] column = (Object[])result;
            columns.add(column[0]);
        }
        
        assertTrue(columns.contains("USERNAME"));
        assertTrue(columns.contains("ROLES"));
    }
    
    @Test
    public void testPlainPasswordNotSaved() throws Exception
    {
        User savedUser = em.find(User.class, "someuser");
        Field plainPassword = User.class.getDeclaredField("plainPassword");
        plainPassword.setAccessible(true);
        assertNull(plainPassword.get(savedUser));
    }
    
    @Test
    public void testRolesStoredAsStrings()
    {
        Query query = em.createNativeQuery("SELECT ROLES FROM USER_ROLES WHERE USERNAME = 'someuser'");
        List results = query.getResultList();
        assertTrue(results.size() == 1 && results.contains(Role.ADMINISTRATOR.name()));
    }
    
    @Test
    public void testQueryFindAll()
    {
        TypedQuery<User> query = em.createNamedQuery("User.findAll", User.class);
        assertEquals(2, query.getResultList().size());
    }
}
