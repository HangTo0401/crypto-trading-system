package com.example.demo.controller;

import com.example.demo.dto.BookTickerDTO;
import com.example.demo.dto.MarketTickerDTO;
import com.example.demo.dto.TickerDTO;
import com.example.demo.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MainController {

    @Autowired
    private PricingService pricingService;

    @GetMapping("/ticker/bookTicker")
    public ResponseEntity<?> getBookTickers() {
        List<BookTickerDTO> bookTickers = pricingService.getBookTickers();
        return new ResponseEntity<>(bookTickers, HttpStatus.OK);
    }

    @GetMapping("/market/tickers")
    public ResponseEntity<?> getMarketTickers() {
        List<MarketTickerDTO> marketTickers = pricingService.getMarketTickers();
        return new ResponseEntity<>(marketTickers, HttpStatus.OK);
    }

    @GetMapping("/store")
    public ResponseEntity<?> storePricing() {
        pricingService.storePricing();
        return new ResponseEntity<>("Saved best pricing successfully", HttpStatus.OK);
    }

    @GetMapping("/pricing/latest")
    public ResponseEntity<?> getLatestBestPricing() {
        List<TickerDTO> tickerDTO = pricingService.getLatestBestPricing();
        return new ResponseEntity<>(tickerDTO, HttpStatus.OK);
    }
}
