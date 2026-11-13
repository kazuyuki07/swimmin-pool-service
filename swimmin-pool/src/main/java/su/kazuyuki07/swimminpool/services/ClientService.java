package su.kazuyuki07.swimminpool.services;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import su.kazuyuki07.swimminpool.dto.ClientDTO;
import su.kazuyuki07.swimminpool.dto.PagedResponse;
import su.kazuyuki07.swimminpool.entities.Client;
import su.kazuyuki07.swimminpool.enums.ClientSort;
import su.kazuyuki07.swimminpool.exceptions.NotFoundException;
import su.kazuyuki07.swimminpool.mapper.SwimminPoolMapper;
import su.kazuyuki07.swimminpool.repositories.ClientRepository;
import su.kazuyuki07.swimminpool.repositories.specifications.ClientSpecification;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    private final SwimminPoolMapper swimminPoolMapper;

    public Client createClient(ClientDTO clientDTO) {
        Client newClient = swimminPoolMapper.clientDTOToClient(clientDTO);
        return clientRepository.save(newClient);
    }

    public Client getClientById(Long clientId) {
        return clientRepository.findById(clientId).orElseThrow(() -> new NotFoundException("Клиент не найден"));
    }

    public PagedResponse<ClientDTO> getClients(
            Integer page,
            Integer size,
            String firstName,
            List<String> firstNames,
            String lastName,
            List<String> lastNames,
            String surname,
            List<String> surnames,
            String phoneNumber,
            LocalDate birthDate,
            UUID subscriptionId,
            List<UUID> subscriptionIds,
            String sort) {
        Specification<Client> clientSpecification = Specification.where(
                ClientSpecification.firstNameFilter(firstName))
                .and(ClientSpecification.firstNamesFilter(firstNames))
                .and(ClientSpecification.lastNameFilter(lastName))
                .and(ClientSpecification.lastNamesFilter(lastNames))
                .and(ClientSpecification.surnameFilter(surname))
                .and(ClientSpecification.surnamesFilter(surnames))
                .and(ClientSpecification.phoneNumberFilter(phoneNumber))
                .and(ClientSpecification.birthDateFilter(birthDate))
                .and(ClientSpecification.subscriptionIdFilter(subscriptionId))
                .and(ClientSpecification.subscriptionIdsFilter(subscriptionIds));

        ClientSort clientSort = ClientSort.of(sort);
        Sort sorted = Sort.by(
                clientSort.getDirection(),
                clientSort.getEntityValue()
        );

        Page<Client> clients = clientRepository.findAll(
                clientSpecification,
                PageRequest.of(page, size, sorted)
        );

        return PagedResponse.from(
                clients.map(swimminPoolMapper::clientToClientDTO)
        );
    }

    public Client fullUpdateClientByClientId(Long clientId, ClientDTO clientDTO) {
        Client foundClient = clientRepository.findById(clientId).orElseThrow(() -> new NotFoundException("Клиент не найден"));

        swimminPoolMapper.fullUpdateClient(clientDTO, foundClient);

        return clientRepository.save(foundClient);
    }
}
