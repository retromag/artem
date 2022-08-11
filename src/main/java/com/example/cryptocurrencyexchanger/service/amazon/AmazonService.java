package com.example.cryptocurrencyexchanger.service.amazon;

import org.springframework.web.multipart.MultipartFile;

public interface AmazonService {
    void uploadImage(MultipartFile multipartFile, String symbol);

    void changeImage(MultipartFile multipartFile, String symbol);

    void uploadQRCode(MultipartFile multipartFile, String symbol);

    void changeQRCode(MultipartFile multipartFile, String symbol);
}
