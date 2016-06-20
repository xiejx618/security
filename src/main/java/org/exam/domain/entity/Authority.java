package org.exam.domain.entity;

import org.exam.config.Constants;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created on 15/1/10.
 */
@Entity
@Table(name = Constants.TABLE_PREFIX + "authority")
public class Authority implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    @Column(length = 64, nullable = false)
    private String name;
    @Column(length = 64, nullable = false, unique = true)
    private String authority;
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "authorities")
    private Set<Role> roles = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Authority) {
            return authority.equals(((Authority) o).authority);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return authority.hashCode();
    }
}
