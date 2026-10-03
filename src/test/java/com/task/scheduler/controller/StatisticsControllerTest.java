package com.task.scheduler.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.task.scheduler.dto.StatisticsResponse;
import com.task.scheduler.service.StatisticsService;

@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsService statisticsService;

    @Test
    void shouldReturnStatisticsResponse() throws Exception {
        when(statisticsService.getStatistics()).thenReturn(
                new StatisticsResponse(4.2, 8.5, 2.1, 10.4, 87, 2)
        );

        mockMvc.perform(get("/api/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageWaitingTime").value(4.2))
                .andExpect(jsonPath("$.averageTurnaroundTime").value(8.5))
                .andExpect(jsonPath("$.averageResponseTime").value(2.1))
                .andExpect(jsonPath("$.throughput").value(10.4))
                .andExpect(jsonPath("$.completedTasks").value(87))
                .andExpect(jsonPath("$.failedTasks").value(2));
    }
}