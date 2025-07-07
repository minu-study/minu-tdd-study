package io.hhplus.tdd.api.point.service;

import io.hhplus.tdd.common.vo.TransactionType;
import io.hhplus.tdd.domain.point.PointHistoryTable;
import io.hhplus.tdd.domain.user.UserPointTable;
import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.user.model.UserPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    private final ConcurrentHashMap<Long, Lock> userLockMap = new ConcurrentHashMap<>();

    private Lock getLock(long userId) {
        return userLockMap.computeIfAbsent(userId, id -> new ReentrantLock());
    }


    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    public List<PointHistory> getPointHistory(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    public UserPoint chargePoint(long userId, long amount) {

        if (amount < 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        Lock lock = getLock(userId);
        lock.lock();

        try {
            UserPoint currentPoint = userPointTable.selectById(userId);
            UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, currentPoint.point() + amount);

            pointHistoryTable.insert(
                    userId,
                    amount,
                    TransactionType.CHARGE,
                    System.currentTimeMillis()
            );
            return updatedPoint;
        } finally {
            lock.unlock();
        }

    }

    public UserPoint usePoint(long userId, long amount) {

        if (amount < 0) {
            throw new IllegalArgumentException("사용 포인트는 0보다 커야 합니다.");
        }

        Lock lock = getLock(userId);
        lock.lock();

        try {
            UserPoint currentPoint = userPointTable.selectById(userId);
            if (currentPoint.point() < amount) {
                throw new IllegalArgumentException("포인트가 부족합니다.");
            }
            UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, currentPoint.point() - amount);

            pointHistoryTable.insert(
                    userId,
                    amount,
                    TransactionType.USE,
                    System.currentTimeMillis()
            );
            return updatedPoint;
        } finally {
            lock.unlock();
        }

    }

}



