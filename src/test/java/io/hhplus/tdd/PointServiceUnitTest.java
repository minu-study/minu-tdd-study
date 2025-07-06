package io.hhplus.tdd;


import io.hhplus.tdd.api.point.service.PointService;
import io.hhplus.tdd.domain.point.PointHistoryTable;
import io.hhplus.tdd.domain.user.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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





}
