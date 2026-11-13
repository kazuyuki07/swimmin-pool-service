package su.kazuyuki07.swimminpool.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import su.kazuyuki07.swimminpool.dto.SubscriptionDTO;
import su.kazuyuki07.swimminpool.mapper.SwimminPoolMapper;
import su.kazuyuki07.swimminpool.services.SubscriptionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final SwimminPoolMapper swimminPoolMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionDTO createSubscription(@RequestBody SubscriptionDTO subscriptionDTO) {
        return swimminPoolMapper.subscriptionToSubscriptionDTO(subscriptionService.createSubscription(subscriptionDTO));
    }

    @GetMapping("/{subscriptionId}")
    public SubscriptionDTO getSubscriptionById(@PathVariable Long subscriptionId) {
        return swimminPoolMapper.subscriptionToSubscriptionDTO(subscriptionService.getSubscriptionById(subscriptionId));
    }
}
