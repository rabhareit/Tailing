package com.rabhareit.tailing.service;

import com.rabhareit.tailing.entity.TailingAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class TailingUserDetails implements UserDetails {
  private static final long serialVersionUID = -256740067874995659L;

  private TailingAccount user;
  private Collection<GrantedAuthority> authorities;

  public TailingUserDetails(TailingAccount account, Collection<GrantedAuthority> authorities){
    this.user = account;
    this.authorities = authorities;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.authorities;
  }

  @Override
  public String getPassword() {
    return user.getPasswd();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
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
    return user.isEnabled();
  }


}
