package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointService pointService;

    private static final long MAX_POINT = 1000L;

    @Nested
    @DisplayName("getUserPoint 메서드")
    class GetUserPoint {

        @Nested
        @DisplayName("비정상 케이스")
        class FailCase {

            @Test
            @DisplayName("존재하지 않는 id에 대해 요청할 때, 예외가 발생하는지 확인")
            void getUserPointFail1() {
                long invalidId = 9999L;

                when(userPointTable.selectById(anyLong())).thenReturn(UserPoint.empty(invalidId));

                assertThatThrownBy(() -> pointService.getUserPoint(invalidId))
                        .isInstanceOf(RuntimeException.class);
            }

            @Test
            @DisplayName("데이터베이스 접근 중 예외가 발생할 때, 이를 적절히 처리하는지 확인")
            void getUserPointFail2() {
                long validId = 1L;

                when(userPointTable.selectById(anyLong())).thenThrow(new RuntimeException("DB 접근 실패"));

                assertThatThrownBy(() -> pointService.getUserPoint(validId))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("DB 접근 실패");
            }
        }

        @Nested
        @DisplayName("정상 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 id가 주어졌을 때, 올바른 UserPoint 데이터를 반환하는지 확인")
            void getUserPointSuccess() {
                long validId = 1L;
                UserPoint expectedUserPoint = new UserPoint(validId, 100, System.currentTimeMillis());

                when(userPointTable.selectById(anyLong())).thenReturn(expectedUserPoint);

                UserPoint result = pointService.getUserPoint(validId);

                assertThat(result).isEqualTo(expectedUserPoint);
            }
        }
    }

    @Nested
    @DisplayName("getPointHistory 메서드")
    class GetPointHistory {

        @Nested
        @DisplayName("비정상 케이스")
        class FailCase {

            @Test
            @DisplayName("존재하지 않는 id에 대해 요청할 때, null 또는 예외가 발생하는지 확인")
            void getPointHistoryFail1() {
                long invalidUserId = 9999L;

                when(pointHistoryTable.selectAllByUserId(anyLong())).thenReturn(Collections.emptyList());

                assertThatThrownBy(() -> pointService.getPointHistory(invalidUserId))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("조회 결과가 없습니다.");
            }

            @Test
            @DisplayName("데이터베이스 접근 중 예외가 발생할 때, 이를 적절히 처리하는지 확인")
            void getPointHistoryFail2() {
                long validUserId = 1L;

                when(pointHistoryTable.selectAllByUserId(anyLong())).thenThrow(new RuntimeException("DB 접근 실패"));

                assertThatThrownBy(() -> pointService.getPointHistory(validUserId))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("DB 접근 실패");
            }
        }

        @Nested
        @DisplayName("정상 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 id가 주어졌을 때, 올바른 PointHistory 데이터를 반환하는지 확인")
            void getPointHistorySuccess() {
                long validUserId = 1L;
                List<PointHistory> expectedPointHistory = List.of(new PointHistory(1, validUserId, 100, TransactionType.USE, System.currentTimeMillis()));

                when(pointHistoryTable.selectAllByUserId(anyLong())).thenReturn(expectedPointHistory);

                List<PointHistory> result = pointService.getPointHistory(validUserId);

                assertThat(result).isEqualTo(expectedPointHistory);
            }
        }
    }

    @Nested
    @DisplayName("chargePoint 메서드")
    class chargePoint {

        @Nested
        @DisplayName("비정상 케이스")
        class FailCase {

            @Test
            @DisplayName("존재하지 않는 id에 대해 요청할 때, 예외가 발생하는지 확인")
            void chargePointFail1() {
                long invalidId = 9999L;
                long amount = 100L;

                when(userPointTable.selectById(anyLong())).thenReturn(UserPoint.empty(invalidId));

                assertThatThrownBy(() -> pointService.chargePoint(invalidId, amount))
                        .isInstanceOf(RuntimeException.class);
            }

            @Test
            @DisplayName("데이터베이스 접근 중 예외가 발생할 때, 이를 적절히 처리하는지 확인")
            void chargePointFail2() {
                long validId = 1L;
                long amount = 100L;

                when(userPointTable.selectById(anyLong())).thenThrow(new RuntimeException("DB 접근 실패"));

                assertThatThrownBy(() -> pointService.chargePoint(validId, amount))
                        .isInstanceOf(RuntimeException.class);
            }

            @Test
            @DisplayName("포인트 충전이 실패했을 경우 통계 저장이 이루어지지 않는지 확인")
            void chargePointFail3() {
                long validId = 1L;
                long amount = 100L;
                UserPoint currentUserPoint = new UserPoint(validId, 200, System.currentTimeMillis());

                when(userPointTable.selectById(validId)).thenReturn(currentUserPoint);
                when(userPointTable.insertOrUpdate(validId, 300L)).thenThrow(new RuntimeException("충전 실패"));

                // 충전 내역 저장이 호출되지 않도록 확인
                assertThatThrownBy(() -> pointService.chargePoint(validId, amount))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("충전 실패");

                // 내역 저장 메서드가 호출되지 않았는지 확인
                verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
            }

            @Test
            @DisplayName("최대잔고이상 충전을 시도할 때, 예외가 발생하는지 확인")
            void chargePointFail4() {
                long validId = 1L;
                long amount = 1100L; // 최대 포인트를 초과하는 충전 금액
                UserPoint currentUserPoint = new UserPoint(validId, 200, System.currentTimeMillis());

                when(userPointTable.selectById(validId)).thenReturn(currentUserPoint);

                assertThatThrownBy(() -> pointService.chargePoint(validId, amount))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessageContaining("충전 가능한 최대 포인트는 1000 입니다.");
            }
        }

        @Nested
        @DisplayName("정상 케이스")
        class SuccessCase {

            @Test
            @DisplayName("유효한 파라미터가 주어졌을 때, 포인트가 성공적으로 증가하는지 확인")
            void chargePointSuccess1() {
                long validId = 1L;
                long amount = 100L;
                UserPoint currentUserPoint = new UserPoint(validId, 200, System.currentTimeMillis());

                when(userPointTable.selectById(validId)).thenReturn(currentUserPoint);
                when(userPointTable.insertOrUpdate(validId, 300L)).thenReturn(new UserPoint(validId, 300, System.currentTimeMillis()));

                UserPoint result = pointService.chargePoint(validId, amount);

                assertThat(result.point()).isEqualTo(300L);

            }

            @Test
            @DisplayName("포인트 충전 내역이 PointHistoryTable에 제대로 저장되는지 확인")
            void chargePointSuccess2() {
                long validId = 1L;
                long amount = 100L;
                UserPoint currentUserPoint = new UserPoint(validId, 200, System.currentTimeMillis());

                when(userPointTable.selectById(validId)).thenReturn(currentUserPoint);
                when(userPointTable.insertOrUpdate(validId, 300L)).thenReturn(new UserPoint(validId, 300, System.currentTimeMillis()));
                when(pointHistoryTable.insert(validId, amount, TransactionType.CHARGE, System.currentTimeMillis()))
                        .thenReturn(new PointHistory(1, validId, amount, TransactionType.CHARGE, System.currentTimeMillis()));

                // 포인트 충전 메서드 호출
                UserPoint result = pointService.chargePoint(validId, amount);

                // 결과 검증: 포인트가 300L인지 확인
                assertThat(result.point()).isEqualTo(300L);

                System.out.println(pointHistoryTable.selectAllByUserId(validId));
                // pointHistoryTable의 insert 메서드가 호출되었는지 검증
                verify(pointHistoryTable).insert(eq(validId), eq(amount), eq(TransactionType.CHARGE), anyLong());
            }
        }
    }
}