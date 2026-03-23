package HR.enterprise.service;

import HR.enterprise.dto.LeaveRequestDTO;
import HR.enterprise.dto.LeaveResponseDTO;
import HR.enterprise.entity.*;
import HR.enterprise.repository.LeaveRepository;
import HR.enterprise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final UserRepository userRepository;
    private final LeaveRepository leaveRepository;

    // ================= APPLY LEAVE =================
    // Any role (ADMIN, HR, EMPLOYEE) can apply leave
    public LeaveResponseDTO applyLeave(LeaveRequestDTO dto, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Leave leave = new Leave();
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setReason(dto.getReason());
        leave.setStatus(LeaveStatus.PENDING);
        leave.setAppliedBy(user);             // ✅ any role can apply
        leave.setCompany(user.getCompany());  // ✅ company from user directly

        leaveRepository.save(leave);

        return mapToDTO(leave);
    }


    // ================= APPROVE / REJECT =================
    public LeaveResponseDTO updateLeaveStatus(Long leaveId, LeaveStatus status, String username) {

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        // Multi-tenant check — same company only
        if (!leave.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new RuntimeException("Unauthorized: Different company");
        }

        // Already processed check
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Leave already processed");
        }

        Role approverRole = currentUser.getRole();
        Role leaveOwnerRole = leave.getAppliedBy().getRole(); // ✅ from appliedBy

        // EMPLOYEE cannot approve anyone
        if (approverRole == Role.EMPLOYEE) {
            throw new RuntimeException("Employees cannot approve leaves");
        }

        // Self approval block
        if (leave.getAppliedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You cannot approve your own leave");
        }

        // HR can only approve EMPLOYEE leaves
        if (approverRole == Role.HR) {
            if (leaveOwnerRole != Role.EMPLOYEE) {
                throw new RuntimeException("HR can only approve EMPLOYEE leaves");
            }
        }

        // ADMIN can approve both HR + EMPLOYEE leaves (no extra check needed)

        leave.setStatus(status);
        leave.setApprovedBy(currentUser.getUsername());
        leave.setApprovedAt(LocalDateTime.now());

        leaveRepository.save(leave);

        return mapToDTO(leave);
    }


    // ================= GET MY LEAVES =================
    public List<LeaveResponseDTO> getMyLeaves(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return leaveRepository.findByAppliedBy(user) // ✅ directly by user
                .stream()
                .map(this::mapToDTO)
                .toList();
    }


    // ================= GET ALL (ROLE BASED) =================
    public List<LeaveResponseDTO> getAllLeaves(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = user.getRole();
        List<Leave> leaves;

        if (role == Role.ADMIN) {
            // Admin sees ALL leaves in the company (HR + EMPLOYEE)
            leaves = leaveRepository.findByCompanyId(user.getCompany().getId());

        } else if (role == Role.HR) {
            // HR sees only EMPLOYEE leaves
            leaves = leaveRepository.findByCompanyId(user.getCompany().getId())
                    .stream()
                    .filter(l -> l.getAppliedBy().getRole() == Role.EMPLOYEE) // ✅
                    .toList();

        } else {
            throw new RuntimeException("Unauthorized");
        }

        return leaves.stream()
                .map(this::mapToDTO)
                .toList();
    }


    // ================= DTO MAPPER =================
    private LeaveResponseDTO mapToDTO(Leave leave) {

        LeaveResponseDTO dto = new LeaveResponseDTO();

        dto.setId(leave.getId());
        dto.setLeaveType(leave.getLeaveType());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setStatus(leave.getStatus().name());
        dto.setEmployeeName(leave.getAppliedBy().getUsername()); // ✅
        dto.setEmployeeRole(leave.getAppliedBy().getRole().name()); // ✅

        return dto;
    }
}