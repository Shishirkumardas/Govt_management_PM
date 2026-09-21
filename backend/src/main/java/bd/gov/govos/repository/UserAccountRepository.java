package bd.gov.govos.repository;

import bd.gov.govos.domain.entity.UserAccount;
import bd.gov.govos.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
	Optional<UserAccount> findByUsername(String username);
	boolean existsByUsername(String username);
	List<UserAccount> findByMinistryId(Long ministryId);
	List<UserAccount> findByRole(UserRole role);
}
