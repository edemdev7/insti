package com.payiskoul.institution.organization.dto;


import java.util.List;

public record PaginatedInstitutionResponse(
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<InstitutionResponse> institutions
) {}
