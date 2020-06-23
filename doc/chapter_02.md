# Chapter 02: 스프링 부트에서 테스트 코드를 작성하자

## 테스트 코드 소개
테스트 코드는 테스트를 목적으로 작성된 코드를 말한다. 보통 테스트 코드를 작성할 때는 단위 테스트 코드를 작성한다.
여기서 단위는 다양한 의미로 해석되어지나 일반적으로는 하나의 책임을 수행하는 메서드를 의미한다.
테스트 코드를 먼저 작성하면서 개발을 하는 TDD(Test Driven Development, 테스트 주도 개발) 개발방법론이 있다.
* Red: 실패하는 테스트를 먼저 작성한다.
* Green: 테스트가 통과하는 프로덕션 코드를 작성한다.
* Refactor: 테스트가 통과하면 프로덕션 코드를 리팩토링 한다.

단위 테스트를 작성하여 얻는 이점은 다음과 같다.
* 개발단계 초기에 문제를 발견할 수 있다.
* 코드를 리팩토링하거나 라이브러리 업그레이드를 하는 등 코드를 변경할 때 기존 기능이 올바르게 작동하는지 확인 할 수 있다(회귀 테스트)
* 기능에 대한 불확실성을 줄인다.
* 시스템에 대한 문서를 제공한다.

### 유지보수하기 쉬운 테스트 케이스 작성
* 하나의 테스트에 하나의 기능만 검증하라
  * SRP(Single Responsibility Principle, 단일책임원칙)를 준수한다.
  * 테스트케이스에서 반복되는 부분도 리팩토링 한다.
  * 구현체에 의존하지 않도록 작성한다.다

## Hello Controller 테스트 코드 작성하기
```java
@ExtendWith(SpringExtension.class) // 1
@WebMvcTest(controllers = HelloController.class) // 2
public class HelloControllerTest {

    @Autowired
    private MockMvc mvc; // 3

    @Test // 4
    public void hello_return() throws Exception {
        String hello = "hello";

        mvc.perform(get("/hello")) // 5
                .andExpect(status().isOk()) // 6
                .andExpect(content().string(hello)); // 7

    }

}

```
1. @ExtendWith(SpringExtension.class)
    * Junit 5의 확장기능(Extenstion)을 등록하는 어노테이션
    * Spring Framework의 기능을 테스트에서 사용할 수 있도록 Junit과 연결한다.
2. @WebMvcTest(controllers = HelloController.class)
    * Spring MVC 테스트에 집중할 수 있도록 지원해주는 어노테이션
    * Spring의 전체적인 자동 설정 대신 MVC를 위한 @Controller, @ControllerAdvice, @JsonComponent 등을 사용할 수 있다.
    * StereoType 어노테이션 (@Service, @Component, @Repository)은 사용할 수 없다.
    * HelloController 클래스를 등록하여 이 테스트 클래스에서는 HelloController만 사용하도록 설정
3. private MockMvc mvc
    * 스프링 MVC 테스트를 지원하는 목 객체
    * [API 문서](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/web/servlet/MockMvc.html)
4. @Test
    * 이 메서드가 Test 메서드임을 알려주는 어노테이션
    * JUnit 5에서는 접근제어자가 public이 아니어도 동작한다.
5. mvc.perform(get("/hello"))
    * MockMvc를 통해 /hello 엔드포인트로 HTTP GET 요청을 한다.
    * 메서드 체이닝을 지원하여 간편하게 작성할 수 있다.
6. .andExpect(status().isOk())
    * perform 메서드의 결과를 검증한다.
    * HTTP Header의 Status가 200인지 검증한다.
7. .andExpect(content().string(hello))
    * 응답 본문의 내용을 검증한다.
    * HelloController에서 응답하는 "hello" 문자열인지 검증한다.


## 롬복을 사용한 ResponseDto 작성 및 테스트

### HelloResponseDto
```java
package me.hoonmaro.study.springboot.web.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter // 1
@RequiredArgsConstructor // 2
public class HelloResponseDto {

    private final String name;
    private final int amount;

}
```
1. 롬복 Getter 메서드로 필드의 getter 메서드를 자동으로 생성한다.
2. final 필드들의 생성자를 자동으로 생성한다.

### 테스트 코드 
```
@Test
void helloDto_return() throws Exception {
    String name = "hello";
    int amount = 1000;

    mvc.perform(
                get("/hello/dto")
            .param("name", name) // 1
            .param("amount", String.valueOf(amount)) // 2
    )
            .andDo(result -> System.out.println("result > " + result.getResponse().getContentAsString())) \\ 3
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", Matchers.is(name))) // 4
            .andExpect(jsonPath("$.amount", Matchers.is(amount)));
}
```
1. HTTP 쿼리 스트링 파라미터를 작성한다. 
2. param의 두번째 인자인 파라미터 값은 문자열만 가능하다.
3. andDo를 이용항 핸들러를 추가하여 추가적인 작업을 할 수 있으며, 여기서는 응답 결과를 System.out.println 으로 표시한다.
4. jsonPath와 Matchers.is를 활용하여 json 응답 객체의 name 필드의 값을 검증한다.
  * $는 Root Node를 의미한다. 오브젝트와 배열 타입 상관없다.