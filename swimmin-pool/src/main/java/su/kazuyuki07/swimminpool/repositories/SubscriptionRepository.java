package su.kazuyuki07.swimminpool.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import su.kazuyuki07.swimminpool.entities.Subscription;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByClientId(Long clientId);

    @Query(value = """
        SELECT s
        FROM Subscription s
        WHERE :clientId = s.client.id AND s.subscriptionType = 'ACTIVE'
    """)
    Optional<Subscription> findActiveSubscriptionById(@Param("clientId") Long clientId);
}
