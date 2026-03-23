package HR.enterprise.repository;

import HR.enterprise.entity.Leave;
import HR.enterprise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {

    // ✅ Get leaves by the user who applied
    List<Leave> findByAppliedBy(User user);

    // ✅ Get all leaves for a company
    List<Leave> findByCompanyId(Long companyId);
}