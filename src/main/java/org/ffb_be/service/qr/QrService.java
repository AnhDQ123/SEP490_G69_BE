package org.ffb_be.service.qr;

public interface QrService {
    String generateQrCode(Long orderId, Long shopId);
}
