package com.Finanza.backend.gemini;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summarize")
public class SummarizeController {

    @Autowired
    private GeminiService GeminiService;

    @PostMapping
    public ResponseEntity<?> summarize(@RequestBody SummarizeRequest request) {
        return GeminiService.summarize(request);
    }
}
