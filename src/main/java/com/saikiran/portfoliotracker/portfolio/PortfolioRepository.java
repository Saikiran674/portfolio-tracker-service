package com.saikiran.portfoliotracker.portfolio;

import com.saikiran.portfoliotracker.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByOwner(UserEntity owner);
}
