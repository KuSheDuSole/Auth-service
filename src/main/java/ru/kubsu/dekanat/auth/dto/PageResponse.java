package ru.kubsu.dekanat.auth.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(List<T> data, Meta meta) {

    public record Meta(int page, int perPage, int totalPages, long totalResults) {
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        Meta meta = new Meta(page.getNumber() + 1, page.getSize(), page.getTotalPages(), page.getTotalElements());
        return new PageResponse<>(page.getContent(), meta);
    }

}
