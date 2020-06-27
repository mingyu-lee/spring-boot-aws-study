package me.hoonmaro.study.springboot.web;

import me.hoonmaro.study.springboot.domain.posts.Posts;
import me.hoonmaro.study.springboot.domain.posts.PostsRepository;
import me.hoonmaro.study.springboot.web.dto.PostsSaveRequestDto;
import me.hoonmaro.study.springboot.web.dto.PostsUpdateRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostsRepository postsRepository;

    @AfterEach
    public void tearDown() {
        postsRepository.deleteAll();
    }

    @DisplayName("Posts 객체 등록")
    @Test
    void posts_save() {
        String title = "제목";
        String content = "본문";
        String author = "훈마로";

        PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();

        String url = UriComponentsBuilder.fromHttpUrl("http://localhost").port(port).path("/api/v1/posts").build().toUriString();

        // when
        ResponseEntity<Long> responseEntity = restTemplate.postForEntity(url, requestDto, Long.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(Long.valueOf(0L));

        List<Posts> postsList = postsRepository.findAll();
        assertThat(postsList.get(0).getTitle()).isEqualTo(title);
        assertThat(postsList.get(0).getContent()).isEqualTo(content);
        assertThat(postsList.get(0).getAuthor()).isEqualTo(author);

    }

    @DisplayName("Posts 객체 수정")
    @Test
    void posts_update() {
        // given
        Posts savedPosts = postsRepository.save(Posts.builder()
                .title("제목")
                .content("내용")
                .author("훈마로")
                .build());

        Long updatedId = savedPosts.getId();
        String expectedTitle = "제목2";
        String expectedContent = "본문2";

        PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder()
                .title(expectedTitle)
                .content(expectedContent)
                .build();

        String url = UriComponentsBuilder.fromHttpUrl("http://localhost").port(port)
                .path("/api/v1/posts/").path(String.valueOf(updatedId)).build().toUriString();

        HttpEntity<PostsUpdateRequestDto> requestDtoHttpEntity = new HttpEntity<>(requestDto);

        // when
        ResponseEntity<Long> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestDtoHttpEntity, Long.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(0L);

        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
        assertThat(all.get(0).getContent()).isEqualTo(expectedContent);
    }

    @DisplayName("Posts 객체 삭제")
    @Test
    void posts_delete() {
        // given
        Posts savedPosts = postsRepository.save(Posts.builder()
                .title("제목")
                .content("내용")
                .author("훈마로")
                .build());

        Long deleteId = savedPosts.getId();

        String url = UriComponentsBuilder.fromHttpUrl("http://localhost").port(port)
                .path("/api/v1/posts/").path(String.valueOf(deleteId)).build().toUriString();

        // when
        ResponseEntity<Long> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, Long.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(deleteId);

        List<Posts> all = postsRepository.findAll();
        assertThat(all.isEmpty()).isTrue();
    }

}
