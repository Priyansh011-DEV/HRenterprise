package HR.enterprise.controller;

import HR.enterprise.dto.LeaveRequestDTO;
import HR.enterprise.entity.LeaveStatus;
import HR.enterprise.service.EmployeeService;
import HR.enterprise.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/leaves")

public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @PostMapping("/apply")
    public ResponseEntity<?> applyLeave(
            @RequestBody LeaveRequestDTO dto,
            Principal principal) {

        return ResponseEntity.ok(
                leaveService.applyLeave(dto, principal.getName())
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam LeaveStatus status,
            Principal principal) {

        return ResponseEntity.ok(
                leaveService.updateLeaveStatus(id, status, principal.getName())
        );
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyLeaves(Principal principal) {
        return ResponseEntity.ok(
                leaveService.getMyLeaves(principal.getName())
        );
    }
}
