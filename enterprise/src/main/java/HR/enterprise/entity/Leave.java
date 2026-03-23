package HR.enterprise.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String leaveType; // SICK, CASUAL, PAID

    private LocalDate startDate;
    private LocalDate endDate;

    private String reason;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // PENDING, APPROVED, REJECTED

    // ✅ Any role (EMPLOYEE, HR, ADMIN) can apply leave
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User appliedBy;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    private String approvedBy;
    private LocalDateTime approvedAt;
}