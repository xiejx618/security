package org.exam.domain;

import org.exam.config.Constants;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by xin on 15/1/12.
 */
@Entity
@Table(name = Constants.TABLE_PREFIX + "role")
public class Role implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    @Column(length = 64, nullable = false)
    private String name;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = Constants.TABLE_PREFIX + "role_authority")
    private Set<Authority> authorities = new HashSet<>();

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

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Role) {
            return name.equals(((Role) o).getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
