package com.saikiran.portfoliotracker.portfolio;

import com.saikiran.portfoliotracker.user.UserEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final Random random = new Random();

    public PortfolioService(PortfolioRepository portfolioRepository,
                            HoldingRepository holdingRepository) {
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
    }

    private UserEntity getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserEntity user) {
            return user;
        }
        throw new RuntimeException("No authenticated user");
    }

    public List<Portfolio> listPortfolios() {
        UserEntity user = getCurrentUser();
        return portfolioRepository.findByOwner(user);
    }

    public Portfolio createPortfolio(String name) {
        UserEntity user = getCurrentUser();
        Portfolio portfolio = new Portfolio();
        portfolio.setName(name);
        portfolio.setOwner(user);
        return portfolioRepository.save(portfolio);
    }

    public Portfolio updatePortfolio(Long id, String name) {
        UserEntity user = getCurrentUser();
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        if (!portfolio.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Forbidden");
        }

        portfolio.setName(name);
        return portfolioRepository.save(portfolio);
    }

    public void deletePortfolio(Long id) {
        UserEntity user = getCurrentUser();
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        if (!portfolio.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Forbidden");
        }

        portfolioRepository.delete(portfolio);
    }

    public Holding addHolding(Long portfolioId, Holding holding) {
        UserEntity user = getCurrentUser();
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        if (!portfolio.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Forbidden");
        }

        holding.setPortfolio(portfolio);
        Holding saved = holdingRepository.save(holding);

        recalcPortfolioValue(portfolio);
        return saved;
    }

    public void removeHolding(Long holdingId) {
        UserEntity user = getCurrentUser();
        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new RuntimeException("Holding not found"));

        Portfolio portfolio = holding.getPortfolio();
        if (!portfolio.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Forbidden");
        }

        holdingRepository.delete(holding);
        recalcPortfolioValue(portfolio);
    }

    private void recalcPortfolioValue(Portfolio portfolio) {
        BigDecimal total = portfolio.getHoldings().stream()
                .map(h -> {
                    BigDecimal lp = h.getLastPrice() != null ? h.getLastPrice() : h.getAveragePrice();
                    if (lp == null || h.getQuantity() == null) {
                        return BigDecimal.ZERO;
                    }
                    return lp.multiply(h.getQuantity());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        portfolio.setTotalValue(total.setScale(2, RoundingMode.HALF_UP));
        portfolioRepository.save(portfolio);
    }

    @Scheduled(fixedDelay = 60000)
    public void refreshMockPrices() {
        List<Holding> holdings = holdingRepository.findAll();
        for (Holding h : holdings) {
            BigDecimal base = h.getLastPrice() != null ? h.getLastPrice() : h.getAveragePrice();
            if (base == null) {
                continue;
            }
            double changePercent = (random.nextDouble() - 0.5) * 0.02;
            BigDecimal newPrice = base.multiply(BigDecimal.valueOf(1 + changePercent));
            h.setLastPrice(newPrice.setScale(2, RoundingMode.HALF_UP));
        }
        holdingRepository.saveAll(holdings);

        portfolioRepository.findAll()
                .forEach(this::recalcPortfolioValue);
    }
}
