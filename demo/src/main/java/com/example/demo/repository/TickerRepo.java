package com.example.demo.repository;

import com.example.demo.entity.Ticker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TickerRepo extends JpaRepository<Ticker, Integer> {
}
