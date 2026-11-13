package su.kazuyuki07.swimminpool.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import su.kazuyuki07.swimminpool.dto.ClientDTO;
import su.kazuyuki07.swimminpool.dto.PagedResponse;
import su.kazuyuki07.swimminpool.dto.SubscriptionDTO;
import su.kazuyuki07.swimminpool.mapper.SwimminPoolMapper;
import su.kazuyuki07.swimminpool.services.ClientService;
import su.kazuyuki07.swimminpool.services.SubscriptionService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clients")
public class ClientController {
    private final ClientService clientService;
    private final SubscriptionService subscriptionService;
    private final SwimminPoolMapper swimminPoolMapper;

    @GetMapping
    public PagedResponse<ClientDTO> getClients(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) List<String> firstNames,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) List<String> lastNames,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) List<String> surnames,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) LocalDate birthDate,
            @RequestParam(required = false) UUID subscriptionId,
            @RequestParam(required = false) List<UUID> subscriptionIds,
            @RequestParam(defaultValue = "first_name") String sort) {
        return clientService.getClients(
                page,
                size,
                firstName,
                firstNames,
                lastName,
                lastNames,
                surname,
                surnames,
                phoneNumber,
                birthDate,
                subscriptionId,
                subscriptionIds,
                sort
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDTO createClient(@RequestBody ClientDTO clientDTO) {
        return swimminPoolMapper.clientToClientDTO(clientService.createClient(clientDTO));
    }

    @GetMapping("/{clientId}")
    public ClientDTO getClientByClientId(@PathVariable Long clientId) {
        return swimminPoolMapper.clientToClientDTO(clientService.getClientById(clientId));
    }

    @PutMapping("/{clientId}")
    public ClientDTO fullUpdateClientByClientId(@PathVariable Long clientId, @RequestBody ClientDTO clientDTO) {
        return swimminPoolMapper.clientToClientDTO(clientService.fullUpdateClientByClientId(clientId, clientDTO));
    }

    @GetMapping("/{clientId}/subscriptions/active")
    public SubscriptionDTO getActiveSubscriptionByClientId(@PathVariable Long clientId) {
        return swimminPoolMapper.subscriptionToSubscriptionDTO(subscriptionService.getActiveSubscriptionByClientId(clientId));
    }

    @GetMapping("/{clientId}/subscriptions")
    public SubscriptionDTO getSubscriptionByClientId(@PathVariable Long clientId) {
        return swimminPoolMapper.subscriptionToSubscriptionDTO(subscriptionService.getSubscriptionByClientId(clientId));
    }
}
