package com.saikiran.portfoliotracker.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
}
