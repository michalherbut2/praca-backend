package pl.most.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.most.backend.model.dto.FridgeItemDto;
import pl.most.backend.model.entity.FridgeItem;
import pl.most.backend.model.entity.User;
import pl.most.backend.repository.FridgeItemRepository;
import pl.most.backend.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FridgeService {

    private final FridgeItemRepository fridgeItemRepository;
    private final UserRepository userRepository;

    public List<FridgeItem> getAllItems() {
        return fridgeItemRepository.findAll();
    }

    public FridgeItem getItemById(String id) {
        return fridgeItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produkt nie znaleziony"));
    }

    @Transactional
    public FridgeItem addItem(FridgeItemDto dto, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie znaleziony"));

        FridgeItem item = new FridgeItem();
        item.setName(dto.getName());
        item.setQuantity(dto.getQuantity());
        item.setUnit(dto.getUnit());
        item.setCategory(dto.getCategory() != null ? dto.getCategory() : FridgeItem.Category.INNE);
        item.setAddedBy(userId);
        item.setAddedByName(user.getFirstName() + " " + user.getLastName());
        item.setExpiryDate(dto.getExpiryDate());
        item.setIsOpened(dto.getIsOpened() != null ? dto.getIsOpened() : false);
        item.setImageUrl(dto.getImageUrl());
        item.setNotes(dto.getNotes());

        return fridgeItemRepository.save(item);
    }

    @Transactional
    public void deleteItem(String id, UUID userId) {
        FridgeItem item = getItemById(id);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie znaleziony"));

        // User can delete own items, or admin/przeslowy can delete any
        if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.PRZESLOWY) {
            throw new SecurityException("Brak uprawnień do usunięcia produktu");
        }

        fridgeItemRepository.delete(item);
    }
}