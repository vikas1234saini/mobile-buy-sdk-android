package com.shopify.sample.data.apollo;

import com.shopify.sample.domain.CollectionPageWithProductsQuery;
import com.shopify.sample.domain.CollectionProductPageQuery;
import com.shopify.sample.domain.ProductByIdQuery;
import com.shopify.sample.domain.model.Collection;
import com.shopify.sample.domain.model.Product;
import com.shopify.sample.domain.model.ProductDetail;
import com.shopify.sample.util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.shopify.sample.util.Util.mapItems;

public final class Converter {

  public static List<Collection> convertCollections(final CollectionPageWithProductsQuery.CollectionConnection collectionConnection) {
    List<Collection> collections = new ArrayList<>();
    for (CollectionPageWithProductsQuery.Edge collectionEdge : collectionConnection.edges) {
      final CollectionPageWithProductsQuery.Collection collection = collectionEdge.collection;
      collections.add(new Collection(
        collection.id,
        collection.title,
        collection.descriptionHtml,
        collection.image.orNull() != null ? collection.image.get().src : null,
        collectionEdge.cursor,
        convertProducts(collection.productConnection)
      ));
    }
    return collections;
  }

  public static List<Product> convertProducts(final CollectionPageWithProductsQuery.ProductConnection productConnection) {
    List<Product> products = new ArrayList<>();
    for (CollectionPageWithProductsQuery.Edge1 productEdge : productConnection.edges) {
      final CollectionPageWithProductsQuery.Product product = productEdge.product;
      products.add(new Product(
        product.id,
        product.title,
        Util.reduce(product.imageConnection.edges, (acc, val) -> val.image.src, null),
        Util.reduce(product.variantConnection.variantEdge, (acc, val) -> val.variant.price, BigDecimal.ZERO),
        productEdge.cursor
      ));
    }
    return products;
  }

  public static List<Product> convertProducts(final CollectionProductPageQuery.ProductConnection productConnection) {
    List<Product> products = new ArrayList<>();
    for (CollectionProductPageQuery.ProductEdge productEdge : productConnection.productEdges) {
      final CollectionProductPageQuery.Product product = productEdge.product;
      products.add(new Product(
        product.id,
        product.title,
        Util.reduce(product.imageConnection.imageEdges, (acc, val) -> val.image.src, null),
        Util.reduce(product.variantConnection.variantEdges, (acc, val) -> val.variant.price, BigDecimal.ZERO),
        productEdge.cursor
      ));
    }
    return products;
  }

  public static ProductDetail convertProductDetail(ProductByIdQuery.AsProduct product) {
    List<String> images = mapItems(product.imageConnection.imageEdge, imageEdge -> imageEdge.image.src);
    List<ProductDetail.Option> options = mapItems(product.options, option -> new ProductDetail.Option(option.id, option.name,
      option.values));
    List<ProductDetail.Variant> variants = mapItems(product.variantConnection.variantEdge,
      variantEdge -> {
        List<ProductDetail.SelectedOption> selectedOptions = mapItems(variantEdge.variant.selectedOptions, selectedOption ->
          new ProductDetail.SelectedOption(selectedOption.name, selectedOption.value));
        return new ProductDetail.Variant(variantEdge.variant.id, variantEdge.variant.title, variantEdge.variant.availableForSale,
          selectedOptions, variantEdge.variant.price);
      });
    return new ProductDetail(product.id, product.title, product.descriptionHtml, product.tags, images, options, variants);
  }

  private Converter() {
  }
}
