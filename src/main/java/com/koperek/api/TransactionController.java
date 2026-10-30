package com.koperek.api;

import com.koperek.risk.RiskAssessment;
import com.koperek.risk.RiskAssessmentService;
import com.koperek.risk.Transaction;
import io.quarkus.security.Authenticated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/transactions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class TransactionController {
    private final RiskAssessmentService riskAssessmentService;

    public TransactionController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @POST
    public TransactionResponse assess(@Valid @NotNull TransactionRequest request) {
        Transaction transaction = TransactionMapper.toTransaction(request);
        RiskAssessment riskAssessment = riskAssessmentService.assess(transaction);
        return TransactionMapper.toResponse(riskAssessment);
    }
}
