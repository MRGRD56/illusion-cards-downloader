package com.mrgrd56.illusioncardsdownloader.controller;

import com.mrgrd56.illusioncardsdownloader.service.CardsDownloadingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CardsDownloadingController {
    private final CardsDownloadingService cardsDownloadingService;

    public CardsDownloadingController(CardsDownloadingService cardsDownloadingService) {
        this.cardsDownloadingService = cardsDownloadingService;
    }

    @GetMapping("download-illusion-cards")
    public ResponseEntity<byte[]> downloadIllusionCards(
            @RequestParam String search,
            @RequestParam(required = false) Integer pageLimit) {
        var file = cardsDownloadingService.downloadIllusionCards(search, pageLimit);
        return ResponseEntity.ok()
                .header("Content-Type", "application/zip")
                .body(file);
    }
}
