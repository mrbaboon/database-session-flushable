package grails.plugin.databasesessionflushable;

import com.google.common.collect.ImmutableMap;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * A deeply immutable holder for all the data that we need to pass to/from a session.
 *
 * @author Robert Fischer
 */
public class SessionData {

	public final String sessionId;
	public final Map<String,Serializable> attrs;
	public final long createdAt;
	public final long lastAccessedAt;
	public final int maxInactiveInterval; // In seconds

	public static SessionData fromSession(HttpSession session) {
		if(session instanceof DatabaseSession) return fromProxy((DatabaseSession)session);
	
		ImmutableMap.Builder<String,Serializable> builder = ImmutableMap.builder();
		
		for(String name : Collections.list(session.getAttributeNames())) {
			builder.put(name, (Serializable)session.getAttribute(name));
		}
		final ImmutableMap<String,Serializable> attrs = builder.build();
		builder = null;

		return new SessionData(
			session.getId(), attrs,
			session.getCreationTime(), session.getLastAccessedTime(),
			session.getMaxInactiveInterval()
		);
	}

	public static SessionData fromProxy(DatabaseSession proxy) {
		return new SessionData(
			proxy.getId(), ImmutableMap.copyOf(proxy.getAttributes()),
			proxy.getCreationTime(), proxy.getLastAccessedTime(),
			proxy.getMaxInactiveInterval()
		);
	}

	public SessionData(
		final String sessionId, final Map<String,Serializable> attrs,
		final long createdAt, final long lastAccessedAt,
		final int maxInactiveInterval
	) {
		this.sessionId = sessionId;
		if(attrs == null || attrs.isEmpty()) {
			this.attrs = ImmutableMap.of();
		} else {
			this.attrs = ImmutableMap.copyOf(attrs);
		}
		this.createdAt = createdAt;
		this.lastAccessedAt = lastAccessedAt;
		this.maxInactiveInterval = maxInactiveInterval;
	}

	public String toString() {
		return "SessionData[" + sessionId + "]";
	}

}

