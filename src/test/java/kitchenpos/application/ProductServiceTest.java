package kitchenpos.application;

import static kitchenpos.Fixture.DomainFixture.createProduct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceTest
class ProductServiceTest {

    private static final String PRODUCE_NAME = "빅맥";
    private static final BigDecimal PRICE = new BigDecimal(5000);

    @Autowired
    private ProductService productService;

    @DisplayName("상품을 생성할 수 있다.")
    @Test
    void create() {
        Product product = createProduct(PRODUCE_NAME, PRICE);

        productService.create(product);

        List<Product> products = productService.list();
        List<String> productNames = products.stream()
                .map(Product::getName)
                .collect(Collectors.toUnmodifiableList());
        assertAll(
                () -> assertThat(products).hasSize(7),
                () -> assertThat(productNames).contains(PRODUCE_NAME)
        );
    }

    @DisplayName("상품 가격이 존재하지 않으면 예외를 발생시킨다.")
    @Test
    void create_NullPrice() {
        Product product = createProduct(PRODUCE_NAME, null);

        assertThatThrownBy(() -> productService.create(product))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품 가격이 0보다 작으면 예외를 발생시킨다.")
    @Test
    void create_InvalidPrice() {
        BigDecimal invalidPrice = new BigDecimal(-1);
        Product product = createProduct(PRODUCE_NAME, invalidPrice);

        assertThatThrownBy(() -> productService.create(product))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
