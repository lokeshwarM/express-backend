package com.express.expressbackend.domain.ledger;

public enum LedgerType {

    RECHARGE,            // User adds money
    SESSION_DEBIT,       // User charged for session
    LISTENER_CREDIT,     // Listener earns
    PLATFORM_COMMISSION, // Platform cut
    WITHDRAWAL           // Listener withdrawal
}