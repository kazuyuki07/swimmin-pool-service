package su.kazuyuki07.swimminpool.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import su.kazuyuki07.swimminpool.dto.ClientDTO;
import su.kazuyuki07.swimminpool.dto.SubscriptionDTO;
import su.kazuyuki07.swimminpool.dto.VisitDTO;
import su.kazuyuki07.swimminpool.entities.Client;
import su.kazuyuki07.swimminpool.entities.Subscription;
import su.kazuyuki07.swimminpool.entities.Visit;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        imports = { LocalDateTime.class, LocalDate.class }
)
public interface SwimminPoolMapper {
    @Mapping(target = "id", ignore = true)
    VisitDTO visitToVisitDTO(Visit visit);

    Visit visitDTOToVisit(VisitDTO visitDTO);

    @Mapping(target = "id", ignore = true)
    ClientDTO clientToClientDTO(Client client);

    Client clientDTOToClient(ClientDTO clientDTO);

    void fullUpdateClient(ClientDTO clientDTO, @MappingTarget Client client);

    @Mapping(target = "id", ignore = true)
    SubscriptionDTO subscriptionToSubscriptionDTO(Subscription subscription);

    Subscription subscriptionDTOToSubscription(SubscriptionDTO subscriptionDTO);
}