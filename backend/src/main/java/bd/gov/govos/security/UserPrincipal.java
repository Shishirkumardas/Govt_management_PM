package bd.gov.govos.security;

import bd.gov.govos.domain.entity.UserAccount;
import bd.gov.govos.domain.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails {

	private final Long id;
	private final String username;
	private final String password;
	private final String fullName;
	private final UserRole role;
	private final Long ministryId;
	private final boolean active;

	public UserPrincipal(UserAccount user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.password = user.getPasswordHash();
		this.fullName = user.getFullName();
		this.role = user.getRole();
		this.ministryId = user.getMinistry() != null ? user.getMinistry().getId() : null;
		this.active = Boolean.TRUE.equals(user.getActive());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return active;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return active;
	}
}
