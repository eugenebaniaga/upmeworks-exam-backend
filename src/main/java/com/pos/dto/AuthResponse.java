/**
 ▄▄▄▄▄▄ Dev < Eugene Baniaga >
 ▓ 🦖 ▒ Debian GNU/Linux 12 (bookworm)
 ▀▀▀▀▀▀ Kernel: 6.1.0-32-amd64
**/

package com.pos.dto;

import com.pos.entity.User;

import java.util.UUID;

public class AuthResponse {
    private String token;
    private UserDto user;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }

    public static class UserDto {
        private UUID id;
        private String email;
        private String fullName;
        private User.Role role;

        public UserDto() {}

        public UserDto(UUID id, String email, String fullName, User.Role role) {
            this.id = id;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
        }

        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public User.Role getRole() { return role; }
        public void setRole(User.Role role) { this.role = role; }
    }
}