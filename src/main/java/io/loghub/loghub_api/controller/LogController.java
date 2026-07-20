package io.loghub.loghub_api.controller;

import io.loghub.loghub_api.dto.*;
import io.loghub.loghub_api.service.LogEventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Validated
@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogEventService service;

    public LogController(LogEventService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<LogEventResponse> ingest(@Valid @RequestBody LogEvent logEvent) {
        LogEventResponse response = service.ingest(logEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<LogEventResponse>> search(
            @RequestParam(required = false) String application,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) LogLevel level,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        PageResponse<LogEventResponse> response = service.search(
                application,
                environment,
                level,
                from,
                to,
                page,
                size
        );
        return ResponseEntity.ok(response);
    }
}

