package com.everyones_coupon.controller;

import com.everyones_coupon.domain.Game;
import com.everyones_coupon.dto.GameCreateRequest;
import com.everyones_coupon.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    // 게임 추가 API (POST /api/games)
    @PostMapping
    public ResponseEntity<Long> createGame(@RequestBody GameCreateRequest request) {
        Long gameId = gameService.addGame(request);
        return ResponseEntity.ok(gameId);
    }

    // 요즘 뜨는 게임 목록 조회 API (GET /api/games/trending)
    @GetMapping("/trending")
    public ResponseEntity<List<Game>> getTrendingGames() {
        List<Game> games = gameService.getTrendingGames();
        return ResponseEntity.ok(games);
    }

    // 게임 검색 API (GET /api/games/search?keyword=게임명)
    @GetMapping("/search")
    public ResponseEntity<List<Game>> searchGames(@RequestParam("keyword") String keyword) {
        List<Game> games = gameService.searchGames(keyword);
        return ResponseEntity.ok(games);
    }

    // 게임 조회 API (GET /api/games/{gameId})
    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGame(@PathVariable("gameId") Long  gameId) {
        Game game = gameService.getGame(gameId);
        return ResponseEntity.ok(game);
    }

    // 전체 게임 조회 (GET /api/games)
    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gameService.getAllGames();
        return ResponseEntity.ok(games);
    }
}