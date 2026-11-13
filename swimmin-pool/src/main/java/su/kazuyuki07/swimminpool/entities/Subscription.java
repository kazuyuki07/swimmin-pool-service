package su.kazuyuki07.swimminpool.entities;

import jakarta.persistence.*;
import lombok.*;
import su.kazuyuki07.swimminpool.enums.Status;
import su.kazuyuki07.swimminpool.enums.SubscriptionType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Table(name = "subscription")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type")
    private SubscriptionType subscriptionType;
    private LocalDate expire;

    @OneToOne(targetEntity = Client.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(
            targetEntity = Visit.class,
            mappedBy = "visit",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<Visit> visit;

    @Column(name = "created_at")
    private LocalDate createdAt;
}
