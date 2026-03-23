package HR.enterprise.dto;

import lombok.Data;

import java.time.LocalDate;
@Data
public class LeaveRequestDTO {
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}
