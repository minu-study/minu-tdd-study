package io.hhplus.tdd;

import io.hhplus.tdd.api.point.service.PointService;
import io.hhplus.tdd.common.vo.TransactionType;
import io.hhplus.tdd.domain.point.PointHistoryTable;
import io.hhplus.tdd.domain.user.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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


}
