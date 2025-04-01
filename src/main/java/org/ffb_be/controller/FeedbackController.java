package org.ffb_be.controller;



import org.ffb_be.dto.feedback.FeedbackDTO;
import org.ffb_be.service.feedback.FeedbackService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;

    }

    @GetMapping("/product/{id}")
    public ResponseEntity<?> getAllByProduct(@PathVariable Long id,
                                             @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                             @RequestParam(value = "size", defaultValue = "20", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page-1, size);
        return ResponseEntity.ok(feedbackService.findAll(id,pageable) );
    }

    @PostMapping("/create")
    public void create(@RequestParam("userId") Long userId,
                                    @RequestParam("productId") Long productId,
                                    @RequestBody FeedbackDTO feedbackDTO) {
        feedbackService.create(userId,productId,feedbackDTO);
    }
}