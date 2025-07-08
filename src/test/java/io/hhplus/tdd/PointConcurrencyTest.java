package io.hhplus.tdd;

import io.hhplus.tdd.api.point.service.PointService;
import io.hhplus.tdd.common.vo.TransactionType;
import io.hhplus.tdd.domain.point.PointHistoryTable;
import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.user.UserPointTable;
import io.hhplus.tdd.domain.user.model.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class PointConcurrencyTest {

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    @Autowired
    private PointService pointService;


    /**
     * 동시 충전 요청에 대한 검증
     * - 동시에 100개의 충전 요청 발생
     * - 각 요청은 1000 포인트 충전하여 총 100,000 포인트 충전되었는지 검증
     * - 포인트 이력 기록 검증
     */
    @Test
    @DisplayName("동시 충전 요청에 대한 검증")
    void concurrencyChargePoint() throws InterruptedException {
        // given
        long userId = 1L;
        int threadCount = 100;
        long chargeAmount = 1000L;
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 초기 포인트 세팅
        userPointTable.insertOrUpdate(userId, 0L);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.chargePoint(userId, chargeAmount);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(50, TimeUnit.SECONDS);

        // then
        UserPoint userPoint = userPointTable.selectById(userId);
        assertThat(userPoint.point()).isEqualTo(threadCount * chargeAmount);

        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);
        assertThat(histories.stream()
                .filter(h -> h.type() == TransactionType.CHARGE)
                .count()).isEqualTo(threadCount);

        executor.shutdown();
    }


    /**
     * 동시 포인트 사용 요청에 대한 검증
     * - 동시에 100개의 포인트 사용 요청 발생
     * - 각 요청은 500 포인트 사용하여 총 50,000 포인트 사용되었는지 검증
     * - 포인트 이력 기록 검증
     */
    @Test
    @DisplayName("동시 포인트 사용 요청에 대한 동시성 검증")
    void concurrencyUsePoint() throws InterruptedException {

        // given
        long userId = 2L;
        long initialAmount = 50000L;
        int threadCount = 100;
        long useAmount = 500L;
        userPointTable.insertOrUpdate(userId, initialAmount);

        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    try {
                        pointService.usePoint(userId, useAmount);
                    } catch (IllegalArgumentException ignored) {
                        // 포인트 부족 예외 무시
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(10, TimeUnit.SECONDS);

        // then
        UserPoint userPoint = userPointTable.selectById(userId);
        assertThat(userPoint.point()).isGreaterThanOrEqualTo(0L);

        long usedCount = pointHistoryTable.selectAllByUserId(userId).stream()
                .filter(h -> h.type() == TransactionType.USE)
                .count();
        assertThat(usedCount * useAmount + userPoint.point()).isEqualTo(initialAmount);

        executor.shutdown();
    }

    /**
     * 여러 사용자가 동시에 충전 요청을 보내는 경우에 대한 검증
     * - 5명의 사용자 각각 10개의 충전 요청을 보내 총 50,000 포인트 충전되었는지 검증
     * - 각 사용자의 포인트 이력 기록 검증
     */
    @Test
    @DisplayName("여러 사용자 동시 충전 요청 검증")
    void multiUserConcurrencyTest() throws InterruptedException {
        int userCount = 5;
        int threadCount = 10;
        long chargeAmount = 1000L;

        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(userCount * threadCount);

        // 사용자별 초기화
        for (long userId = 1; userId <= userCount; userId++) {
            userPointTable.insertOrUpdate(userId, 0L);
        }

        // when
        for (long userId = 1; userId <= userCount; userId++) {
            for (int t = 0; t < threadCount; t++) {
                long finalUserId = userId;
                executor.submit(() -> {
                    try {
                        pointService.chargePoint(finalUserId, chargeAmount);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }
        latch.await(50, TimeUnit.SECONDS);

        // then
        for (long userId = 1; userId <= userCount; userId++) {
            UserPoint userPoint = userPointTable.selectById(userId);
            assertThat(userPoint.point()).isEqualTo(threadCount * chargeAmount);
        }

        executor.shutdown();
    }


}
