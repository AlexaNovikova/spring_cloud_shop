package ru.otus.product.service.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.otus.product.service.model.entity.Product;
import ru.otus.product.lib.api.ProductDto;

@Mapper(componentModel = "spring")
public interface ProductMapper {

     @Mapping(target = "categoryTitle", source = "product.category.title")
     ProductDto map(Product product);

}
