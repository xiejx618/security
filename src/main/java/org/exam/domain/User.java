package org.exam.domain;

import org.exam.config.Constants;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by xin on 15/1/7.
 */
@Entity
@Table(name = Constants.TABLE_PREFIX + "user")
public class User implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    @Column(length = 64, nullable = false, unique = true)
    private String username;
    @Column(length = 64, nullable = false)
    private String password;
    private boolean enabled = true;
    private boolean accountNonLocked = true;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = Constants.TABLE_PREFIX + "user_role")
    private Set<Role> roles = new HashSet<>();
    @Transient
    private Set<Authority> authorities = null;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof User) {
            return username.equals(((User) o).username);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
