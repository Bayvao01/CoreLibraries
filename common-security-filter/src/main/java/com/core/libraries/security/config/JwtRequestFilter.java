package com.core.libraries.security.config;

import com.core.libraries.security.util.EncryptionUtil;
import com.core.libraries.security.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.IOException;

import static com.core.libraries.security.constants.SecurityConstants.*;
import static com.core.libraries.security.constants.SecurityErrorConstants.*;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String tokenFromRequest = request.getHeader(AUTHORIZATION);

        String userName;
        String encryptedJwtToken;
        String jwtToken;

        logger.debug("Inside JwtRequestFilter--OncePerRequestFilter");
        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (tokenFromRequest != null && tokenFromRequest.startsWith(BEARER)) {

            encryptedJwtToken = tokenFromRequest.substring(7);
            jwtToken = EncryptionUtil.decrypt(encryptedJwtToken);
            try {
                userName = jwtTokenUtil.getUserNameFromToken(jwtToken);

                //  Once we get the token validate it and extract username(principal/subject) from it.
                if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // if token is valid configure Spring Security to manually set authentication
                    if (jwtTokenUtil.validateToken(jwtToken)) {

                        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
                        List<Map<String, String>> authorities = (ArrayList) jwtTokenUtil.getCustomParamFromToken(jwtToken, "authorities");

                        authorities.forEach(authority ->
                            grantedAuthorities.add(new SimpleGrantedAuthority(authority.get(AUTHORITY)))
                        );

                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                                userName, null, grantedAuthorities);

                        usernamePasswordAuthenticationToken
                                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        // After setting the Authentication in the context, we specify
                        // that the current user is authenticated. So it passes the
                        // Spring Security Configurations successfully.
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }

            } catch (IllegalArgumentException e) {
                logger.error(JWT_NOT_FOUND_LOG_MSG);
            } catch (ExpiredJwtException e) {
                logger.error(JWT_EXPIRED_LOG_MSG);
            }
        } else {
            logger.warn(JWT_DOESNOTS_START_WITH_BEARER_LOG_MSG);
        }
        filterChain.doFilter(request, response);
    }
}