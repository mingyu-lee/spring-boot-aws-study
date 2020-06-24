package me.hoonmaro.study.springboot.service.posts;

import lombok.RequiredArgsConstructor;
import me.hoonmaro.study.springboot.domain.posts.PostsRepository;
import me.hoonmaro.study.springboot.web.dto.PostsSaveRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostsService {

    private final PostsRepository postsRepository;

    @Transactional
    public Long save(PostsSaveRequestDto requestDto) {
        return postsRepository.save(requestDto.toEntity()).getId();
    }
}
