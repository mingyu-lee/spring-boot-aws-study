package me.hoonmaro.study.springboot.domain.posts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PostsRepositoryTest {

    @Autowired
    PostsRepository postsRepository;

    @AfterEach
    public void cleanup() {
        postsRepository.deleteAll();
    }

    @DisplayName("게시글저장_불러오기")
    @Test
    void saveArticle_findAll() {
        String title = "테스트 제목";
        String content = "테스트 내용";
        String author = "훈마로";

        postsRepository.save(Posts.builder()
                .title(title)
                .content(content)
                .author(author)
                .build());

        // when
        List<Posts> postsList = postsRepository.findAll();

        // then
        Posts posts = postsList.get(0);
        assertEquals(title, posts.getTitle());
        assertEquals(content, posts.getContent());
        assertEquals(author, posts.getAuthor());
    }

    @DisplayName("BaseTimeEntity 등록")
    @Test
    void saveBaseTimeEntity() {
        // given
        LocalDateTime now = LocalDateTime.of(2020,3,2,0,0,0);
        postsRepository.save(Posts.builder()
                .title("제목")
                .content("본문")
                .author("훈마로")
                .build());

        // when
        List<Posts> postsList = postsRepository.findAll();

        // then
        Posts posts = postsList.get(0);
        System.out.printf(">>>>>> createDate=%s\n>>>>>> modifiedDate=%s\n", posts.getCreateDate(), posts.getModifiedDate());

        assertThat(posts.getCreateDate()).isAfter(now);
        assertThat(posts.getModifiedDate()).isAfter(now);
    }

}
