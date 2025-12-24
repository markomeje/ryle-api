package com.ryle.security;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;
import org.springframework.lang.NonNull;

@Component
public class ClerkJwtAuthFilter extends OncePerRequestFilter {
    @Value("${clerk.issuer}")
    private String clerkIssuer;
    private ClerkJwksProvider clerkJwksProvider;
    private static final Logger logger = LoggerFactory.getLogger(ClerkJwtAuthFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {

        if(request.getRequestURI().contains("/webhook") || request.getRequestURI().contains("/auth")){
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = request.getHeader("Authorization");
            if(authHeader == null || !authHeader.startsWith("Bearer ")){
                logger.error("Invalid token format: {}", authHeader);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid authorization header");
                return;
            }

            String token = authHeader.substring(7);
            String[] split = token.split("\\.");
            if(split.length < 3){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token format");
                return;
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(split[1]));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode headerNode = objectMapper.readTree(headerJson);
            if(!headerNode.has("kid")){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid key");
                return;
            }

            String kid = headerNode.get("kid").asText();
            PublicKey publicKey = clerkJwksProvider.getPublicKey(kid);

            Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .clockSkewSeconds(60)
                .requireIssuer(clerkIssuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String clerkId = claims.getSubject();
            SimpleGrantedAuthority role = new SimpleGrantedAuthority("ROLE_ADMIN");
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    clerkId, null,
                    Collections.singletonList(role)
                );
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
        }catch(Exception e){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

    }
}
