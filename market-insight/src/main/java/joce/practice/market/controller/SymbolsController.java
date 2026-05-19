package joce.practice.market.controller;

import joce.practice.market.infra.entity.StockSignalDaily;
import joce.practice.market.infra.entity.StockSymbolEntity;
import joce.practice.market.infra.repository.StockSignalDailyRepository;
import joce.practice.market.infra.repository.StockSymbolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/symbol")
@RequiredArgsConstructor
public class SymbolsController {

    private final StockSignalDailyRepository stockSignalDailyRepository;
    private final StockSymbolRepository stockSymbolRepository;

    @GetMapping("/all")
    public List<StockSymbolEntity> getAllSymbols() {
         return stockSymbolRepository
                .findAll();
    }

}