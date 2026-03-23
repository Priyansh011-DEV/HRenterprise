package HR.enterprise.dto;

import lombok.Data;

import java.time.LocalDate;
@Data
public class LeaveResponseDTO {
    private Long id;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String employeeName;
    private String employeeRole;
}
