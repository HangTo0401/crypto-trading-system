package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class RestTemplateService {

    @Value("${installCertificate}")
    private Boolean installCertificate;

    @Value("${local.ssl.keystore.path}")
    private String localKeystorePath;

    @Value("${local.ssl.keystore.password}")
    private String localKeystorePassword;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        this.restTemplate = createRestTemplate();
    }

    private RestTemplate createRestTemplate() {
        RestTemplate restTemplate = null;
        if (BooleanUtils.isTrue(installCertificate)) {
            try {
                SSLContext sslContext = getSSLContextWithKeystore();
                SSLConnectionSocketFactory sslConFactory = new SSLConnectionSocketFactory(sslContext);

                CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslConFactory).build();
                ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
                restTemplate = new RestTemplate(requestFactory);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create RestTemplate. reason: " + e.getMessage(), e);
            }
        } else {
            restTemplate = new RestTemplate();
        }
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    private static SSLContext getSSLContextWithKeystore() {
        SSLContext sslContext = null;
        try (FileInputStream fileInputStream = new FileInputStream(
                ResourceUtils.getFile("classpath:demo_keystore.jks"))) {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore trustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustedStore.load(fileInputStream, "123456".toCharArray());
            tmf.init(trustedStore);
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, tmf.getTrustManagers(), null);
        } catch (CertificateException | IOException | KeyStoreException | KeyManagementException | NoSuchAlgorithmException e) {
            log.error("Failed to initialize confidential client application due to " + e.getMessage());
        }

        return sslContext;
    }

    public <T> List<T> getForList(String url, Class<T> cls) {
        List<T> result = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, cls);
            ResponseEntity response = restTemplate.exchange(url, HttpMethod.GET, null,
                    ParameterizedTypeReference.forType(type));

            if (response.getStatusCode().equals(HttpStatus.OK)) {
                result = (List<T>) response.getBody();
            }
        } catch (Exception ex) {
            log.error("Failed to call get api: " + ex);
        }

        return result;
    }

    public <T> Object getForObject(String url, Class<T> cls) {
        Object result = null;
        try {
            ResponseEntity response = restTemplate.exchange(url, HttpMethod.GET, null,
                    ParameterizedTypeReference.forType(cls));

            if (response.getStatusCode().equals(HttpStatus.OK)) {
                result = response.getBody();
            }
        } catch (Exception ex) {
            log.error("Failed to call get api: " + ex);
        }
        return result;
    }
}
