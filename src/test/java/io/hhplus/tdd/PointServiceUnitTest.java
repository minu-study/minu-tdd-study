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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    @DisplayName("0 이하의 금액으로 충전할 수 없다")
    void chargeNegativeAmount() {
        assertThatThrownBy(() -> pointService.chargePoint(1L, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 0보다 커야 합니다.");
    }


    @Test
    @DisplayName("포인트를 사용한다")
    void usePoint() {

        // given
        long userId = 1L;
        long beforeAmount = 1000L;
        long useAmount = 400L;
        UserPoint beforePoint = new UserPoint(userId, beforeAmount, System.currentTimeMillis());
        UserPoint afterPoint = new UserPoint(userId, beforeAmount - useAmount, System.currentTimeMillis());
        given(userPointTable.selectById(userId)).willReturn(beforePoint);
        given(userPointTable.insertOrUpdate(userId, beforeAmount - useAmount)).willReturn(afterPoint);

        // when
        UserPoint userPoint = pointService.usePoint(userId, useAmount);

        // then
        assertThat(userPoint.point()).isEqualTo(beforeAmount - useAmount);
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, beforeAmount - useAmount);
        verify(pointHistoryTable).insert(eq(userId), eq(useAmount), eq(TransactionType.USE), anyLong());
    }

    @Test
    @DisplayName("0 이하의 포인트로 사용할 수 없다")
    void useNegativeAmount() {
        assertThatThrownBy(() -> pointService.usePoint(1L, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용 포인트는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("보유 포인트가 부족할 때 포인트를 사용할 수 없다")
    void insufficientAmount() {
        // given
        long userId = 1L;
        long beforeAmount = 100L;
        long useAmount = 200L;
        UserPoint beforePoint = new UserPoint(userId, beforeAmount, System.currentTimeMillis());
        given(userPointTable.selectById(userId)).willReturn(beforePoint);

        // when & then
        assertThatThrownBy(() -> pointService.usePoint(userId, useAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트가 부족합니다.");
    }


}
