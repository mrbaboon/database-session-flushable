package grails.plugin.databasesessionflushable;

import com.google.common.collect.ImmutableSortedMap;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import javax.servlet.ServletContext;
import javax.servlet.http.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Burt Beckwith
 * @author Robert Fischer
 */
@SuppressWarnings("deprecation")
public class SessionLazyProxy extends DatabaseSession {

    private static final Logger log = Logger.getLogger(grails.plugin.databasesessionflushable.SessionLazyProxy.class);

    private SessionProxy _session;
    private final Persister _persister;
    private final String _sessionId;
    private final ServletContext _servletContext;


    /**
     * Constructor. Note that you should call {@link #fireSessionActivationListeners()} after construction is complete.
     *
     * @param servletContext the ServletContext
     * @param persister the persister
     * @param sessionId session id
     */
    public SessionLazyProxy(ServletContext servletContext, Persister persister, final String sessionId) {
        _servletContext = servletContext;
        _persister = persister;
        _sessionId = sessionId;
        _session = null;

        log.debug("Done constructing the lazy proxy session for " + sessionId);
    }

    public SessionLazyProxy(final ServletContext servletContext, final Persister persister, final String sessionId, final SessionData data) {
        _servletContext = servletContext;
        _persister = persister;
        _sessionId = sessionId;

        _session = new SessionProxy( servletContext, persister, sessionId, data );
        log.debug("Done constructing the lazy proxy session for " + sessionId);
    }

    public SessionProxy session()
    {
        if ( _session == null )
        {
            log.debug("\n\nLazy Loading Session "+this._sessionId+"!!!\n\n");
            _session = new SessionProxy( this._servletContext, this._persister, this._sessionId );
        }
        return _session;
    }



    /**
     * Mapping back to the {@link grails.plugin.databasesessionflushable.SessionData} holder.
     */
    public SessionData toData() {
        return session().toData();
    }

    public void fireSessionActivationListeners() {
        session().fireSessionActivationListeners();
    }

    public void fireSessionPassivationListeners() {
        session().fireSessionPassivationListeners();
    }

    @Override
    public Serializable getAttribute(String name) {
        return session().getAttribute(name);
    }

    @Override @Deprecated
    public Serializable getValue(String name) {
        return session().getValue(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return session().getAttributeNames();
    }

    @Override @Deprecated
    public String[] getValueNames() {
        return session().getValueNames();
    }

    @Override
    public void setAttribute(String name, Object value) {
        session().setAttribute(name, value);
    }

    @Override @Deprecated
    public void putValue(String name, Object value) {
        session().putValue(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        session().removeAttribute(name);
    }

    @Override @Deprecated
    public void removeValue(String name) {
        session().removeValue(name);
    }

    @Override
    public long getCreationTime() {
        return session().getCreationTime();
    }

    @Override
    public String getId() {
        return _sessionId;
    }

    @Override
    public long getLastAccessedTime() {
        return session().getLastAccessedTime();
    }

    @Override
    public ServletContext getServletContext() {
        return _servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        session().setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval() {
        return session().getMaxInactiveInterval();
    }

    /**
     * Due to security concerns, the returned {@link javax.servlet.http.HttpSessionContext} simply returns {@code null} for all
     * its method calls.
     */
    @Override @Deprecated
    public HttpSessionContext getSessionContext() {
        return session().getSessionContext();
    }

    @Override
    public void invalidate() {
        session().invalidate();
    }

    public void flush() {
        session().flush();
    }

    /**
     * Since we use the built-in session for the first round, this can always be {@code false}.
     */
    @Override
    public boolean isNew() {
        return false;
    }

    /**
     * Gets an immutable map of all the attributes.
     */
    public Map<String,Serializable> getAttributes() {
        return session().getAttributes();
    }

    public long getCreatedAt() {
        return session().getCreatedAt();
    }

}
