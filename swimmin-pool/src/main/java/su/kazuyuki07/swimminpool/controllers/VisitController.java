package su.kazuyuki07.swimminpool.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import su.kazuyuki07.swimminpool.dto.PagedResponse;
import su.kazuyuki07.swimminpool.dto.VisitDTO;
import su.kazuyuki07.swimminpool.mapper.SwimminPoolMapper;
import su.kazuyuki07.swimminpool.services.VisitService;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/visits")
public class VisitController {
    private final VisitService visitService;
    private final SwimminPoolMapper swimminPoolMapper;

    @PostMapping("/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public VisitDTO checkInRegister(@RequestBody VisitDTO visitDTO) {
        return swimminPoolMapper.visitToVisitDTO(visitService.registerVisit(visitDTO));
    }

    @PostMapping("/{visitId}/check-out")
    public VisitDTO checkOutRegister(@PathVariable Long visitId, @RequestParam LocalDateTime checkOutTime) {
        return swimminPoolMapper.visitToVisitDTO(visitService.registerOutVisit(visitId, checkOutTime));
    }

    @GetMapping
    public PagedResponse<VisitDTO> getVisits(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) LocalDateTime createdAtFrom,
            @RequestParam(required = false) LocalDateTime createdAtTo) {
        return visitService.getVisits(page, size, createdAtFrom, createdAtTo);
    }
}
