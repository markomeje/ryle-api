package com.ryle.security;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class ClerkJwksProvider {

    @Value("${clerk.jwks-url}")
    private String jwksUrl;
    private final Map<String, PublicKey> keyCache = new HashMap<>();
    private static final long CACHE_TTL = 3600000;
    private long lastFetchTime = 0;

    public PublicKey getPublicKey(String kid) throws Exception {
        if(keyCache.containsKey(kid) && System.currentTimeMillis() - lastFetchTime > CACHE_TTL) {
            return keyCache.get(kid);
        }

        refreshKeys();
        return keyCache.get(kid);
    }

    private void refreshKeys() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jwks = objectMapper.readTree(jwksUrl);

        JsonNode keys = jwks.get("keys");
        for (JsonNode key : keys) {
            handleJwksKey(key);
        }

        lastFetchTime = System.currentTimeMillis();
    }

    private void handleJwksKey(JsonNode key) throws Exception {
        String kid = key.get("kid").asText();
        String kty = key.get("kty").asText();
        String alg = key.get("alg").asText();

        if("RSA".equals(kty) && "RS256".equals(alg)) {
            String node = key.get("n").asText();
            String exp = key.get("e").asText();

            PublicKey publicKey = createPublicKey(node, exp);
            keyCache.put(kid, publicKey);
        }
    }

    private PublicKey createPublicKey(String modulus, String exponent) throws Exception {
        byte[] exponentBytes = Base64.getUrlDecoder().decode(exponent);
        byte[] modulusBytes = Base64.getUrlDecoder().decode(modulus);

        BigInteger exponentBigInt = new BigInteger(1, exponentBytes);
        BigInteger modulusBigInt = new BigInteger(1, modulusBytes);

        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulusBigInt, exponentBigInt);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}
