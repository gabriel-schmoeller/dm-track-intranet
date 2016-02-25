package ponto.intranet.handlers.domain;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author gabriel.schmoeller
 */
@Component
@Scope(proxyMode= ScopedProxyMode.TARGET_CLASS, value="session")
public class SessionContext {

    private final HttpClientContext httpContext;
    private String username;

    public SessionContext() {
        httpContext = HttpClientContext.create();
        httpContext.setCookieStore(new BasicCookieStore());
        this.username = "";
    }

    public HttpClientContext getHttpContext() {
        return httpContext;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
