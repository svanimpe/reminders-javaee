/*
 * Copyright (c) 2014, Steven Van Impe
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *     following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
