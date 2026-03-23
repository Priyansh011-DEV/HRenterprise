package HR.enterprise.repository;

import HR.enterprise.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByEmployeeId(Long employeeId);

    List<Leave> findByCompanyId(Long companyId);
}
