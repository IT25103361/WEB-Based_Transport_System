package com.seproject.courier.dto.partner;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineStatusRequest {

    @NotNull(message = "isOnline is required")
    private Boolean isOnline;
}