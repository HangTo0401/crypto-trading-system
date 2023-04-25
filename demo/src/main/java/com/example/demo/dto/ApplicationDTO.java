package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicationDTO {
    private Boolean installCertificate;
    private String localKeystorePath;
    private String localKeystorePassword;
}
