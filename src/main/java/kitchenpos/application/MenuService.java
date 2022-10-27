package kitchenpos.application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.dao.MenuGroupRepository;
import kitchenpos.dao.MenuProductRepository;
import kitchenpos.dao.MenuRepository;
import kitchenpos.dao.ProductRepository;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.MenuProductDto;
import kitchenpos.domain.Price;
import kitchenpos.domain.Product;
import kitchenpos.domain.Quantity;
import kitchenpos.dto.MenuDto;
import kitchenpos.exception.MenuGroupNotFoundException;
import kitchenpos.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final MenuProductRepository menuProductRepository;
    private final ProductRepository productRepository;

    public MenuService(
            final MenuRepository menuRepository,
            final MenuGroupRepository menuGroupRepository,
            final MenuProductRepository menuProductRepository,
            final ProductRepository productRepository
    ) {
        this.menuRepository = menuRepository;
        this.menuGroupRepository = menuGroupRepository;
        this.menuProductRepository = menuProductRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Menu create(final MenuDto menuDto) {
        Price menuPrice = Price.from(menuDto.getPrice());
        List<MenuProduct> menuProducts = toMenuProducts(menuDto.getMenuProducts());
        Menu menu = Menu.of(menuDto.getName(), menuPrice, findMenuGroup(menuDto), menuProducts);
        Menu saved = menuRepository.save(menu);
        menuProductRepository.saveAll(menuProducts);
        return saved;
    }

    private List<MenuProduct> toMenuProducts(List<MenuProductDto> menuProductDtos) {
        List<MenuProduct> menuProducts = new ArrayList<>();
        for (MenuProductDto menuProductDto : menuProductDtos) {
            Product product = productRepository.findById(menuProductDto.getProductId())
                    .orElseThrow(ProductNotFoundException::new);
            MenuProduct menuProduct = new MenuProduct(null, product, Quantity.from(menuProductDto.getQuantity()));
            menuProducts.add(menuProduct);
        }
        return menuProducts;
    }

    private MenuGroup findMenuGroup(MenuDto menuDto) {
        return menuGroupRepository.findById(menuDto.getMenuGroupId())
                .orElseThrow(MenuGroupNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<MenuDto> list() {
        return menuRepository.findAll()
                .stream()
                .map(MenuDto::new)
                .collect(Collectors.toUnmodifiableList());
    }
}
