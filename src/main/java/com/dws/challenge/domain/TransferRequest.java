package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull
    @NotEmpty
    private final String fromAccountId;

    @NotNull
    @NotEmpty
    private final String toAccountId;

    @NotNull
    @Min(value = 1, message = "Transfer amount must be positive number.")
    private BigDecimal amount;

    public TransferRequest(String fromAccountId, String toAccountId) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = BigDecimal.ZERO;
    }

    @JsonCreator
    public TransferRequest(@JsonProperty("fromAccountId") String fromAccountId,
                           @JsonProperty("toAccountId") String toAccountId,
                           @JsonProperty("amount") BigDecimal amount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }


}
