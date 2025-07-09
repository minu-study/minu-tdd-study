package io.hhplus.tdd.api.point.controller;

import io.hhplus.tdd.api.point.service.PointService;
import io.hhplus.tdd.domain.point.model.PointHistory;
import io.hhplus.tdd.domain.user.model.UserPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/point")
public class PointController {

    private final PointService pointService;

    @GetMapping("/{id}")
    public UserPoint point(@PathVariable long id) {
        log.info("getUserPoint id: {}", id);
        return pointService.getUserPoint(id);
    }

    @GetMapping("{id}/histories")
    public List<PointHistory> history(@PathVariable long id) {
        log.info("getPointHistory id: {}", id);
        return pointService.getPointHistory(id);
    }

    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        log.info("chargePoint id: {}, amount: {}", id, amount);
        return pointService.chargePoint(id, amount);
    }

    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        log.info("usePoint id: {}, amount: {}", id, amount);
        return pointService.usePoint(id, amount);
    }
}

