package com.task.scheduler.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.task.scheduler.dto.ChangeAlgorithmRequest;
import com.task.scheduler.dto.SchedulerStatusResponse;
import com.task.scheduler.dto.TimeQuantumRequest;
import com.task.scheduler.dto.WorkerConfigurationRequest;
import com.task.scheduler.service.SchedulerControlService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/scheduler")
@Validated
public class SchedulerController {

    private final SchedulerControlService schedulerControlService;

    public SchedulerController(SchedulerControlService schedulerControlService) {
        this.schedulerControlService = schedulerControlService;
    }

    @PostMapping("/start")
    public SchedulerStatusResponse start() {
        return schedulerControlService.start();
    }

    @PostMapping("/stop")
    public SchedulerStatusResponse stop() {
        return schedulerControlService.stop();
    }

    @GetMapping("/status")
    public SchedulerStatusResponse getStatus() {
        return schedulerControlService.getStatus();
    }

    @PutMapping("/algorithm")
    public SchedulerStatusResponse changeAlgorithm(@Valid @RequestBody ChangeAlgorithmRequest request) {
        return schedulerControlService.changeAlgorithm(request.getAlgorithm());
    }

    @PutMapping("/workers")
    public SchedulerStatusResponse configureWorkers(@Valid @RequestBody WorkerConfigurationRequest request) {
        return schedulerControlService.configureWorkers(request.getWorkerCount());
    }

    @PutMapping("/time-quantum")
    public SchedulerStatusResponse configureTimeQuantum(@Valid @RequestBody TimeQuantumRequest request) {
        return schedulerControlService.configureTimeQuantum(request.getTimeQuantum());
    }
}