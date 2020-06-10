package com.practice.junit5study.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.junit5study.domain.Member;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Slf4j
public class BasicControllerTest {
    public MockWebServer mockWebServer;

    @BeforeEach
    void setUp() {
        this.mockWebServer = new MockWebServer();
        log.info("MockWebServer를 시작합니다.");
    }

    @AfterEach
    void tearDown() throws IOException {
        this.mockWebServer.shutdown();
        log.info("MockWebServer를 끝냅니다.");
    }

    @DisplayName("/get 테스트")
    @Test
    void getMemberByIdTest() throws Exception{
        Member member = new Member(1L, "user@gmail.com");
        ObjectMapper objectMapper = new ObjectMapper();
        String expectedBody = objectMapper.writeValueAsString(member);

        prepareResponse( mockResponse ->
                mockResponse
                        .addHeader("Content-Type", "application/json")
                        .setBody(expectedBody)
        );

        Mono<Member> response = getWebClient().get().uri("/get").accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Member.class);

        StepVerifier.create(response)
                .consumeNextWith( member1 -> {
                    assertEquals(member1.getId(), member.getId());
                    assertEquals(member1.getEmail(), member.getEmail());
                })
                .expectComplete().verify();
    }

    private void prepareResponse(Consumer<MockResponse> consumer){
        MockResponse response = new MockResponse();
        consumer.accept(response);
        this.mockWebServer.enqueue(response);
    }

    private WebClient getWebClient(){
        int port = this.mockWebServer.getPort();
        return WebClient.builder().baseUrl("http://localhost:" + port).build();
    }
}
