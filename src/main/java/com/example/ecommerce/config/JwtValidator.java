package com.example.ecommerce.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class JwtValidator extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();


        System.out.println(requestUri);

        if (requestUri.startsWith("/auth")
                || requestUri.startsWith("/health-check")
                || requestUri.startsWith("/favicon") ||
                requestUri.startsWith("/api/seller/apply") ||
                requestUri.startsWith("/api/seller/verify-otp") ||
                requestUri.startsWith("/api/categories/all")||
                requestUri.startsWith("/api/products/all/best-seller")||
                requestUri.startsWith("/api/products/all/featured")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = request.getHeader(JWT_CONSTANT.JWT_HEADER);

        if (jwt == null) {
            throw new BadCredentialsException("Missing Authorization Header!");
        }


        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        } else {
            throw new BadCredentialsException("Invalid Token Format!");
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(JWT_CONSTANT.SECRET_KEY.getBytes()));

            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();

            String email = claims.get("email", String.class);
            String authorities = claims.get("authorities", String.class);

            List<GrantedAuthority> auths = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

            Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, auths);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            throw new BadCredentialsException("Token Validation Failed!");
        }

        filterChain.doFilter(request, response);
    }
}