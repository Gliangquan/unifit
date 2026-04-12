package com.jcen.unifit.model.dto.user;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceRechargeRequest {

    /**
     * 模拟充值金额，默认100
     */
    private BigDecimal amount;
}
