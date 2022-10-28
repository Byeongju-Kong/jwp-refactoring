package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.Optional;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.TableGroupRepository;
import kitchenpos.dao.TableRepository;
import kitchenpos.domain.GuestNumber;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.dto.TableDto;
import kitchenpos.exception.GuestNumberChangeDisabledException;
import kitchenpos.exception.InvalidGuestNumberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

class TableServiceTest extends ServiceTest {

    private static final GuestNumber GUEST_NUMBER = GuestNumber.from(3);

    @Autowired
    private TableService tableService;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private TableGroupRepository tableGroupRepository;

    @Autowired
    private OrderDao orderDao;

    @DisplayName("Table을 생성할 수 있다.")
    @Test
    void create() {
        TableDto tableDto = new TableDto(null, 3, false);

        tableService.create(tableDto);

        assertThat(tableService.list()).hasSize(1);
    }

    @DisplayName("0보다 작은 수로 Table을 생성하려고 하면 예외를 발생시킨다.")
    @Test
    void create_Exception_InvalidGuestNumber() {
        int invalidGuestNumber = 0;
        TableDto tableDto = new TableDto(null, invalidGuestNumber, false);

        assertThatThrownBy(() -> tableService.create(tableDto))
                .isInstanceOf(InvalidGuestNumberException.class);
    }

    @DisplayName("Table의 empty 상태를 변경할 수 있다.")
    @Test
    void changeEmpty() {
        TableDto tableDto = tableService.create(new TableDto(null, 3, false));

        tableService.changeEmpty(tableDto.getId(), new TableDto(tableDto.getId(), 3, true));

        OrderTable changedOrderTable = tableRepository.findAll()
                .stream()
                .filter(OrderTable::isEmpty)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
        assertThat(changedOrderTable.isEmpty()).isTrue();
    }

    @DisplayName("TableGroup에 속해있는 Table의 empty 상태를 변경하려고 하면 예외를 발생시킨다.")
    @Test
    void changeEmpty_Exception_GroupedTable() {
        TableGroup tableGroup = tableGroupRepository.save(new TableGroup());
        OrderTable orderTable = tableRepository.save(new OrderTable(null, GUEST_NUMBER, true, tableGroup));

        assertThatThrownBy(
                () -> tableService.changeEmpty(orderTable.getId(), new TableDto(tableGroup.getId(), 3, true)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Table Group으로 묶인 테이블은 empty를 변경할 수 없습니다.");
    }

    @DisplayName("COOKING, MEAL 상태인 Table의 상태를 변경하려고 하면 예외를 발생시킨다.")
    @ParameterizedTest
    @ValueSource(strings = {"COOKING", "MEAL"})
    void changeEmpty_Exception_NotCompleteOrderStatus(String orderStatus) {
        OrderTable orderTable = tableRepository.save(new OrderTable(null, GUEST_NUMBER, false, null));
        orderDao.save(new Order(orderTable.getId(), orderStatus, Collections.emptyList()));

        assertThatThrownBy(() -> tableService.changeEmpty(orderTable.getId(), new TableDto(null, 3, true)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조리중이거나 식사중인 테이블의 empty를 변경할 수 없습니다.");
    }

    @DisplayName("Table의 numberOfGuests 수를 변경할 수 있다.")
    @Test
    void changeNumberOfGuests() {
        OrderTable orderTable = tableRepository.save(new OrderTable(null, GUEST_NUMBER, false, null));

        tableService.changeNumberOfGuests(orderTable.getId(), new TableDto(null, 200, false));

        Optional<OrderTable> changedOrderTable = tableRepository.findAll()
                .stream()
                .filter(orderTable1 -> orderTable1.getGuestNumber() == 200)
                .findAny();
        assertThat(changedOrderTable).isNotEmpty();
    }

    @DisplayName("0보다 작은 numberOfGuests로 Table을 변경하려고 하면 예외를 발생시킨다.")
    @Test
    void changeNumberOfGuests_Exception_InvalidNumberOfGuests() {
        OrderTable orderTable = tableRepository.save(new OrderTable(null, GUEST_NUMBER, false, null));
        int invalidNumberOfGuests = -1;

        assertThatThrownBy(
                () -> tableService.changeNumberOfGuests(
                        orderTable.getId(), new TableDto(null, invalidNumberOfGuests, false)))
                .isInstanceOf(InvalidGuestNumberException.class);
    }

    @DisplayName("empty 상태인 Table의 numberOfGuests 수를 변경하려고 하면 예외를 발생시킨다.")
    @Test
    void changeNumberOfGuests_Exception_EmptyOrderTable() {
        OrderTable orderTable = tableRepository.save(new OrderTable(null, GUEST_NUMBER, true, null));

        assertThatThrownBy(
                () -> tableService.changeNumberOfGuests(orderTable.getId(), new TableDto(null, 10, true)))
                .isInstanceOf(GuestNumberChangeDisabledException.class);
    }
}
