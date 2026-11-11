package sn.guediawaye.backend.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, Long> {

    Optional<AdminAccount> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByRole(AdminRole role);

    Optional<AdminAccount> findFirstByRole(AdminRole role);

    long countByRole(AdminRole role);

    List<AdminAccount> findAllByRoleOrderByCreatedAtAsc(AdminRole role);
}
