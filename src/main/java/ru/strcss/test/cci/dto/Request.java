package ru.strcss.test.cci.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Dummy Request for testing Spring rqStates Machine
 * <p>
 * Created by Stormcss
 * Date: 24.11.2018
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    private Long id;
    private Long userId;
    private Long price;
    private Long statusId;
    private LocalDateTime approveEndDate;
    private String closeReason;
}
