package com.everyones_coupon.repository;

import com.everyones_coupon.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    // 메인 화면: '요즘 뜨는 게임' (조회수 내림차순으로 상위 10개 가져오기)
    List<Game> findTop10ByOrderByViewCountDesc();

    // 검색 화면: 게임 이름 검색
    List<Game> findByTitleContaining(String keyword);

    // 게임 추가 시: 이름 중복 검사
    boolean existsByTitle(String title);
    
    // 게임 상세 진입 시 이름으로 조회
    Optional<Game> findByTitle(String title);
}