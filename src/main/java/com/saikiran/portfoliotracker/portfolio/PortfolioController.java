package com.saikiran.portfoliotracker.portfolio;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@CrossOrigin(origins = "*")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<List<Portfolio>> listPortfolios() {
        return ResponseEntity.ok(portfolioService.listPortfolios());
    }

    @PostMapping
    public ResponseEntity<Portfolio> createPortfolio(@RequestBody CreatePortfolioRequest request) {
        return ResponseEntity.ok(portfolioService.createPortfolio(request.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Portfolio> updatePortfolio(
            @PathVariable Long id,
            @RequestBody CreatePortfolioRequest request
    ) {
        return ResponseEntity.ok(portfolioService.updatePortfolio(id, request.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{portfolioId}/holdings")
    public ResponseEntity<Holding> addHolding(
            @PathVariable Long portfolioId,
            @RequestBody Holding holding
    ) {
        return ResponseEntity.ok(portfolioService.addHolding(portfolioId, holding));
    }

    @DeleteMapping("/holdings/{holdingId}")
    public ResponseEntity<Void> deleteHolding(@PathVariable Long holdingId) {
        portfolioService.removeHolding(holdingId);
        return ResponseEntity.noContent().build();
    }

    public static class CreatePortfolioRequest {
        @NotBlank
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
