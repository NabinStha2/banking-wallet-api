package com.nabin.bankingwallet.transfer.dto;

import com.nabin.bankingwallet.security.UserPrincipal;
import com.nabin.bankingwallet.transfer.dto.request.TransferRequest;
import com.nabin.bankingwallet.transfer.dto.response.TransferResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @AuthenticationPrincipal
            UserPrincipal principal,
            @Valid
            @RequestBody
            TransferRequest transferRequest,
            @org.springframework.web.bind.annotation.RequestHeader(name = "Idempotency-Key", required = false)
            String idempotencyKey
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transferService.transfer(principal.getId(), transferRequest, idempotencyKey));
    }
}