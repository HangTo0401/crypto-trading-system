package com.example.demo.service;

import com.example.demo.dto.BookTickerDTO;
import com.example.demo.dto.MarketTickerDTO;
import com.example.demo.dto.ResponseDTO;
import com.example.demo.dto.TickerDTO;
import com.example.demo.entity.Ticker;
import com.example.demo.repository.TickerRepo;
import com.example.demo.utils.Constants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PricingService {

    @Value("${binance.api.url}")
    private String binanceUrl;

    @Value("${huobi.api.url}")
    private String huobiUrl;

    @Autowired
    private RestTemplateService restTemplateService;

    @Autowired
    private TickerRepo tickerRepo;

    @Autowired
    private ModelMapper modelMapper;

    private static SortedSet<Double> btcBuySet = Collections.synchronizedSortedSet(new TreeSet<>());
    private static SortedSet<Double> btcSellSet = Collections.synchronizedSortedSet(new TreeSet<>());
    private static SortedSet<Double> ethBuySet = Collections.synchronizedSortedSet(new TreeSet<>());
    private static SortedSet<Double> ethSellSet = Collections.synchronizedSortedSet(new TreeSet<>());

    /**
     * Create a 10 seconds interval scheduler to retrieve the pricing from the source
     * above and store the best pricing into the database.
     * Hints: Bid Price use for SELL order, Ask Price use for BUY order
     * */
    @Scheduled(fixedDelayString = "10000")
    public void storePricing() {
        compareAndSaveBestPricing();
    }

    private void compareAndSaveBestPricing() {
        saveBTCBestPricing();
        saveETHBestPricing();
    }

    private void saveBTCBestPricing() {
        double btcBinanceAsk = getBTCBookTicker().getAskPrice();
        double btcHuobiAsk = getBTCMarketTicker().getAsk();
        double btcBuyPrice = btcBinanceAsk < btcHuobiAsk ? btcBinanceAsk : btcHuobiAsk;
        btcBuySet.add(btcBuyPrice);
        String btcBuyPlatform = btcBinanceAsk < btcHuobiAsk ? Constants.BINANCE : Constants.HUOBI;

        double btcBinanceBid = getBTCBookTicker().getBidPrice();
        double btcHuobiBid = getBTCMarketTicker().getBid();
        double btcSellPrice = btcBinanceBid > btcHuobiBid ? btcBinanceBid : btcHuobiBid;
        String btcSellPlatform = btcBinanceBid > btcHuobiBid ? Constants.BINANCE : Constants.HUOBI;
        btcSellSet.add(btcSellPrice);

        // save best pricing to db
        Ticker ticker = new Ticker();
        ticker.setSymbol(Constants.BTC_USDT);
        ticker.setBuyPrice(getBuyPrice(Constants.BTC_USDT));
        ticker.setSellPrice(getSellPrice(Constants.BTC_USDT));
        ticker.setBuyPlatForm(btcBuyPlatform);
        ticker.setSellPlatForm(btcSellPlatform);
        tickerRepo.save(ticker);
    }

    private void saveETHBestPricing() {
        double ethBinanceAsk = getETHBookTicker().getAskPrice();
        double ethHuobiAsk = getETHMarketTicker().getAsk();
        double ethBuyPrice = ethBinanceAsk < ethHuobiAsk ? ethBinanceAsk : ethHuobiAsk;
        ethBuySet.add(ethBuyPrice);
        String ethBuyPlatform = ethBinanceAsk < ethHuobiAsk ? Constants.BINANCE : Constants.HUOBI;

        double ethBinanceBid = getETHBookTicker().getBidPrice();
        double ethHuobiBid = getETHMarketTicker().getBid();
        double ethSellPrice = ethBinanceBid > ethHuobiBid ? ethBinanceBid : ethHuobiBid;
        String ethSellPlatform = ethBinanceBid > ethHuobiBid ? Constants.BINANCE : Constants.HUOBI;
        ethSellSet.add(ethSellPrice);

        // save best pricing to db
        Ticker ticker = new Ticker();
        ticker.setSymbol(Constants.ETH_USDT);
        ticker.setBuyPrice(getBuyPrice(Constants.ETH_USDT));
        ticker.setSellPrice(getSellPrice(Constants.ETH_USDT));
        ticker.setBuyPlatForm(ethBuyPlatform);
        ticker.setSellPlatForm(ethSellPlatform);
        tickerRepo.save(ticker);
    }

    public List<TickerDTO> getLatestBestPricing() {
        List<TickerDTO> tickerDTOS = new ArrayList<>();

        Optional<TickerDTO> latestBtcTicker = tickerRepo.findAll().stream().filter(c -> StringUtils.isNotBlank(c.getSymbol()) && c.getSymbol().toLowerCase().equals(Constants.BTC_USDT.toLowerCase()))
                .sorted(Comparator.comparing(Ticker::getId).reversed())
                .map(item -> modelMapper.map(item, TickerDTO.class))
                .findFirst();

        Optional<TickerDTO> latestEthTicker = tickerRepo.findAll().stream().filter(c -> StringUtils.isNotBlank(c.getSymbol()) && c.getSymbol().toLowerCase().equals(Constants.ETH_USDT.toLowerCase()))
                .sorted(Comparator.comparing(Ticker::getId).reversed())
                .map(item -> modelMapper.map(item, TickerDTO.class))
                .findFirst();

        if (latestBtcTicker.isPresent()) {
            tickerDTOS.add(latestBtcTicker.get());
        }

        if (latestEthTicker.isPresent()) {
            tickerDTOS.add(latestEthTicker.get());
        }

        return tickerDTOS;
    }

    public double getBuyPrice(String symbol) {
        if (symbol.equals(Constants.BTC_USDT)) {
            if (btcBuySet != null && !btcBuySet.isEmpty())
                return Collections.max(btcBuySet);
        } else if (symbol.equals(Constants.ETH_USDT)) {
            if (ethBuySet != null && !ethBuySet.isEmpty())
                return Collections.max(ethBuySet);
        }

        return Double.NEGATIVE_INFINITY;
    }

    public static double getSellPrice(String symbol) {
        if (symbol.equals(Constants.BTC_USDT)) {
            if (btcSellSet != null && !btcSellSet.isEmpty())
                return Collections.min(btcSellSet);
        } else if (symbol.equals(Constants.ETH_USDT)) {
            if (ethSellSet != null && !ethSellSet.isEmpty())
                return Collections.min(ethSellSet);
        }

        return Double.POSITIVE_INFINITY;
    }

    public List<BookTickerDTO> getBookTickers() {
        List<BookTickerDTO> bookTickerDTOS = restTemplateService.getForList(binanceUrl, BookTickerDTO.class);
        return bookTickerDTOS;
    }

    public List<MarketTickerDTO> getMarketTickers() {
        ResponseDTO response = (ResponseDTO) restTemplateService.getForObject(huobiUrl, ResponseDTO.class);
        return response.getData();
    }

    public BookTickerDTO getETHBookTicker() {
        Optional<BookTickerDTO> matchingObject = getBookTickers().stream().
                filter(item -> StringUtils.isNotBlank(item.getSymbol()) && item.getSymbol().toLowerCase().equals(Constants.ETH_USDT.toLowerCase())).
                findFirst();

        return matchingObject.get();
    }

    public BookTickerDTO getBTCBookTicker() {
        Optional<BookTickerDTO> matchingObject = getBookTickers().stream().
                filter(item -> StringUtils.isNotBlank(item.getSymbol()) && item.getSymbol().toLowerCase().equals(Constants.BTC_USDT.toLowerCase())).
                findFirst();

        return matchingObject.get();
    }

    public MarketTickerDTO getETHMarketTicker() {
        Optional<MarketTickerDTO> matchingObject = getMarketTickers().stream().
                filter(item -> StringUtils.isNotBlank(item.getSymbol()) && item.getSymbol().toLowerCase().equals(Constants.ETH_USDT.toLowerCase())).
                findFirst();

        return matchingObject.get();
    }

    public MarketTickerDTO getBTCMarketTicker() {
        Optional<MarketTickerDTO> matchingObject = getMarketTickers().stream().
                filter(item -> StringUtils.isNotBlank(item.getSymbol()) && item.getSymbol().toLowerCase().equals(Constants.BTC_USDT.toLowerCase())).
                findFirst();

        return matchingObject.get();
    }
}
