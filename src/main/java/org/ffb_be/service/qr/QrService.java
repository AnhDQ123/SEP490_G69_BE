package org.ffb_be.service.qr;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface QrService {
    String generateQrCode(Long orderId, Long shopId);

    void updatePaymentProof(Long orderId, MultipartFile paymentProof) throws IOException;
}
