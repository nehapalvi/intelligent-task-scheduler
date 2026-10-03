package com.task.scheduler.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.task.scheduler.dto.SchedulerStatusResponse;
import com.task.scheduler.scheduler.SchedulingAlgorithmType;
import com.task.scheduler.service.SchedulerControlService;

@WebMvcTest(SchedulerController.class)
class SchedulerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchedulerControlService schedulerControlService;

    @Test
    void shouldExposeStartStopAndStatusEndpoints() throws Exception {
        when(schedulerControlService.start()).thenReturn(statusResponse("RUNNING"));
        when(schedulerControlService.stop()).thenReturn(statusResponse("STOPPED"));
        when(schedulerControlService.getStatus()).thenReturn(statusResponse("STOPPED"));

        mockMvc.perform(post("/api/scheduler/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"));

        mockMvc.perform(post("/api/scheduler/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STOPPED"));

        mockMvc.perform(get("/api/scheduler/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm").value("FCFS"))
                .andExpect(jsonPath("$.workerCount").value(3))
                .andExpect(jsonPath("$.timeQuantum").value(2));
    }

    @Test
    void shouldAcceptAlgorithmWorkerAndQuantumConfiguration() throws Exception {
        when(schedulerControlService.changeAlgorithm(any())).thenReturn(statusResponse("STOPPED"));
        when(schedulerControlService.configureWorkers(5)).thenReturn(statusResponse("STOPPED"));
        when(schedulerControlService.configureTimeQuantum(4)).thenReturn(statusResponse("STOPPED"));

        mockMvc.perform(put("/api/scheduler/algorithm")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"algorithm\":\"ROUND_ROBIN\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/scheduler/workers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"workerCount\":5}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/scheduler/time-quantum")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"timeQuantum\":4}"))
                .andExpect(status().isOk());

        verify(schedulerControlService).changeAlgorithm(SchedulingAlgorithmType.ROUND_ROBIN);
        verify(schedulerControlService).configureWorkers(5);
        verify(schedulerControlService).configureTimeQuantum(4);
    }

    @Test
    void shouldRejectInvalidConfigurationRequests() throws Exception {
        mockMvc.perform(put("/api/scheduler/algorithm")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/scheduler/workers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"workerCount\":0}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/scheduler/time-quantum")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"timeQuantum\":0}"))
                .andExpect(status().isBadRequest());
    }

    private SchedulerStatusResponse statusResponse(String status) {
        return new SchedulerStatusResponse(status, SchedulingAlgorithmType.FCFS, 3, 2);
    }
}