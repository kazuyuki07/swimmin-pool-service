package su.kazuyuki07.swimminpool.services;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Service;
import su.kazuyuki07.swimminpool.dto.SubscriptionDTO;
import su.kazuyuki07.swimminpool.entities.Subscription;
import su.kazuyuki07.swimminpool.exceptions.NotFoundException;
import su.kazuyuki07.swimminpool.mapper.SwimminPoolMapper;
import su.kazuyuki07.swimminpool.repositories.SubscriptionRepository;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SwimminPoolMapper swimminPoolMapper;

    public Subscription createSubscription(SubscriptionDTO subscriptionDTO) {
        Subscription newSubscription = swimminPoolMapper.subscriptionDTOToSubscription(subscriptionDTO);
        return subscriptionRepository.save(newSubscription);
    }

    public Subscription getSubscriptionByClientId(Long clientId) {
        return subscriptionRepository.findByClientId(clientId).orElseThrow(() -> new NotFoundException("Не найден клиент"));
    }

    public Subscription getSubscriptionById(UUID id) {
        return subscriptionRepository.findById(id).orElseThrow(() -> new NotFoundException("Не найдена подписка"));
    }

    public Subscription getActiveSubscriptionByClientId(Long clientId) {
        return subscriptionRepository.findActiveSubscriptionById(clientId).orElseThrow(() -> new NotFoundException("Не найден активный абонемент"));
    }
}
