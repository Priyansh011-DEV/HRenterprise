package HR.enterprise.repository;

import HR.enterprise.entity.Company;
import HR.enterprise.entity.Employee;
import HR.enterprise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByCompany(Company company);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByUser(User user);
}
