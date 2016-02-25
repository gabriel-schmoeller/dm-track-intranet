package ponto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.web.http.HeaderHttpSessionStrategy;
import org.springframework.session.web.http.SessionRepositoryFilter;

/**
 * @author gabriel.schmoeller
 */
@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private MapSessionRepository sessionRepository = new MapSessionRepository();
    private HeaderHttpSessionStrategy httpSessionStrategy = new HeaderHttpSessionStrategy();

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        SessionRepositoryFilter<ExpiringSession> sessionRepositoryFilter = new SessionRepositoryFilter<>(sessionRepository);
        sessionRepositoryFilter.setHttpSessionStrategy(httpSessionStrategy);
        http.addFilterBefore(sessionRepositoryFilter, ChannelProcessingFilter.class);

        http.authorizeRequests()
                .antMatchers("/service/login", "/service/logout").permitAll()
                .antMatchers("/service/**").hasAuthority("user")
                .anyRequest().permitAll()
                .and()
                .formLogin().disable()
                .csrf().disable();
    }
}
