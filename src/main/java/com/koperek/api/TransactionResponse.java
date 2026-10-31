package com.koperek.api;

import java.util.List;

public record TransactionResponse(int riskScore, String riskLevel, String assessment,
                                  List<TriggeredRule> triggeredRules, String requestId, boolean degraded) {
}
