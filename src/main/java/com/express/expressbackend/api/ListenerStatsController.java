package com.express.expressbackend.api;

import com.express.expressbackend.domain.common.ApiResponse;
import com.express.expressbackend.domain.common.AuthUtil;
import com.express.expressbackend.domain.flag.FlagRecordRepository;
import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerRepository;
import com.express.expressbackend.domain.session.Session;
import com.express.expressbackend.domain.session.SessionRepository;
import com.express.expressbackend.domain.session.SessionStatus;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/listeners")
public class ListenerStatsController {

    private final UserRepository userRepository;
    private final ListenerRepository listenerRepository;
    private final SessionRepository sessionRepository;
    private final FlagRecordRepository flagRecordRepository;

    public ListenerStatsController(UserRepository userRepository,
                                   ListenerRepository listenerRepository,
                                   SessionRepository sessionRepository,
                                   FlagRecordRepository flagRecordRepository) {
        this.userRepository = userRepository;
        this.listenerRepository = listenerRepository;
        this.sessionRepository = sessionRepository;
        this.flagRecordRepository = flagRecordRepository;
    }

    // ✅ Get listener stats — total sessions, flag count, rating
    @GetMapping("/me/stats")
    public ApiResponse<Map<String, Object>> getMyStats() {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Listener listener = listenerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Listener profile not found"));

        List<Session> sessions = sessionRepository.findByListenerId(listener.getId());
        long totalSessions = sessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.ENDED)
                .count();

        int flagCount = listener.getRedFlagCount();

        // Simple rating: starts at 5.0, each flag reduces by 0.3, min 1.0
        double rating = Math.max(1.0, 5.0 - (flagCount * 0.3));

        return new ApiResponse<>(Map.of(
                "totalSessions", totalSessions,
                "flagCount", flagCount,
                "rating", Math.round(rating * 10.0) / 10.0,
                "isBlacklisted", listener.isBlacklisted()
        ));
    }

    // ✅ Get listener sessions (for listener history page)
    @GetMapping("/me/sessions")
    public ApiResponse<List<Map<String, Object>>> getMySessions() {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Listener listener = listenerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Listener profile not found"));

        List<Map<String, Object>> result = sessionRepository
                .findByListenerId(listener.getId())
                .stream()
                .filter(s -> s.getStatus() == SessionStatus.ENDED)
                .sorted((a, b) -> {
                    if (a.getStartedAt() == null) return 1;
                    if (b.getStartedAt() == null) return -1;
                    return b.getStartedAt().compareTo(a.getStartedAt());
                })
                .map(s -> Map.of(
                        "id", (Object) s.getId().toString(),
                        "type", s.getType().name(),
                        "status", s.getStatus().name(),
                        "startedAt", s.getStartedAt() != null ? s.getStartedAt().toString() : "",
                        "endedAt", s.getEndedAt() != null ? s.getEndedAt().toString() : ""
                ))
                .toList();

        return new ApiResponse<>(result);
    }
}