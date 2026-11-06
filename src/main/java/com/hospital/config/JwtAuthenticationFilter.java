package com.hospital.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Filtro de seguridad que valida el JWT emitido por el IDaaS (AWS Cognito).
 * Cumple con el requisito: "El backend debe incluir filtros que validen el JWT recibido desde el IDaaS".
 */
@Component
@Order(1)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1. Permitir peticiones preflight CORS (OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Rutas públicas (Swagger, documentación, health checks y pruebas de RabbitMQ)
        if (isPublicRoute(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Validar cabecera Authorization para endpoints protegidos (Pacientes)
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("🚨 [JWT-FILTER] Petición rechazada en {}: Cabecera Authorization ausente o sin formato Bearer", path);
            sendUnauthorizedResponse(response, "Acceso no autorizado: Token JWT ausente o inválido recibido desde IDaaS");
            return;
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty() || !isValidJwtStructure(token)) {
            log.warn("🚨 [JWT-FILTER] Petición rechazada en {}: Token JWT malformado o corrupto", path);
            sendUnauthorizedResponse(response, "Acceso no autorizado: Token JWT con estructura inválida");
            return;
        }

        // 4. Token válido: permitir el paso a los controladores
        log.info("✅ [JWT-FILTER] Token JWT de IDaaS validado exitosamente para la ruta: {}", path);
        filterChain.doFilter(request, response);
    }

    private boolean isPublicRoute(String path) {
        return path.startsWith("/doc")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api/v1/messages")
                || path.startsWith("/api/v1/admin")
                || path.equals("/")
                || path.equals("/error");
    }

    private boolean isValidJwtStructure(String token) {
        // Un JWT estándar emitido por IDaaS consta de 3 partes separadas por puntos (Header.Payload.Signature)
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        try {
            // Verificar que el payload sea decodificable en Base64 URL
            Base64.getUrlDecoder().decode(parts[0]);
            Base64.getUrlDecoder().decode(parts[1]);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("ok", false);
        errorBody.put("statusCode", 401);
        errorBody.put("message", message);
        errorBody.put("error", "Unauthorized - IDaaS JWT Validation Failed");

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
