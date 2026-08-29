package com.example.OAuthBankingBackendApplication.filter;

import com.example.OAuthBankingBackendApplication.constants.ApplicationConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

public class JWTTokenGeneratorFiler extends OncePerRequestFilter {

    private static final long TOKEN_VALIDITY_MS = 30_000_000L; //

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if( null != authentication)
        {
            Environment env = getEnvironment();
            if (null!= env)
            {
                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                                                ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                /*String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY);*/


                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

                String authorities = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));

                String jwt = Jwts.builder()
                        .issuer("SBI Bank")
                        .subject(authentication.getName())   // meaningful subject
                        .claim("username", authentication.getName())
                        .claim("authorities", authorities)
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY_MS))
                        .signWith(secretKey)
                        .compact();

                response.setHeader(ApplicationConstants.JWT_HEADER, jwt);
            }

        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getServletPath().equals("/user");
    }
}
