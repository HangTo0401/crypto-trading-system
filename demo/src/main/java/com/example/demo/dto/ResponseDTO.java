package com.example.demo.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ResponseDTO {
    @Getter
    private List<MarketTickerDTO> data;
}
