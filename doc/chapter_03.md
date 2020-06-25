# Chapter 03: 스프링 부트에서 JPA로 데이터베이스 다뤄보자

## 데이터지향 프로그래밍과 객체지향 프로그래밍 간 패러다임 불일치
프로그램을 개발하다보면 데이터베이스를 이용해 데이터를 다루게 된다. 
일반적으로는 데이터를 저장(C), 조회(R), 수정(U), 삭제(D)하는 일을 많이 할 수 밖에 없다.
간단하지만 반복적인 SQL 문을 작성해야되고 MyBatis와 같은 쿼리 매퍼를 사용하면
프로그램 개발 로직보다 SQL 작성에 더 많은 시간을 쏟게 될 수 있다.

그럼 점차 개발할 때 드는 생각은 객체지향적인 사고보단 데이터 지향적인 사고를 하게 되어 객체지향 프로그래밍을 못 하게 된다.
이는 객체지향 원칙을 어기게되고, 결국 잘못된 설계에 빠지게 된다.
응집도가 낮아지고 결합도가 높아져 변화에 대응하기 어려워 진다.
이를 데이터 지향 프로그래밍과 객체지향 프로그래밍 간의 패러다임 불일치가 발생한다고 말한다.

## ORM과 JPA
ORM(Object Relational Mapping) 기술은 객체를 SQL에 자동으로 매핑해준다. 따라서 개발자들은 객체지향 프로그래밍에 집중할 수 있게 되고
더 나은 설계를 통해 유지보수가 용이한, 변경이 쉬운 확장성 있고 안정성 있는 설계로 개발하기 쉬워졌다.
자바 진영에서는 JPA라는 ORM 표준 인터페이스가 존재하며, 이 인터페이스에 대한 구현체로 Hibernate 등이 있다.
스프링은 Spring Data JPA 프로젝트를 통해 구현체들을 추상화 하였다. 
Spring Data JPA 인터페이스만 따르면 구현체와 저장소가 바뀌어도 쉽게 변경이 가능하다.

예를 들어 Hibernate JPA 구현체 이외의 Eclipse Link 같은 구현체로 변경할 경우 구현체 설정만 바꾸면 나머지 코드는 건드릴 일이 없다.
마찬가지로, MariaDB 저장소를 쓰다가 Oracle로 바꿔도 설정만 바꿔주면 된다.

## JPA 의존성 추가
```groovy
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-data-jpa')
    implementation('com.h2database:h2')
}
```
* org.springframework.boot:spring-boot-starter-data-jpa: Spring Data JPA 스타터 의존성
* com.h2database:h2: 인메모리 데이터베이스로 가볍고 빨라 로컬/테스트 용도로 자주 사용한다.
 
## Entity 클래스
```java
package me.hoonmaro.study.springboot.domain.posts;

import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor // 1
@Entity // 2
public class Posts {

    @Id // 3
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 4
    private Long id;

    @Column(length = 500, nullable = false) // 5
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String author;

    @Builder // 6
    public Posts(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

}
```
* 클래스 어노테이션은 필수 어노테이션을 클래스에 가깝게 작성한다.
  * @Getter, @NoArgsConstructor 같은 롬복 어노테이션은 코틀린으로 언어를 변경할 경우에 필요 없는데 클래스 쪽에 쓰여져있으면 어노테이션 삭제가 번거롭다

1. @NoArgsConstructor: 롬복 어노테이션으로 기본 생성자를 만들어준다.
2. @Entity: JPA Entity 클래스임을 선언한다. 데이터베이스에 대한 영속성이 생기며 하나의 테이블이 생성된다.
3. @Id: Entity 클래스의 식별자임을 선언한다. 테이블의 PK 필드임을 나타낸다.
4. @GeneratedValue: 식별자를 생성하는 방법을 선언한다. 스프링 부트 2부터는 IDENTITY를 사용하여 AUTO_INCREMENT 방식으로 생성한다.
  * 비즈니스상 자연키가 PK가 될 경우 여러 문제가 발생할 여지가 있어 PK는 SEQUENCE와 같은 인조키를 사용하는 것이 좋다.
  * 이러한 자연키들은 따로 컬럼으로 빼고 유니크키로 선언하여 중복을 방지하는 것이 좋다.
5. @Column: 테이블의 컬럼을 나타낸다. 기본적으로느 Entity 클래스의 필드는 모두 컬럼으로 만들어진다.
  * 컬럼의 기본값 이외의 변경이 필요한 옵션이 있을 경우 해당 어노테이션과 속성을 설정한다.
6. @Builder: 빌더 패턴을 위한 빌더 클래스/메서드를 자동으로 생성해준다.
  * 일반적으로는 빌더 클래스에 포함할 필드를 인자로 가진 생성자 위에 선언하며, 보통 모든 필드에 대한 생성자에 사용한다.

