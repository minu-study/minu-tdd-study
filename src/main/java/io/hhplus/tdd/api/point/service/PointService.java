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
        return null;
    }

    public UserPoint usePoint(long userId, long amount) {
        return null;
    }

}



