/**
 ▄▄▄▄▄▄ Dev < Eugene Baniaga >
 ▓ 🦖 ▒ Debian GNU/Linux 12 (bookworm)
 ▀▀▀▀▀▀ Kernel: 6.1.0-32-amd64
**/

package com.pos.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}