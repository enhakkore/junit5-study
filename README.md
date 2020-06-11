# JUnit5 Study  
* Contents  
    * [Concept](#concept)
    * [Mockito 사용하기](#mockito-사용하기)  
    * [MockWebServer 사용하기](#mockWebserver-사용하기)  
* [참고](#참고)  

---

### Concept  
* jUnit5는 junit platform, vintage, jupiter 으로 구성되어있다.  
* 스프링 부트 프로젝트에는 starter 의존성에 junit이 포함되어 있으며, 스프링 부트 2.2+ 부터는 junit5가 포함된다.
* 스프링 부트 프로젝트가 아니라면 아래 의존성을 추가하면 된다.  
    * Maven  
        ```
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.6.2</version>
            <scope>test</scope>
        </dependency>

        ```  
    * Gradle  
        ```
        testCompile group: 'org.junit.jupiter', name: 'junit-jupiter-api', version: '5.6.2'

        ```

* 4부터 사용하고 있는 자바의 리플렉션 기능 덕분에 5부터는 테스트 클래스와 메서드 모두 public으로 사용하지 않아도 된다.  

* JUnit의 default lifecycle은 `PER_METHOD`이다.  
    `PER_CLASS`로 사용하려면 테스트 클래스에 `@TestInstance(Lifecycle.PER_CLASS)`를 붙여주면 된다.  
    `PER_CLASS`로 사용하면 `@BeforeAll`과 `@AfterAll` 메서드를 static으로 사용하지 않아도 된다.  
    * JVM 수준에서 `PER_CLASS`로 변경  
        ```
        -Djunit.jupiter.testinstance.lifecycle.default=per_class
        ```  
    * JUnit platform configuration file을 이용해 `PER_CLASS`로 변경  
        * 프로젝트 루트 디렉토리 안(e.g., src/test/resource)에 `junit-platform.properties` 파일 생성  
        * `junit.jupiter.testinstance.lifecycle.default = per_class` 내용 추가  
        * configuration file을 통해 변경하면 프로젝트를 관리하는 version control system이 file을 확인하기 때문에 IDE와 build software에서 확실하게 사용할 수 있다.  


### Mockito 사용하기  
* `import org.mockito.Mockito;`
    ```java
    public interface MemberService{
        Optional<Member> findById(Long memberId);
        void validate(Long memberId);
        void notify(Study newStudy);
        void notify(Member member);
    }
    ```
* 구현체가 없는 interface를 테스트에 사용하기  
    * 테스트 클래스에 `@ExtendWith(MockitoExtentsion.class)`를 추가  
    * 방법 1  
        * 테스트 메서드 안에서  
            ```java
            MemberService memberService = Mockito.mock(MemberService.class);
            ```
    * 방법 2  
        * 클래스 멤버변수로 정의하기  
            ```java
            @Mock MemberService memberService;
            ```
    * 방법 3  
        * 테스트 메서드의 매개변수로 넘기기  
            ```java
            @Test
            void MemberServiceTest(@Mock MemberService service) { ... }
            ```  


* 테스트 상황 가정하기  
    ```java
    Member member = new Member(1L, "user@gmail.com");

    Mockito.when(memberService.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(member))    // 첫 번째 호출됐을 때
                .thenThrow(new RuntimeException())  // 두 번째 호출됐을 때
                .thenReturn(Optional.empty());      // 세 번째 호출됐을 때

    // 첫 번째 호출하고 검사
    Optional<Member> expectedMember = memberService.findById(1L);
    assertTrue(expectedMember.isPresent());
    assertEquals("user@gmail.com", expectedMember.get().getEmail());

    // 두 번째 호출하고 검사
    assertThrows(RuntimeException.class, () -> memberService.findById(2L));

    // 세 번째 호출하고 검사
    assertEquals(Optional.empty(), memberService.findById(3L));
    ```  
    * `memberService.findById(ArgumentMatchers.anyLong())`를 첫 번째 호출했을 때는 `Optional.of(member)`를 반환하도록 가정  
    * 두 번째 호출했을 때는 `RuntimeException`이 발생하도록 가정  
    * 세 번째 호출했을 때는 `Optional.empty()`를 반환하도록 가정  


* 메서드 호출 횟수 검사하기  
    ```java
    @Entity
    public class Study{
        ...

        public Study createNewStudy(Long memberId, Study study){
            ...

            memberService.notify(newStudy);
            memberService.notify(member.get());

            ...
        }
    }
    ```  
    ```java
    studyService.createNewStudy(member.getId(), study);

    Mockito.verify(memberService, Mockito.times(1)).notify(study);
    Mockito.verify(memberService, Mockito.times(1)).notify(member);
    Mockito.verify(memberService, Mockito.never()).validate(member.getId());
    ```  
    * memberService의 notify(Study study) 메서드와 notify(Member member) 메서드가 한 번만 호출됐는지 검사  
    * memberService의 validate(Long memberId) 메서드가 호출되지 않았는지 검사  


* 메서드 호출 순서 검사하기  
    ```java
    studyService.createNewStudy(member.getId(), study);

    InOrder inOrder = Mockito.inOrder(memberService);
    inOrder.verify(memberService).notify(study);
    inOrder.verify(memberService).notify(member);
    ```  
    * memberService의 notify(Study study) 메서드와 notify(Member member) 메서드가 순서대로 호출됐는지 검사  


* BDD 스타일  
    * Given, When, Then으로 행동에 따라 테스트하는 방법  
    ```java
    @Test
    void openStudyTest(@Mock MemberService memberService, @Mock StudyRepository studyRepository){

        // Given
        StudyService studyService = new StudyService(memberService, studyRepository);
        Study study = new Study(10L, "더 자바, 테스트");
        BDDMockito.given(studyRepository.save(study)).willReturn(study);

        // When
        studyService.openStudy(study);

        // Then
        assertEquals(StudyStatus.OPENED, study.getStatus());
        assertNotNull(study.getOpenedDateTime());
        BDDMockito.then(memberService).should().notify(study);
    }
    ```  
    * `Mockito.when().thenReturn()` -> `BDDMockito.given().willReturn()`  

### MockWebServer 사용하기  
* MockWebServer을 사용하기 위해 추가해야 하는 의존성 라이브러리  
    * Gradle  
        ```
        testImplementation 'com.squareup.okhttp3:okhttp:4.7.2'
        testImplementation 'com.squareup.okhttp3:mockwebserver:4.7.2'
        ```
* Spring Webflux를 테스트하기 위해 추가해야 하는 의존성 라이브러리  
    * Gradle  
        ```
        testImplementation 'io.projectreactor:reactor-test'
        ```  

* MockWebServer를 생성하고 응답값 셋팅  
    ```java  
    import okhttp3.mockwebserver.MockResponse;
    import okhttp3.mockwebserver.MockWebServer;
    ```
    ```java
    Member member = new Member(1L, "user@gmail.com");
    ObjectMapper objectMapper = new ObjectMapper();
    String expectedBody = objectMapper.writeValueAsString(member);

    // 응답값 만들기
    MockResponse response = new MockResponse();
    response.addHeader("Content-Type", "application/json")
            .setBody(expectedBody);

    // MockWebServer에 응답값 셋팅
    MockWebServer mockWebServer = new MockWebServer();
    mockWebServer.enqueue(response);
    ```  

* WebClient로 요청보내고 받은 응답값 검사  
    ```java
    import org.springframework.web.reactive.function.client.WebClient;
    import reactor.core.publisher.Mono;
    import reactor.test.StepVerifier;
    ```  
    ```java
    Mono<Member> response = client.get().uri("/get").accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(Member.class);

    StepVerifier.create(response)
            .consumeNextWith( member1 -> {
                assertEquals(member1.getId(), member.getId());
                assertEquals(member1.getEmail(), member.getEmail());
            })
            .expectComplete().verify();
    ```  
    * `StepVerifier`를 이용해서 Mono로 감싼 응닶값을 확인한다.  
    * `consumeNextWith()`를 이용해 응답 body에 기대되는 값이 있는지 확인한다.  
    * `expectComplete()`은 Publisher가 Subscriber에게 모든 데이터를 보내면 호출하는 onComplete() 메서드가 호출됐는지 확인한다.  
    * `verify()`은 설정된 timeout 시간 동안 Subscriber가 받는 signal을 확인한다. 따로 설정하지 않으면 default timeout 값이 사용된다.  


#### 참고  
* [인프런 강의 - 더 자바, 애플리케이션을 테스트하는 다양한 방법](https://www.inflearn.com/course/the-java-application-test/)  
* [https://www.baeldung.com/spring-mocking-webclient](https://www.baeldung.com/spring-mocking-webclient)  
* [https://spring.io/guides/gs/testing-web/](https://spring.io/guides/gs/testing-web/)  
* [https://junit.org/junit5/docs/current/user-guide/](https://junit.org/junit5/docs/current/user-guide/)  
* [https://github.com/square/okhttp/tree/master/mockwebserver](https://github.com/square/okhttp/tree/master/mockwebserver)
