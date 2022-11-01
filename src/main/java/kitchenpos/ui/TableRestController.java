package kitchenpos.ui;

import java.net.URI;
import java.util.List;
import kitchenpos.application.TableService;
import kitchenpos.dto.request.OrderTableCreateRequest;
import kitchenpos.dto.request.OrderTableEmptyChangeRequest;
import kitchenpos.dto.request.OrderTableGuestNumberChangeRequest;
import kitchenpos.dto.response.OrderTableResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tables")
public class TableRestController {

    private final TableService tableService;

    public TableRestController(final TableService tableService) {
        this.tableService = tableService;
    }

    @PostMapping
    public ResponseEntity<OrderTableResponse> create(@RequestBody OrderTableCreateRequest orderTableCreateRequest) {
        OrderTableResponse created = tableService.create(orderTableCreateRequest);
        URI uri = URI.create("/api/tables/" + created.getId());
        return ResponseEntity.created(uri)
                .body(created)
                ;
    }

    @GetMapping
    public ResponseEntity<List<OrderTableResponse>> list() {
        return ResponseEntity.ok()
                .body(tableService.list())
                ;
    }

    @PutMapping("/{orderTableId}/empty")
    public ResponseEntity<OrderTableResponse> changeEmpty(@PathVariable Long orderTableId,
                                                          @RequestBody OrderTableEmptyChangeRequest
                                                                  orderTableEmptyChangeRequest) {
        return ResponseEntity.ok()
                .body(tableService.changeEmpty(orderTableId, orderTableEmptyChangeRequest))
                ;
    }

    @PutMapping("/api/tables/{orderTableId}/number-of-guests")
    public ResponseEntity<OrderTableResponse> changeNumberOfGuests(@PathVariable Long orderTableId,
                                                                   @RequestBody OrderTableGuestNumberChangeRequest
                                                                           orderTableGuestNumberChangeRequest) {
        return ResponseEntity.ok()
                .body(tableService.changeNumberOfGuests(orderTableId, orderTableGuestNumberChangeRequest))
                ;
    }
}
