package com.example.hotel_management.common; // Projenizin ortak paket yapısına göre ayarlayabilirsiniz

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import jakarta.validation.constraints.Min;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;

    @Min(value = 0, message = "Sayfa numarası 0 veya daha büyük olmalıdır.")
    private int pageNo;

    @Min(value = 1, message = "Sayfa boyutu en az 1 olmalıdır.")
    private int pageSize;

    private long totalElements;

    private int totalPages;

    private boolean last;
}