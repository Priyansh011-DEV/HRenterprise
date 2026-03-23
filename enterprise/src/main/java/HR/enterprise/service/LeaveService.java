package HR.enterprise.service;

import HR.enterprise.dto.LeaveRequestDTO;
import HR.enterprise.dto.LeaveResponseDTO;
import HR.enterprise.entity.*;
import HR.enterprise.repository.EmployeeRepository;
import HR.enterprise.repository.LeaveRepository;
import HR.enterprise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LeaveRepository leaveRepository;

    // ✅ Apply Leave (EMPLOYEE)
    public LeaveResponseDTO applyLeave(LeaveRequestDTO dto, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.EMPLOYEE) {
            throw new RuntimeException("Only EMPLOYEE can apply for leave");
        }

        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Leave leave = new Leave();
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setReason(dto.getReason());
        leave.setStatus(LeaveStatus.PENDING);

        leave.setEmployee(employee);
        leave.setCompany(employee.getCompany());

        leaveRepository.save(leave);

        return mapToDTO(leave);
    }

    // ✅ Approve / Reject Leave (ADMIN)
    public LeaveResponseDTO updateLeaveStatus(Long leaveId, LeaveStatus status, String username) {

        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (admin.getRole() != Role.ADMIN && admin.getRole() != Role.HR) {
            throw new RuntimeException("Only ADMIN can approve/reject leave");
        }

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        // Multi-tenant security check
        if (!leave.getCompany().getId().equals(admin.getCompany().getId())) {
            throw new RuntimeException("Unauthorized: Different company");
        }

        leave.setStatus(status);
        leaveRepository.save(leave);

        return mapToDTO(leave);
    }

    // ✅ Get My Leaves (EMPLOYEE)
    public List<LeaveResponseDTO> getMyLeaves(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return leaveRepository.findByEmployeeId(employee.getId())
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ✅ (Optional) Admin - Get all leaves of company
    public List<LeaveResponseDTO> getAllCompanyLeaves(String username) {

        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only ADMIN can view all leaves");
        }

        return leaveRepository.findByCompanyId(admin.getCompany().getId())
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // 🔁 Mapping function
    private LeaveResponseDTO mapToDTO(Leave leave) {

        LeaveResponseDTO dto = new LeaveResponseDTO();

        dto.setId(leave.getId());
        dto.setLeaveType(leave.getLeaveType());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setStatus(leave.getStatus().name());

        dto.setEmployeeName(
                leave.getEmployee().getUser().getUsername()
        );

        return dto;
    }
}
