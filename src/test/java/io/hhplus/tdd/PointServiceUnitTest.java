package io.hhplus.tdd;


import io.hhplus.tdd.api.point.service.PointService;
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



}
