package org.penpal.api.gateway.filter;

import io.jsonwebtoken.Claims;
import org.penpal.api.gateway.exception.JwtTokenMalformedException;
import org.penpal.api.gateway.exception.JwtTokenMissingException;
import org.penpal.api.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@Component
public class JwtAuthenticationFilter implements GatewayFilter {
    @Autowired
    private JwtUtil jwtUtil;

    // Use PathMatcher to handle dynamic paths
    PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Define endpoints for which POST requests should be bypassed
        final List<String> bypassPostEndpoints = List.of(
                "/api/auth/students",
                "/api/auth/teachers",
                "/api/auth/translators",
                "/api/auth/researchers"
        );

//        final List<String> bypassGetEndpoints = List.of(
//                "/api/auth/teachers"
//        );

        // Define all endpoints to bypass
        final List<String> apiEndpoints = List.of(
                "api/auth/login",
                "api/auth/forgot-password/**",
                "api/auth/change-password/**"
        );

        if (pathMatcher.match("/api/auth/teachers/{email}", request.getURI().getPath())) {
            return chain.filter(exchange); // Bypass this request
        }

        // Combine logic to bypass certain endpoints and specific POST requests
        Predicate<ServerHttpRequest> isApiSecured = r -> {
            String path = r.getURI().getPath();
            boolean isBypassedPost = "POST".equalsIgnoreCase(r.getMethod().name()) &&
                    bypassPostEndpoints.stream().anyMatch(path::contains);
            boolean isBypassedGeneral = apiEndpoints.stream().anyMatch(path::contains);
            return !(isBypassedPost || isBypassedGeneral);
        };

//        if (isApiSecured.test(request)) {
//            if (!request.getHeaders().containsKey("Authorization")) {
//                ServerHttpResponse response = exchange.getResponse();
//                response.setStatusCode(HttpStatus.UNAUTHORIZED);
//                return response.setComplete();
//            }
//
//            final String token = request.getHeaders().getOrEmpty("Authorization").get(0).split(" ")[1];
//
//            try {
//                jwtUtil.validateToken(token);
//            } catch (JwtTokenMalformedException | JwtTokenMissingException e) {
//                System.out.println(token + "*" + e.toString());
//                ServerHttpResponse response = exchange.getResponse();
//                response.setStatusCode(HttpStatus.BAD_REQUEST);
//                return response.setComplete();
//            }
//
//            Claims claims = jwtUtil.getClaims(token);
//            exchange.getRequest().mutate().header("id", String.valueOf(claims.get("id"))).build();
//        }

        return chain.filter(exchange);
    }
}
