package lk.esports.betting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@NamedQueries({
        @NamedQuery(name = "UserSession.findByToken",
                query = "SELECT s FROM UserSession s WHERE s.sessionToken = :token"),
        @NamedQuery(name = "UserSession.findByUser",
                query = "SELECT s FROM UserSession s WHERE s.user.id = :userId ORDER BY s.createdAt DESC"),
        @NamedQuery(name = "UserSession.findExpired",
                query = "SELECT s FROM UserSession s WHERE s.expiresAt < CURRENT_TIMESTAMP"),
        @NamedQuery(name = "UserSession.deleteExpired",
                query = "DELETE FROM UserSession s WHERE s.expiresAt < CURRENT_TIMESTAMP")
})
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @Column(name = "session_token", unique = true, nullable = false)
    @NotNull(message = "Session token is required")
    private String sessionToken;

    @Column(name = "expires_at", nullable = false)
    @NotNull(message = "Expiration date is required")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Additional session metadata
    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Constructors
    public UserSession() {
        this.createdAt = LocalDateTime.now();
    }

    public UserSession(User user, String sessionToken, LocalDateTime expiresAt) {
        this();
        this.user = user;
        this.sessionToken = sessionToken;
        this.expiresAt = expiresAt;
    }

    public UserSession(User user, String sessionToken, LocalDateTime expiresAt, String ipAddress, String userAgent) {
        this(user, sessionToken, expiresAt);
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // Business methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return isActive && !isExpired();
    }

    public void invalidate() {
        this.isActive = false;
    }

    public void extendSession(int hours) {
        this.expiresAt = LocalDateTime.now().plusHours(hours);
    }

    public long getRemainingTimeInMinutes() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toMinutes();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", sessionToken='" + sessionToken + '\'' +
                ", expiresAt=" + expiresAt +
                ", isActive=" + isActive +
                '}';
    }
}