package ru.strcss.test.cci.dto;

import lombok.Data;

/**
 * Dummy Request for testing Spring rqStates Machine
 * <p>
 * Created by Stormcss
 * Date: 24.11.2018
 */
@Data
public class Request {
    private Long id;
    private Long userId;
    private Long price;
    private Long statusId;
    private String closeReason;
}
