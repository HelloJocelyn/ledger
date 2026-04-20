package com.ledgerx.auth.security;

import com.ledgerx.auth.infra.persistence.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@Setter
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String uuid;
    private final String email;
    private final String name;
    private final UserEntity user;
    private final Map<String, Object> attributes;

    public UserPrincipal(
            Long userId, String uuid, String email, String name, Map<String, Object> attributes, UserEntity user) {
        this.userId = userId;
        this.uuid = uuid;
        this.email = email;
        this.name = name;
        this.attributes = attributes;
        this.user = user;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 目前不做权限
    }

    // 下面是 UserDetails 的方法，先简单实现
    @Override
    public String getPassword() {
        return ""; // social 登录不用密码
    }

    @Override
    public String getUsername() {
        return name == null ? email : name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
