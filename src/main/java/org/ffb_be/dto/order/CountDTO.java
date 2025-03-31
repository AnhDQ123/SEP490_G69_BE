package org.ffb_be.dto.order;

import lombok.Data;

@Data
public class CountDTO {
    private int pending;
    private int processing;
    private int shipPending;
    private int shipping;
    private int delivered;
    private int cancelled;
    private int returned;
    private int rejected;
    private int returnPending;
    private int returnRejected;
}
