package com.express.expressbackend.api;

import com.express.expressbackend.domain.flag.CreateFlagRequest;
import com.express.expressbackend.domain.flag.FlagService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flags")
public class FlagController {

    private final FlagService flagService;

    public FlagController(FlagService flagService) {
        this.flagService = flagService;
    }

    @PostMapping
    public void createFlag(@RequestBody CreateFlagRequest request) {
        flagService.createFlag(request);
    }
}