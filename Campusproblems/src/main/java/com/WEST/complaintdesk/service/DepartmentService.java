package com.WEST.complaintdesk.service;

import com.WEST.complaintdesk.dto.request.DepartmentRequest;
import com.WEST.complaintdesk.dto.response.DepartmentResponse;
import com.WEST.complaintdesk.entity.Department;
import com.WEST.complaintdesk.exception.BadRequestException;
import com.WEST.complaintdesk.exception.ResourceNotFoundException;
import com.WEST.complaintdesk.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Department code '" + request.getCode() + "' is already in use");
        }
        if (departmentRepository.existsByName(request.getName())) {
            throw new BadRequestException("Department name '" + request.getName() + "' is already in use");
        }

        Department dept = Department.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .build();

        return DepartmentResponse.from(departmentRepository.save(dept));
    }

    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::from)
                .collect(Collectors.toList());
    }

    public DepartmentResponse getById(Long id) {
        return DepartmentResponse.from(findEntityById(id));
    }

    public Department findEntityById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department dept = findEntityById(id);
        dept.setName(request.getName());
        dept.setCode(request.getCode().toUpperCase());
        dept.setDescription(request.getDescription());
        return DepartmentResponse.from(departmentRepository.save(dept));
    }

    public void delete(Long id) {
        Department dept = findEntityById(id);
        departmentRepository.delete(dept);
    }
}
