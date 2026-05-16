package com.bravo.brain.service;

import com.bravo.brain.domain.entity.Department;
import com.bravo.brain.domain.repository.DepartmentRepository;
import com.bravo.brain.model.dto.DepartmentDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository repo;

    public DepartmentResponse createDepartment(CreateRequest req) {
        if (repo.existsByNameAndStoreName(req.getName(), req.getStoreName()))
            throw new RuntimeException("Bu şöbə artıq mövcuddur: " + req.getName());

        Department dept = Department.builder()
                .name(req.getName())
                .storeName(req.getStoreName())
                .imageBase64(req.getImageBase64())
                .active(true)
                .build();

        return toResponse(repo.save(dept));
    }

    public List<DepartmentResponse> getByStore(String storeName) {
        return repo.findByStoreNameAndActiveTrue(storeName).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void deactivate(Long id) {
        Department dept = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Şöbə tapılmadı"));
        dept.setActive(false);
        repo.save(dept);
    }

    private DepartmentResponse toResponse(Department d) {
        return new DepartmentResponse(
                d.getId(), d.getName(), d.getStoreName(),
                d.getImageBase64(), d.isActive(), d.getCreatedAt()
        );
    }
}