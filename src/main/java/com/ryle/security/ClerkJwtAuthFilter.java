package com.ryle.security;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {

        if(request.getRequestURI().contains("/webhook")){
            filterChain.doFilter(request, response);
        }

        String header = request.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer ")){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authorization header");
            return;
        }

        try {
            String token = header.substring(7);
            String[] split = token.split("\\.");
            if(split.length != 3){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(split[1]));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode headerNode = objectMapper.readTree(headerJson);
            if(!headerNode.has("kid")){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid key");
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
            SimpleGrantedAuthority role = new SimpleGrantedAuthority("ROLE_USER");
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    clerkId, null,
                    Collections.singletonList(role)
                );
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
        }catch(Exception e){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

    }
}
