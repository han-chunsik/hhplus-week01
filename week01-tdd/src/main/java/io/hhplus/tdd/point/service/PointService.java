package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private static final Logger log = LoggerFactory.getLogger(PointService.class);

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    /**
     * User Point 조회
     * @param id
     * @return userPoint
     */
    public UserPoint getUserPoint(long id) {
        // 데이터 요청
        UserPoint userPoint = getPoint(id);
        return userPoint;
    }

    /**
     * Point history 조회
     * @param userId
     * @return pointHistory
     */
    public List<PointHistory> getPointHistory(long userId) {
        List<PointHistory> pointHistory = pointHistoryTable.selectAllByUserId(userId);

        if (pointHistory.isEmpty()) { // isEmpty 메서드를 통해 비어 있는지 확인
            throw new RuntimeException("조회 결과가 없습니다.");
        }
        return pointHistory;
    }

    /**
     * Point 충전
     * @param id
     * @param amount
     * @return UserPoint
     */
    public UserPoint changePoint(long id, long amount) {
        // 포인트 충전
        UserPoint cruuntUserPoint = getPoint(id);

        long chargedPoint;

        if ((cruuntUserPoint.point() + amount) > 1000) {
            throw new RuntimeException("포인트는 최대 1000까지 충전 가능합니다.");
        }else{
            chargedPoint = cruuntUserPoint.point() + amount;
        }

        UserPoint userPoint = userPointTable.insertOrUpdate(id, chargedPoint);

        if (cruuntUserPoint.point() > userPoint.point()) {
            throw new RuntimeException("포인트 충전에 실패했습니다.");
        }

        PointHistory pointHistory = pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
        return userPoint;
    }

    /**
     * Point 사용
     * @param id
     * @param amount
     * @return UserPoint
     */
    public UserPoint usePoint(long id, long amount) {
        // 포인트 사용
        UserPoint cruuntUserPoint = getPoint(id);
        long usedPoint;

        if (cruuntUserPoint.point() < amount) {
            throw new RuntimeException("사용가능한 포인트가 없습니다.");
        }else{
            usedPoint = cruuntUserPoint.point() - amount;
        }

        UserPoint userPoint = userPointTable.insertOrUpdate(id, usedPoint);

        if (cruuntUserPoint.point() < userPoint.point()) {
            throw new RuntimeException("포인트 사용에 실패했습니다.");
        }

        PointHistory pointHistory = pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());
        return userPoint;
    }

    private UserPoint getPoint(long id) {
        UserPoint cruuntUserPoint = userPointTable.selectById(id);

        if (cruuntUserPoint.equals(UserPoint.empty(id))) { // empty 메서드를 통해 비어 있는지 확인
            throw new RuntimeException("조회 결과가 없습니다.");
        }
        return cruuntUserPoint;
    }
}
