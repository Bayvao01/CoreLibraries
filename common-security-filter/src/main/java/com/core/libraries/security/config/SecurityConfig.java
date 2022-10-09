package com.core.libraries.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private LoggingFilter loggingFilter;

    @Autowired
    private CustomSecurityConfigurer customSecurityConfigurer;

    @Autowired
    private MultiValueMap<String, String> allListedPublicURIs;

    private static final String[] AUTH_SWAGGER_WHITELIST = {
            "/authenticate",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**"
    };

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
        logger.debug("SecurityConfig initialized.");

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>
                .ExpressionInterceptUrlRegistry authorizeRequests = httpSecurity.authorizeRequests();

        // don't authenticate this particular uri requests
        addPublicURIs(authorizeRequests);

        authorizeRequests
                // all other requests need to be authenticated
                .anyRequest().authenticated().and()
                // make sure we use stateless session; session won't be used to
                // store user's state.
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                /*
                .and().csrf(csrfCustomizer -> {
                    ignoreCsrfForPunlicURIs(csrfCustomizer);
                    csrfCustomizer.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
                })
                 */
                .and().csrf().disable().cors();

        // Add a filter to log the request-response of every request
        httpSecurity.addFilterBefore(loggingFilter, UsernamePasswordAuthenticationFilter.class);
        // Add a filter to validate the tokens with every request
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    private void ignoreCsrfForPunlicURIs(CsrfConfigurer<HttpSecurity> csrfCustomizer) {
        if(!ObjectUtils.isEmpty(allListedPublicURIs)) {
            allListedPublicURIs.keySet()
                    .forEach(uri -> csrfWhitelistPublicURIs(csrfCustomizer, uri));
        }
    }

    private void csrfWhitelistPublicURIs(CsrfConfigurer<HttpSecurity> csrfCustomizer, String uri) {
        for (String currentMethod : allListedPublicURIs.get(uri)){
            csrfCustomizer.ignoringAntMatchers(currentMethod, uri);
        }
    }

    public void addPublicURIs(ExpressionUrlAuthorizationConfigurer<HttpSecurity>
                                      .ExpressionInterceptUrlRegistry authorizeRequests){

        if(!ObjectUtils.isEmpty(allListedPublicURIs)){
            allListedPublicURIs.keySet()
                    .forEach(uri -> whitelistAllURI(authorizeRequests, uri));
        }
    }

    private void whitelistAllURI(ExpressionUrlAuthorizationConfigurer<HttpSecurity>
                                         .ExpressionInterceptUrlRegistry authorizeRequests, String uri) {

        for (String currentMethod : allListedPublicURIs.get(uri)){
            authorizeRequests.antMatchers(currentMethod, uri).permitAll();
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        logger.info("AuthenticationManager invoked.");
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("PasswordEncoder invoked.");
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    //Used for customizing Web Security
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers(AUTH_SWAGGER_WHITELIST);
    }
}
