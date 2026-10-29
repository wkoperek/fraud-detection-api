package com.koperek.api;

import com.koperek.risk.RiskAssessment;
import com.koperek.risk.RiskAssessmentService;
import com.koperek.risk.Transaction;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/transactions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource {
    private final RiskAssessmentService riskAssessmentService;

    public TransactionResource(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @POST
    public TransactionResponse assess(@Valid TransactionRequest request) {
        Transaction transaction = TransactionMapper.toTransaction(request);
        RiskAssessment riskAssessment = riskAssessmentService.assess(transaction);
        return TransactionMapper.toResponse(riskAssessment);
    }
}
