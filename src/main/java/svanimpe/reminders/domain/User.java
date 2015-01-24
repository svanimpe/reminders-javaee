package svanimpe.reminders.domain;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import svanimpe.reminders.validation.OnPasswordUpdate;
import svanimpe.reminders.validation.ValidPassword;
import svanimpe.reminders.validation.ValidUsername;

import static svanimpe.reminders.util.Utilities.*;

/*
 * Note that this class uses unnecessary mapping annotations to set table and column names. This is
 * done to make the mapping explicit and serves as a reminder because the names are needed to set up
 * the security realm. The table name is set to TBL_USER because USER is a reserved SQL keyword.
 * Passwords are stored in a separate table, for good form.
 */

@Entity
@Table(name = "TBL_USER")
@SecondaryTable(name = "USER_PASSWORD")
@NamedQueries({
    @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u")
})
public class User
{
    @Id
    @ValidUsername
    private String username;
    
    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = cleanUp(username);
    }
    
    private String fullName;

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = cleanUp(fullName);
    }
    
    public static final String DEFAULT_PROFILE_PICTURE = "default.png";
    
    @NotNull(message = "profilePicture should never be null")
    private String profilePicture = DEFAULT_PROFILE_PICTURE;

    public String getProfilePicture()
    {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture)
    {
        this.profilePicture = cleanUp(profilePicture, DEFAULT_PROFILE_PICTURE);
    }
    
    /*
     * The plain text password is used for validation purposes only. It is not persisted. Only the
     * encrypted password is persisted. The plain text password also cannot be read, it can only be
     * set. The method getPassword returns the encrypted password.
     */
    
    @Transient
    @ValidPassword(groups = OnPasswordUpdate.class)
    private String plainPassword;

    @NotNull(message = "USER_PASSWORD")
    @Pattern(regexp = "[A-Fa-f0-9]{64}+", message = "invalid encrypted password")
    @Column(name = "PASSWORD", table = "USER_PASSWORD")
    private String encryptedPassword;

    /*
     * Returns the encrypted password. Returns null as long as the password is not yet set.
     */
    public String getPassword()
    {
        return encryptedPassword;
    }

    /*
     * Sets and encrypts the password. Will trim off any leading or trailing whitespace before
     * encryption. Null values are replaced with an empty string to avoid NullPointerExceptions.
     */
    public void setPassword(String plainPassword)
    {
        this.plainPassword = cleanUp(plainPassword, "");
        
        try {
            BigInteger hash = new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(this.plainPassword.getBytes("UTF-8")));
            encryptedPassword = hash.toString(16);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "USER_ROLES", joinColumns = @JoinColumn(name = "USERNAME"))
    @Column(name = "ROLES")
    private final List<Role> roles = new ArrayList<>();

    public List<Role> getRoles()
    {
        return roles;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.username);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        return Objects.equals(this.username, other.username);
    }

    @Override
    public String toString()
    {
        return username;
    }
}
