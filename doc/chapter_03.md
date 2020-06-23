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
