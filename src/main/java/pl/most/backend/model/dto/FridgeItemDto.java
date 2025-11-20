package pl.most.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import pl.most.backend.model.entity.FridgeItem;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FridgeItemDto {

    @NotBlank(message = "Nazwa produktu jest wymagana")
    private String name;

    @NotNull @Positive
    private BigDecimal quantity;

    @NotBlank
    private String unit;

    private FridgeItem.Category category;
    private LocalDate expiryDate;
    private Boolean isOpened;
    private String imageUrl;
    private String notes;
}