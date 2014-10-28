package grails.plugin.databasesessionflushable;


import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Michael Ferguson
 */
public abstract class DatabaseSession implements HttpSession,Serializable,Cloneable
{

    public abstract SessionData toData();

    public abstract void fireSessionActivationListeners();

    public abstract void fireSessionPassivationListeners();

    public abstract Map<String,Serializable> getAttributes();

    public abstract long getCreatedAt();

    public abstract void flush();
}
