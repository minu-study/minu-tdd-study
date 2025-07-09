package io.hhplus.tdd.domain.point.model;

import io.hhplus.tdd.common.vo.TransactionType;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {
}
