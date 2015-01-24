package svanimpe.reminders;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class ArchiveFactory
{
    public static WebArchive createArchive()
    {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage("svanimpe.reminders.domain")
                .addPackage("svanimpe.reminders.json")
                .addPackage("svanimpe.reminders.resources")
                .addPackage("svanimpe.reminders.util")
                .addPackage("svanimpe.reminders.validation")
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("test-glassfish-web.xml", "glassfish-web.xml")
                .addAsWebInfResource("test-web.xml", "web.xml");
    }
}
