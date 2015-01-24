package svanimpe.reminders.domain;

import javax.persistence.Embeddable;

/*
 * A location, consisting of a latitude and longitude. This class is mapped as an embeddable, it
 * doesn't need to be an entity.
 */
@Embeddable
public class Location
{
    private double latitude;
    private double longitude;

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }
}
