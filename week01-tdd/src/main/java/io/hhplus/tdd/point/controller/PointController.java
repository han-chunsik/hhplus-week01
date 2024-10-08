package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final PointService pointService;
    public PointController(PointService pointService) {
        this.pointService = pointService;
    }
    @GetMapping("/")
    public String home() {
        return "홈페이지입니다.";
    }
    /**
     * 특정 유저의 포인트를 조회하는 기능
     * @param id 유저 ID
     * @return UserPoint
     */
    @GetMapping("/{id}")
    public UserPoint point(@PathVariable(name = "id") long id) throws Exception {
        validLong(id);

        return pointService.getUserPoint(id);
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
     * @param id 유저 ID
     * @return List<>
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(@PathVariable long id) throws Exception {
        validLong(id);

        return pointService.getPointHistory(id);
    }

    /**
     * 특정 유저의 포인트를 충전하는 기능
     * @param id
     * @param amount
     * @return UserPoint
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge (@PathVariable long id, @RequestBody long amount) throws Exception {
        validLong(id);
        validLong(amount);

        return pointService.chargePoint(id, amount);
    }

    /**
     * 특정 유저의 포인트를 사용하는 기능
     * @param id
     * @param amount
     * @return UserPoint
     */
    @PatchMapping("{id}/use")
    public UserPoint use(@PathVariable long id, @RequestBody long amount) throws Exception {
        validLong(id);
        validLong(amount);

        return pointService.usePoint(id, amount);
    }

    private static void validLong(long num) throws Exception {
        if (num <= 0) {
            throw new BadRequestException("1 이상의 정수여야 합니다.");
        }
    }
}
