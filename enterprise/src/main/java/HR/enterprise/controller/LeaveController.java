package HR.enterprise.controller;

import HR.enterprise.dto.LeaveRequestDTO;
import HR.enterprise.entity.LeaveStatus;
import HR.enterprise.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    // ================= APPLY LEAVE =================
    @PostMapping("/apply")
    public ResponseEntity<?> applyLeave(
            @RequestBody LeaveRequestDTO dto,
            Principal principal) {

        return ResponseEntity.ok(
                leaveService.applyLeave(dto, principal.getName())
        );
    }

    // ================= APPROVE / REJECT =================
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam LeaveStatus status,
            Principal principal) {

        return ResponseEntity.ok(
                leaveService.updateLeaveStatus(id, status, principal.getName())
        );
    }

    // ================= MY LEAVES =================
    @GetMapping("/my")
    public ResponseEntity<?> getMyLeaves(Principal principal) {

        return ResponseEntity.ok(
                leaveService.getMyLeaves(principal.getName())
        );
    }

    // ================= ROLE-BASED ALL LEAVES =================
    @GetMapping("/all")
    public ResponseEntity<?> getAllLeaves(Principal principal) {

        return ResponseEntity.ok(
                leaveService.getAllLeaves(principal.getName())
        );
    }
}