package com.express.expressbackend.domain.review;

import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerRepository;
import com.express.expressbackend.domain.session.Session;
import com.express.expressbackend.domain.session.SessionRepository;
import com.express.expressbackend.domain.session.SessionStatus;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SessionRepository sessionRepository;
    private final ListenerRepository listenerRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         SessionRepository sessionRepository,
                         ListenerRepository listenerRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.sessionRepository = sessionRepository;
        this.listenerRepository = listenerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void submitReview(String userEmail, ReviewRequest request) {

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != SessionStatus.ENDED) {
            throw new RuntimeException("Can only review ended sessions");
        }

        // Prevent duplicate reviews for same session
        if (reviewRepository.findBySessionId(request.getSessionId()).isPresent()) {
            throw new RuntimeException("You have already reviewed this session");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify session belongs to this user
        if (!session.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only review your own sessions");
        }

        Listener listener = session.getListener();

        Review review = new Review();
        review.setSession(session);
        review.setListener(listener);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        // ✅ Update listener's average rating in their record
        List<Review> allReviews = reviewRepository
                .findByListenerIdOrderByCreatedAtDesc(listener.getId());
        double avgRating = allReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(5.0);
        listener.setAverageRating(avgRating);
        listenerRepository.save(listener);
    }

    public List<ReviewResponse> getListenerReviews(UUID listenerId) {
        return reviewRepository
                .findByListenerIdOrderByCreatedAtDesc(listenerId)
                .stream()
                .map(r -> new ReviewResponse(
                        r.getId(),
                        r.getRating(),
                        r.getComment(),
                        r.getUser().getPublicDisplayId(),
                        r.getSession().getId(),
                        r.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}