package software.openlab.catalog.infra.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import software.openlab.catalog.application.usecase.product.AddProductBarcodeUseCase;
import software.openlab.catalog.application.usecase.product.AddProductImageUseCase;
import software.openlab.catalog.application.usecase.product.AutocompleteProductsUseCase;
import software.openlab.catalog.application.usecase.product.ChangeProductPriceUseCase;
import software.openlab.catalog.application.usecase.product.ChangeProductStatusUseCase;
import software.openlab.catalog.application.usecase.product.CreateProductUseCase;
import software.openlab.catalog.application.usecase.product.DeleteProductBarcodeUseCase;
import software.openlab.catalog.application.usecase.product.DeleteProductImageUseCase;
import software.openlab.catalog.application.usecase.product.DeleteProductUseCase;
import software.openlab.catalog.application.usecase.product.ExportProductsUseCase;
import software.openlab.catalog.application.usecase.product.GetProductAttributesUseCase;
import software.openlab.catalog.application.usecase.product.GetProductBarcodesUseCase;
import software.openlab.catalog.application.usecase.product.GetProductByIdUseCase;
import software.openlab.catalog.application.usecase.product.GetProductImagesUseCase;
import software.openlab.catalog.application.usecase.product.GetProductPriceHistoryUseCase;
import software.openlab.catalog.application.usecase.product.ImportProductsUseCase;
import software.openlab.catalog.application.usecase.product.ListProductsUseCase;
import software.openlab.catalog.application.usecase.product.ReplaceProductAttributesUseCase;
import software.openlab.catalog.application.usecase.product.SetPrimaryProductImageUseCase;
import software.openlab.catalog.application.usecase.product.UpdateProductUseCase;
import software.openlab.catalog.domain.brand.BrandId;
import software.openlab.catalog.domain.category.CategoryId;
import software.openlab.catalog.domain.product.Product;
import software.openlab.catalog.domain.product.ProductAttribute;
import software.openlab.catalog.domain.product.ProductBarcodeId;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.product.ProductImageId;
import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.Csv;
import software.openlab.catalog.domain.supplier.SupplierId;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureId;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.AddBarcodeRequest;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.AddImageRequest;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.AttributeResponse;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.AutocompleteResponse;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.BarcodeResponse;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.ChangePriceRequest;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.ChangeStatusRequest;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.CreateProductRequest;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.ImageResponse;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.ImportReportResponse;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.PriceHistoryResponse;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.ProductPageResponse;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.ProductResponse;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.ReplaceAttributesRequest;
import software.openlab.catalog.infra.rest.dto.ProductDTOs.UpdateProductRequest;