### Setter 메서드 미사용
* 자바빈 규약에 따라 getter/setter를 무조건 생성하는 경우가 있지만, 도메인 클래스에서는 setter를 사용하지 않는다다.
* 필드에 값을 세팅할 필요가 있는 경우 그 목적과 의도를 명확하게 드러내는 public 메서드로 대신한다.
* 데이터베이스에 저장하기 위해 값을 세팅할 때, 일반적으로는 모든 생성자(그리고 빌더 패턴)를 사용하여 최종 값을 도메인 클래스에 세팅한다.

## Spring 웹 계층

### Web Layer
* 컨트롤러, Freemarker,JSP,Thymeleaf 등의 뷰 템플릿 영역
* 필터, 인터셉터, 컨트롤러 어드바이스 등 외부 요청과 응답에 대한 전반적인 영역

### Service Layer
* @Service StreoType Bean에 사용되는 서비스 영역
* 일반적으로 Controller와 Repository Layer의 중간 영역에서 사용된다.
* @Transactional 을 사용하는 트랜잭션 처리 영역
* 흔히 이야기하는 비즈니스 로직을 담당하는 Layer가 아니다!!!
* 비즈니스 로직은 Domain이 담당한다.

### Repository Layer
* Database와 같이 데이터 저장소에 접근하는 영역
* 예전 DAO(Data Access Object) 영역이랑 같다.

### Dtos
* DTO(Data Transfer Object)라는 계층(레이어) 간 데이터 교환을 위한 객체들의 영역
* 컨트롤러 -> 뷰 템플릿에서 사용할 객체, Repository에서 결과로 넘어온 객체 등이 있다.
* 일반적으로 Domain과 비슷할 수도 있으나, 각 목적에 맞게 DTO를 생성하여 추가적인 필드나 메서드를 구성하는 것이 Domain의 변경을 최소화한다.

### Domain Model
* 도메인이라 불리는 개발 대상을 모든 사람이 동일한 관점에서 이해할 수 있고 공유할 수 있또록 단수환 시킨 것
* @Entity가 사용된 영역 역이 도메인 모델
* 무조건 데이터베이스의 테이블과 관계가 있어야만 도메인 모델인 것은 아니며, VO 와 같은 값 객체들도 도메인 모델에 포함된다.

### 트랜잭션 스크립트
* 트랜잭션(한번에 모두 수행되어야 하는 일련의 연산) 단위의 비즈니스 로직을 단일 함수/스크립트에서 수행하는 것
* 흔한 잘못된 Spring MVC 패턴 하에서 Service 영역에서 주로 보이는 형태
* 객체란 단순히 데이터 덩어리가 되며 계층의 역할이 무의미함
* OOP 스럽지 않음
* 낮은 응집도, 높은 결합도로 변경에 취약
  * (하나의 트랜잭션에 묶인 여러 연산들 중 하나가 변경될 경우 다른 연산들에 모두 영향을 미칠 수 있음)
* 서비스 레이어는 실제 비즈니스 로직을 각 역할/책임을 가진 도메인의 메서드를 호출하며, 트랜잭션과 도메인간의 순서만 보장
* 도메인 객체들을 호출하는 클라이언트라고 보면 될 듯


## JPA Auditing 사용하기
* 일반적으로 DB에 데이터를 생성할 때 생성시간과, 수정할 때 수정시간을 같이 기록한다.
* 회원 DB가 있을 경우 생성자와, 변경자까지도 생성과 수정시 기록을 하는 것이 일반적이다.
* 이 때 Entity 클래스들마다 createDate, updateDate 등과 같은 필드값을 매번 선언해주고, 각 비즈니스 로직에서도 각 값들을 세팅해주는 것은 번거롭다.
* Auditing은 이러한 일련의 과정들을 자동으로 도와주는 기능이다.

```java
package me.hoonmaro.study.springboot.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 1
@EntityListeners(AuditingEntityListener.class) // 2
public abstract class BaseTimeEntity {

    @CreatedDate // 3
    private LocalDateTime createDate;

    @LastModifiedDate // 4
    private LocalDateTime modifiedDate;
    
}
```
1. @MappedSuperClass
  * JPA 엔티티 클래스들이 BaseTimeEntity를 상속할 경우 필드들(createDate, modifiedDate)도 컬럼으로 인식하도록 한다.

2. @EntityListeners(AuditingEntityListener.class)
  * BaseTimeEntity 클래스에 Auditing 기능을 포함 시킨다.
  
3. @CreatedDate
  * Entity가 생성되어 저장될 때 시간이 자동 저장 된다.
  
4. @LastModifiedDate
  * 조회한 Entity의 값을 변경할 때 시간이 자동 저장 된다.
 
  