package io.hhplus.tdd;


import io.hhplus.tdd.api.point.service.PointService;
import io.hhplus.tdd.common.vo.TransactionType;
import io.hhplus.tdd.domain.point.PointHistoryTable;
import io.hhplus.tdd.domain.user.UserPointTable;
import io.hhplus.tdd.domain.user.model.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class PointServiceUnitTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointService pointService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("사용자의 포인트를 조회한다")
    void getUserPoint() {

        // given
        long userId = 1L;
        UserPoint expectedPoint = new UserPoint(userId, 0L, System.currentTimeMillis());
        given(userPointTable.selectById(userId)).willReturn(expectedPoint);

        // when
        UserPoint userPoint = pointService.getUserPoint(userId);

        // then
        assertThat(userPoint.point()).isZero();
        verify(userPointTable).selectById(userId);
    }

    @Test
    @DisplayName("포인트를 충전한다")
    void chargePoint() {
        // given
        long userId = 1L;
        long beforeAmount = 1000L;
        long chargeAmount = 500L;
        UserPoint beforePoint = new UserPoint(userId, beforeAmount, System.currentTimeMillis());
        UserPoint afterPoint = new UserPoint(userId, beforeAmount + chargeAmount, System.currentTimeMillis());
        given(userPointTable.selectById(userId)).willReturn(beforePoint);
        given(userPointTable.insertOrUpdate(userId, beforeAmount + chargeAmount)).willReturn(afterPoint);

        // when
        UserPoint userPoint = pointService.chargePoint(userId, chargeAmount);

        // then
        assertThat(userPoint.point()).isEqualTo(beforeAmount + chargeAmount);
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, beforeAmount + chargeAmount);
        verify(pointHistoryTable).insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
    }



}
