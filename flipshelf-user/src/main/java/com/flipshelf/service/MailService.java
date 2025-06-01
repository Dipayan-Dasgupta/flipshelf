package com.flipshelf.service;

import com.flipshelf.model.Purchase;

public interface MailService {
    void sendPurchaseConfirmation(String to,Purchase purchase);
}
