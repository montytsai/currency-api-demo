package io.github.montytsai.currencyapi.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Configuration
public class AppConfig {

    /**
     * 建立一個組態好超時的 WebClient Bean。（取代傳統 RestTemplate 的現代化作法。）
     *
     * @param connectTimeoutMs 連線超時毫秒數
     * @param readTimeoutMs    讀取超時毫秒數
     * @param webClientBuilder Spring 提供的建構器
     * @return 已設定超時的 WebClient 實例
     */
    @Bean
    public WebClient webClient(
            @Value("${coin-desk.api.timeout.connect}") int connectTimeoutMs,
            @Value("${coin-desk.api.timeout.read}") int readTimeoutMs,
            WebClient.Builder webClientBuilder) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(readTimeoutMs));

        return webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}