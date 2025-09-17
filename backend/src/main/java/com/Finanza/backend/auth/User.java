package com.Finanza.backend.auth;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // opzionale, ma utile per coerenza
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // lascia che il DB generi l'id
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    public User() {}

    // getter & setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; } // normalmente non lo userai

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}