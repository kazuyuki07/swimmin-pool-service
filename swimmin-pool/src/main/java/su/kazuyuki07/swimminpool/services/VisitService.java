package su.kazuyuki07.swimminpool.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import su.kazuyuki07.swimminpool.dto.PagedResponse;
import su.kazuyuki07.swimminpool.dto.VisitDTO;
import su.kazuyuki07.swimminpool.entities.Visit;
import su.kazuyuki07.swimminpool.exceptions.NotFoundException;
import su.kazuyuki07.swimminpool.mapper.SwimminPoolMapper;
import su.kazuyuki07.swimminpool.repositories.VisitRepository;
import su.kazuyuki07.swimminpool.repositories.specifications.VisitSpecification;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;
    private final SwimminPoolMapper swimminPoolMapper;

    public Visit registerVisit(VisitDTO visitDTO) {
        return visitRepository.save(swimminPoolMapper.visitDTOToVisit(visitDTO));
    }

    public Visit registerOutVisit(Long id, LocalDateTime checkOutTime) {
        Visit foundVisit = visitRepository.findById(id).orElseThrow(() -> new NotFoundException("Данное посещение не найдено"));
        foundVisit.setCheckOutTime(checkOutTime);

        return visitRepository.save(foundVisit);
    }

    public PagedResponse<VisitDTO> getVisits(
            Integer page,
            Integer size,
            LocalDateTime createdAtFrom,
            LocalDateTime createdAtTo) {
        Specification<Visit> visitSpecification = Specification.where(
                VisitSpecification.createdAtFilter(createdAtFrom, createdAtTo));

        Page<Visit> visits = visitRepository.findAll(visitSpecification, PageRequest.of(page, size));

        return PagedResponse.from(visits.map(swimminPoolMapper::visitToVisitDTO));
    }
}