@Path("/v1/products")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    CreateProductUseCase createProduct;

    @Inject
    GetProductByIdUseCase getProductById;

    @Inject
    ListProductsUseCase listProducts;

    @Inject
    UpdateProductUseCase updateProduct;

    @Inject
    ChangeProductStatusUseCase changeProductStatus;

    @Inject
    DeleteProductUseCase deleteProduct;

    @Inject
    GetProductAttributesUseCase getProductAttributes;

    @Inject
    ReplaceProductAttributesUseCase replaceProductAttributes;

    @Inject
    GetProductBarcodesUseCase getProductBarcodes;

    @Inject
    AddProductBarcodeUseCase addProductBarcode;

    @Inject
    DeleteProductBarcodeUseCase deleteProductBarcode;

    @Inject
    GetProductImagesUseCase getProductImages;

    @Inject
    AddProductImageUseCase addProductImage;

    @Inject
    SetPrimaryProductImageUseCase setPrimaryProductImage;

    @Inject
    DeleteProductImageUseCase deleteProductImage;

    @Inject
    ChangeProductPriceUseCase changeProductPrice;

    @Inject
    GetProductPriceHistoryUseCase getProductPriceHistory;

    @Inject
    ImportProductsUseCase importProducts;

    @Inject
    ExportProductsUseCase exportProducts;

    @Inject
    AutocompleteProductsUseCase autocompleteProducts;

    @POST
    public Response create(@Valid CreateProductRequest request) {
        var created = createProduct.execute(
                request.description(),
                request.shortDescription(),
                request.type(),
                UnitOfMeasureId.of(request.unitOfMeasureId()),
                CategoryId.of(request.categoryId()),
                toBrandId(request.brandId()),
                toSupplierId(request.defaultSupplierId()));
        return Response.status(Response.Status.CREATED)
                .entity(toResponse(created))
                .build();
    }

    @GET
    public ProductPageResponse list(
            @QueryParam("q") String q,
            @QueryParam("category_id") String categoryId,
            @QueryParam("status") String status,
            @QueryParam("type") String type,
            @QueryParam("brand_id") String brandId,
            @QueryParam("page") Integer page,
            @QueryParam("page_size") Integer pageSize) {
        CategoryId categoryIdFilter = (categoryId == null || categoryId.isBlank()) ? null : CategoryId.of(categoryId);
        BrandId brandIdFilter = toBrandId(brandId);
        var result = listProducts.execute(q, categoryIdFilter, status, type, brandIdFilter, page, pageSize);
        return ProductPageResponse.from(result, this::toResponse);
    }

    @GET
    @Path("/export")
    @Produces("text/csv")
    public Response export(
            @QueryParam("q") String q,
            @QueryParam("category_id") String categoryId,
            @QueryParam("status") String status,
            @QueryParam("type") String type,
            @QueryParam("brand_id") String brandId) {
        CategoryId categoryIdFilter = (categoryId == null || categoryId.isBlank()) ? null : CategoryId.of(categoryId);
        BrandId brandIdFilter = toBrandId(brandId);
        List<Product> products = exportProducts.execute(q, categoryIdFilter, status, type, brandIdFilter);
        return Response.ok(toCsv(products)).type("text/csv").build();
    }

    @GET
    @Path("/autocomplete")
    public List<AutocompleteResponse> autocomplete(@QueryParam("q") String q, @QueryParam("limit") Integer limit) {
        return autocompleteProducts.execute(q, limit).stream().map(AutocompleteResponse::from).toList();
    }

    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ImportReportResponse importCsv(@RestForm("file") FileUpload file) {
        if (file == null) {
            throw new BadRequestException("error.csv.missing");
        }
        String content;
        try {
            content = Files.readString(file.filePath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BadRequestException("error.csv.unreadable");
        }
        return ImportReportResponse.from(importProducts.execute(content));
    }

    @GET
    @Path("/{id}")
    public ProductResponse getById(@PathParam("id") String id) {
        return toResponse(getProductById.execute(ProductId.of(id)));
    }

    @PUT
    @Path("/{id}")
    public ProductResponse update(@PathParam("id") String id, @Valid UpdateProductRequest request) {
        Product updated = updateProduct.execute(
                ProductId.of(id),
                request.description(),
                request.shortDescription(),
                UnitOfMeasureId.of(request.unitOfMeasureId()),
                CategoryId.of(request.categoryId()),
                toBrandId(request.brandId()),
                toSupplierId(request.defaultSupplierId()));
        return toResponse(updated);
    }

    @PATCH
    @Path("/{id}/status")
    public ProductResponse changeStatus(@PathParam("id") String id, @Valid ChangeStatusRequest request) {
        return toResponse(changeProductStatus.execute(ProductId.of(id), request.status()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        deleteProduct.execute(ProductId.of(id));
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/attributes")
    public List<AttributeResponse> replaceAttributes(@PathParam("id") String id, @Valid ReplaceAttributesRequest request) {
        List<ProductAttribute> raw = request.attributes().stream()
                .map(item -> new ProductAttribute(item.name(), item.value()))
                .toList();
        return replaceProductAttributes.execute(ProductId.of(id), raw).stream().map(AttributeResponse::from).toList();
    }

    @PATCH
    @Path("/{id}/price")
    public ProductResponse changePrice(@PathParam("id") String id, @Valid ChangePriceRequest request) {
        Product updated = changeProductPrice.execute(ProductId.of(id), request.salePrice(), request.costPrice());
        return toResponse(updated);
    }

    @GET
    @Path("/{id}/price-history")
    public List<PriceHistoryResponse> priceHistory(@PathParam("id") String id) {
        return getProductPriceHistory.execute(ProductId.of(id)).stream().map(PriceHistoryResponse::from).toList();
    }

    @POST
    @Path("/{id}/barcodes")
    public Response addBarcode(@PathParam("id") String id, @Valid AddBarcodeRequest request) {
        var barcode = addProductBarcode.execute(ProductId.of(id), request.code(), request.type());
        return Response.status(Response.Status.CREATED).entity(BarcodeResponse.from(barcode)).build();
    }

    @DELETE
    @Path("/{id}/barcodes/{barcodeId}")
    public Response deleteBarcode(@PathParam("id") String id, @PathParam("barcodeId") String barcodeId) {
        deleteProductBarcode.execute(ProductId.of(id), ProductBarcodeId.of(barcodeId));
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/images")
    public Response addImage(@PathParam("id") String id, @Valid AddImageRequest request) {
        var image = addProductImage.execute(ProductId.of(id), request.url());
        return Response.status(Response.Status.CREATED).entity(ImageResponse.from(image)).build();
    }

    @PATCH
    @Path("/{id}/images/{imageId}/primary")
    public Response setPrimaryImage(@PathParam("id") String id, @PathParam("imageId") String imageId) {
        setPrimaryProductImage.execute(ProductId.of(id), ProductImageId.of(imageId));
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}/images/{imageId}")
    public Response deleteImage(@PathParam("id") String id, @PathParam("imageId") String imageId) {
        deleteProductImage.execute(ProductId.of(id), ProductImageId.of(imageId));
        return Response.noContent().build();
    }

    private String toCsv(List<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,description,short_description,type,status,unit_of_measure_code,category_id,brand_id,sale_price,cost_price\r\n");
        for (Product p : products) {
            sb.append(Csv.escape(p.getProductId().toString())).append(',')
                    .append(Csv.escape(p.getDescription())).append(',')
                    .append(Csv.escape(p.getShortDescription())).append(',')
                    .append(Csv.escape(p.getType().name())).append(',')
                    .append(Csv.escape(p.getStatus().name())).append(',')
                    .append(Csv.escape(p.getUnitOfMeasureCode())).append(',')
                    .append(Csv.escape(p.getCategoryId().toString())).append(',')
                    .append(Csv.escape(p.getBrandId() != null ? p.getBrandId().toString() : "")).append(',')
                    .append(Csv.escape(p.getSalePrice().toPlainString())).append(',')
                    .append(Csv.escape(p.getCostPrice().toPlainString()))
                    .append("\r\n");
        }
        return sb.toString();
    }

    private ProductResponse toResponse(Product product) {
        var attributes = getProductAttributes.execute(product.getProductId());
        var barcodes = getProductBarcodes.execute(product.getProductId());
        var images = getProductImages.execute(product.getProductId());
        return ProductResponse.from(product, attributes, barcodes, images);
    }

    private BrandId toBrandId(String raw) {
        return (raw == null || raw.isBlank()) ? null : BrandId.of(raw);
    }

    private SupplierId toSupplierId(String raw) {
        return (raw == null || raw.isBlank()) ? null : SupplierId.of(raw);
    }
}
