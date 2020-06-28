# 구글 로그인 연동하기

## Spring Security Oauth
* 기존 Spring Security Oauth 프로젝트는 deprecated 되었다.
* Spring Security OAuth 기능들은 Spring Security 5.x에 포함 되었다.
* Spring Security OAuth, Spring Cloud Security, Spring Boot OAuth 등 OAuth 관련 다양한 스프링 프로젝트들이 산재해 있어 개발자들이 어떤 라이브러리를 사용할지 선택하기 혼란스러웠다.
* 확장 포인트가 적절하게 오픈되어 있지 않아 개발자가 OAuth 관련 개발을 할 때 불편했다.
* [참고1. Next Generation OAuth 2.0 Support with Spring Security](https://spring.io/blog/2018/01/30/next-generation-oauth-2-0-support-with-spring-security)
* [참고2. Spring Security OAuth 2.0 Roadmap Update](https://spring.io/blog/2019/11/14/spring-security-oauth-2-0-roadmap-update)
* [참고3. Announcing the Spring Authorization Server](https://spring.io/blog/2020/04/15/announcing-the-spring-authorization-server)
* [참고4. Spring OAuth 2.0 Features Matrix](https://github.com/spring-projects/spring-security/wiki/OAuth-2.0-Features-Matrix)

### 의존성 추가하기
```groovy
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-oauth2-client')
}
```
* spring-boot-starter-oauth2-client 의존성은 spring-security-oauth2-client 스타터 라이브러리이다.
* OAuth2 Client 관련 기능들을 지원한다.
* 구글로그인, 페이스북로그인 등 이미 존재하는 OAuth2 로그인을 연동하는 클라이언트로써 개발을 할 때 사용한다.

### SecurityConfig
```java
package me.hoonmaro.study.springboot.config.auth;

import lombok.RequiredArgsConstructor;
import me.hoonmaro.study.springboot.domain.user.Role;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@RequiredArgsConstructor
@EnableWebSecurity // 1
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers("/css/**", "/images/**", "/js/**"); // 2
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                csrf().disable()
                .headers().frameOptions().disable() // 3
            .and()
                .authorizeRequests()
                .antMatchers("/he-console/**").permitAll()
                .antMatchers("/api/v1/**").hasRole(Role.USER.name())
                .anyRequest().authenticated()
            .and()
                .logout()
                    .logoutSuccessUrl("/")
            .and()
                .oauth2Login() // 4
                    .userInfoEndpoint() // 5
                        .userService(customOAuth2UserService); // 6
    }
}
```
1. @EnableWebSecurity: 스프링 시큐리티를 활성화 합니다.
2. static 리소스들은 WebSecurity ignoring 설정을 통해 시큐리티를 바로 통과시키도록 한다.
  * 책에서는 HttpSecurity의 permitAll()로 설정했었는데, 문서를 찾아보니 WebSecurity가 더 적절한 것 같다.
  * 두 가지 방식의 결과는 같으나 과정에 있어 HttpSecurity는 더 많은 SpringSecurityFilterChain을 거쳐야 하므로 더 느리다.
  * [참고1. Spring Security Java Config Preview: Web Security
](https://spring.io/blog/2013/07/03/spring-security-java-config-preview-web-security/)
  * [참고2. SO: Difference between Web ignoring and Http permitting in Spring Security?](https://stackoverflow.com/questions/55652267/difference-between-web-ignoring-and-http-permitting-in-spring-security)
3. csrf().disable().headers().frameOptions().disable()
  * h2-console에 접근할 수 있게 csrf와 frameOptions를 비활성화.
4. oauth2Login(): OAuth2 로그인 기능에 대한 설정 진입점
5. userInfoEndpoint(): OAuth2 로그인 성공 이후 사용자 정보를 가져올 때의 설정들을 담당
6. userService()
  * 소셜 로그인 성공 시 후속 조치를 진행할 UserService 인터페이스의 구현체를 등록한다.
  * 리소스 서버에서 사용자 정보를 가져온 상태에서 추가로 진행하고자 하는 기능을 명시할 수 있다.
  
### SessionUser
* 세션에 저장할 때 직렬화를 해야한다. 그래야 세션에 저장된 객체를 불러와 역직렬화 할 수 있다.
* 하지만 User 객체는 Entity 객체이므로 직렬화를 추가하는 것은 옳지 않다.
* 기능이 추가되면서 User 객체에 대한 변경이 생길 경우 객체의 버전이 달라 오류가 발생한다.

## 세션 저장소
* 기본적으로 스프링 시큐리티 세션은 내장 WAS(기본적으로 Tomcat)의 메모리에 저장된다.
* 애플리케이션을 재실행하면 세션 정보가 초기화 된다.
* 또, WAS가 이중화 될 경우 WAS들 간 세션 공유를 위한 추가 설정이 필요하다.

### 데이터베이스 세션 저장
* 쉽게 여러 WAS들간 세션 공유 및 세션 정보를 저장하는 방법이다.
* 다만 로그인 처리가 많은 B2C 서비스에서는 잦은 DB IO로 성능 이슈가 발생할 수 있어 주로 백오피스에서 사용한다.

### 캐시 세션 저장
* Redis, Memcached와 같은 외장 캐시 저장소를 사용하여 세션을 저장하는 것이 일반적이다.