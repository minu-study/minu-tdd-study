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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;


    @BeforeEach
    void setUp() {
        userPointTable.insertOrUpdate(1L, 1000L);
        pointHistoryTable.insert(1L, 1000L, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryTable.insert(1L, 500L, TransactionType.USE, System.currentTimeMillis());
    }

    @Test
    @DisplayName("사용자의 포인트를 조회한다.")
    void getUserPoint() throws Exception {
        mockMvc.perform(get("/point/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.point").value(1000L));
    }

    @Test
    @DisplayName("사용자의 포인트 내역을 조회한다.")
    void getPointHistory() throws Exception {
        mockMvc.perform(get("/point/{id}/histories", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].amount").value(1000L))
                .andExpect(jsonPath("$[0].type").value(TransactionType.CHARGE.name()))
                .andExpect(jsonPath("$[1].userId").value(1L))
                .andExpect(jsonPath("$[1].amount").value(500L))
                .andExpect(jsonPath("$[1].type").value(TransactionType.USE.name()));
    }

    @Test
    @DisplayName("사용자의 포인트를 충전한다.")
    void chargePoint() throws Exception {
        mockMvc.perform(patch("/point/{id}/charge", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("500"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.point").value(1500L));

        UserPoint userPoint = userPointTable.selectById(1L);
        assertThat(userPoint.point()).isEqualTo(1500L);

        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(1L);
        assertThat(histories.stream()
                .anyMatch(h -> h.amount() == 500L && h.type() == TransactionType.CHARGE)).isTrue();
    }

    @Test
    @DisplayName("사용자의 포인트를 사용한다.")
    void usePoint() throws Exception {
        mockMvc.perform(patch("/point/{id}/use", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("300"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.point").value(700L));

        // 실제 Table에 반영됐는지 확인
        UserPoint userPoint = userPointTable.selectById(1L);
        assertThat(userPoint.point()).isEqualTo(700L);

        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(1L);
        assertThat(histories.stream()
                .anyMatch(h -> h.amount() == 300L && h.type() == TransactionType.USE)).isTrue();
    }


}
