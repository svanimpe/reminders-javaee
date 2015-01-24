package svanimpe.reminders.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.TableGenerator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import static svanimpe.reminders.util.Utilities.cleanUp;

@Entity
@NamedQueries({
    @NamedQuery(name = "List.findByOwner", query = "SELECT l FROM List l WHERE l.owner = :owner"),
    @NamedQuery(name = "List.findSize", query = "SELECT COUNT(r) FROM Reminder r WHERE r.list = :list")
})
public class List
{
    @Id
    @GeneratedValue(generator = "LIST_ID")
    @TableGenerator(name = "LIST_ID", table = "ID_GEN", allocationSize = 1)
    @Min(value = 0, message = "a list's id must be greater than 0")
    private long id;

    @NotNull(message = "LIST_TITLE")
    private String title;

    @ManyToOne
    @NotNull(message = "a list must be assigned to a user")
    private User owner;

    public long getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = cleanUp(title);
    }

    public User getOwner()
    {
        return owner;
    }

    public void setOwner(User owner)
    {
        this.owner = owner;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 83 * hash + (int) (this.id ^ (this.id >>> 32));
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
        final List other = (List) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
}
