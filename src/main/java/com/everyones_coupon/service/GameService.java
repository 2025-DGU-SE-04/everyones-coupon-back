package com.everyones_coupon.service;

import com.everyones_coupon.domain.Game;
import com.everyones_coupon.dto.GameCreateRequest;
import com.everyones_coupon.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    /*
    게임 추가 기능
    중복된 게임 이름이 있으면 예외 발생 
    */
    @Transactional
    public Long addGame(GameCreateRequest request) {
        // 중복 검사 (이미 동일한 게임명이 존재하는 경우 등록 차단)
        if (gameRepository.existsByTitle(request.getTitle())) {
            throw new IllegalArgumentException("이미 등록된 게임입니다.");
        }

        // 엔티티 변환 및 저장
        Game game = request.toEntity();
        Game savedGame = gameRepository.save(game);
        
        return savedGame.getId();
    }

    /*
    요즘 뜨는 게임 조회
    조회수가 높은 순서대로 상위 10개 반환 
    */
    @Transactional(readOnly = true)
    public List<Game> getTrendingGames() {
        return gameRepository.findTop10ByOrderByViewCountDesc();
    }
    
    /*
    게임 검색 기능
    검색어가 포함된 게임 목록 반환
    */
    @Transactional(readOnly = true)
    public List<Game> searchGames(String keyword) {
        return gameRepository.findByTitleContaining(keyword);
    }
}