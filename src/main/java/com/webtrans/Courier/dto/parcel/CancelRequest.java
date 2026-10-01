package com.webtrans.Courier.dto.parcel;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequest {

    /** One or more reasons picked from checkboxes/dropdown in the UI. */
    private List<String> reasons;

    /** Optional free-text the user typed. */
    @Size(max = 300)
    private String note;
}