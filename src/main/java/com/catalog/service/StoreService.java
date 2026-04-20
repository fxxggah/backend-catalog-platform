package com.catalog.service;

import com.catalog.domain.entity.Store;
import com.catalog.domain.entity.StoreUser;
import com.catalog.domain.entity.User;
import com.catalog.domain.enums.Role;
import com.catalog.dto.store.StoreRequest;
import com.catalog.dto.store.StoreResponse;
import com.catalog.repository.StoreRepository;
import com.catalog.repository.StoreUserRepository;
import com.catalog.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreUserRepository storeUserRepository;
    private final AccessControlService access;

    public StoreResponse create(StoreRequest req, Long userId) {
        String generatedSlug = generateUniqueStoreSlug(req.getName());

        Store store = new Store();
        store.setName(req.getName());
        store.setSlug(generatedSlug);
        store.setLogo(req.getLogo());
        store.setPrimaryColor(req.getPrimaryColor());
        store.setSecondaryColor(req.getSecondaryColor());
        store.setTertiaryColor(req.getTertiaryColor());
        store.setWhatsappNumber(req.getWhatsappNumber());
        store.setInstagram(req.getInstagram());
        store.setFacebook(req.getFacebook());
        store.setTemplate(req.getTemplate());
        store.setStreet(req.getStreet());
        store.setNumber(req.getNumber());
        store.setCity(req.getCity());
        store.setState(req.getState());
        store.setCountry(req.getCountry());
        store.setGoogleMapsLink(req.getGoogleMapsLink());
        store.setActive(true);
        store.setCreatedAt(LocalDateTime.now());
        store.setCreatedBy(userId);

        store = storeRepository.save(store);

        StoreUser su = new StoreUser();
        su.setStore(store);
        User user = new User();
        user.setId(userId);
        su.setUser(user);
        su.setRole(Role.OWNER);
        su.setCreatedAt(LocalDateTime.now());

        storeUserRepository.save(su);

        return map(store);
    }

    public StoreResponse getBySlug(String storeSlug) {
        Store s = storeRepository.findBySlug(storeSlug)
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));

        return map(s);
    }

    public List<StoreResponse> getUserStores(Long userId) {
        return storeUserRepository.findByUserId(userId)
                .stream()
                .map(su -> map(su.getStore()))
                .toList();
    }

    public StoreResponse update(String storeSlug, StoreRequest req, Long userId) {

        Store store = getStoreBySlug(storeSlug);

        access.checkOwnerAccess(userId, store.getId());

        store.setName(req.getName());
        store.setLogo(req.getLogo());
        store.setPrimaryColor(req.getPrimaryColor());
        store.setSecondaryColor(req.getSecondaryColor());
        store.setTertiaryColor(req.getTertiaryColor());
        store.setWhatsappNumber(req.getWhatsappNumber());
        store.setInstagram(req.getInstagram());
        store.setFacebook(req.getFacebook());
        store.setTemplate(req.getTemplate());
        store.setStreet(req.getStreet());
        store.setNumber(req.getNumber());
        store.setCity(req.getCity());
        store.setState(req.getState());
        store.setCountry(req.getCountry());
        store.setGoogleMapsLink(req.getGoogleMapsLink());
        store.setUpdatedAt(LocalDateTime.now());
        store.setUpdatedBy(userId);

        return map(storeRepository.save(store));
    }

    public void deactivate(String storeSlug, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkOwnerAccess(userId, store.getId());

        store.setActive(false);
        store.setUpdatedAt(LocalDateTime.now());
        store.setUpdatedBy(userId);

        storeRepository.save(store);
    }

    public void activate(String storeSlug, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkOwnerAccess(userId, store.getId());

        store.setActive(true);
        store.setUpdatedAt(LocalDateTime.now());
        store.setUpdatedBy(userId);

        storeRepository.save(store);
    }

    private Store getStoreBySlug(String storeSlug) {
        return storeRepository.findBySlug(storeSlug)
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));
    }

    private String generateUniqueStoreSlug(String name) {
        String baseSlug = SlugUtils.toSlug(name);
        String slug = baseSlug;
        int counter = 2;

        while (storeRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    private StoreResponse map(Store s) {
        return StoreResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .slug(s.getSlug())
                .logo(s.getLogo())
                .primaryColor(s.getPrimaryColor())
                .secondaryColor(s.getSecondaryColor())
                .tertiaryColor(s.getTertiaryColor())
                .whatsappNumber(s.getWhatsappNumber())
                .instagram(s.getInstagram())
                .facebook(s.getFacebook())
                .template(s.getTemplate())
                .active(s.getActive())
                .street(s.getStreet())
                .number(s.getNumber())
                .city(s.getCity())
                .state(s.getState())
                .country(s.getCountry())
                .googleMapsLink(s.getGoogleMapsLink())
                .createdAt(s.getCreatedAt())
                .build();
    }
}