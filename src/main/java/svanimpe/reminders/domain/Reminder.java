package svanimpe.reminders.domain;

import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static svanimpe.reminders.util.Utilities.cleanUp;

@Entity
@NamedQueries({
    @NamedQuery(name = "Reminder.findByList", query = "SELECT r FROM Reminder r WHERE r.list = :list")
})
public class Reminder
{
    @Id
    @GeneratedValue(generator = "REMINDER_ID")
    @TableGenerator(name = "REMINDER_ID", table = "ID_GEN", allocationSize = 1)
    @Min(value = 0, message = "a reminder's id must be greater than 0")
    private long id;

    @ManyToOne
    @NotNull(message = "a reminder must belong to a list")
    private List list;

    @NotNull(message = "REMINDER_TITLE")
    private String title;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "REMINDER_DATE")
    private Calendar date;

    @Embedded
    private Location location;

    private String image;
    
    public long getId()
    {
        return id;
    }

    public List getList()
    {
        return list;
    }

    public void setList(List list)
    {
        this.list = list;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = cleanUp(title);
    }

    public Calendar getDate()
    {
        return date;
    }

    public void setDate(Calendar date)
    {
        this.date = date;
    }

    public Location getLocation()
    {
        return location;
    }

    public void setLocation(Location location)
    {
        this.location = location;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = cleanUp(image);
    }
    
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 59 * hash + (int) (this.id ^ (this.id >>> 32));
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
        final Reminder other = (Reminder) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
}
