package me.hoonmaro.study.springboot.service.posts;

import lombok.RequiredArgsConstructor;
import me.hoonmaro.study.springboot.domain.posts.Posts;
import me.hoonmaro.study.springboot.domain.posts.PostsRepository;
import me.hoonmaro.study.springboot.web.dto.PostListResponseDto;
import me.hoonmaro.study.springboot.web.dto.PostsResponseDto;
import me.hoonmaro.study.springboot.web.dto.PostsSaveRequestDto;
import me.hoonmaro.study.springboot.web.dto.PostsUpdateRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostsService {

    private final PostsRepository postsRepository;

    @Transactional
    public Long save(PostsSaveRequestDto requestDto) {
        return postsRepository.save(requestDto.toEntity()).getId();
    }

    @Transactional
    public Long update(Long id, PostsUpdateRequestDto requestDto) {

        Posts posts = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("해당 게시글이 없습니다. id=%d", id)));
        posts.update(requestDto.getTitle(), requestDto.getContent());

        return id;
    }

    @Transactional(readOnly = true)
    public PostsResponseDto findById(Long id) {
        Posts posts = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("해당 게시글이 없습니다. id=%d", id)));

        return new PostsResponseDto(posts);
    }

    @Transactional(readOnly = true)
    public List<PostListResponseDto> findAllDesc() {
        return postsRepository.findAllDesc().stream()
                .map(PostListResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id) {
        Posts posts = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
        postsRepository.delete(posts);
    }
}
