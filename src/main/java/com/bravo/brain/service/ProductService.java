package com.bravo.brain.service;

import com.bravo.brain.domain.entity.Product;
import com.bravo.brain.domain.entity.ProductBatch;
import com.bravo.brain.domain.entity.Sale;
import com.bravo.brain.domain.repository.ProductBatchRepository;
import com.bravo.brain.domain.repository.ProductRepository;
import com.bravo.brain.domain.repository.SaleRepository;
import com.bravo.brain.domain.repository.WasteLogRepository;
import com.bravo.brain.model.dto.ProductDto;
import com.bravo.brain.model.enums.BatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final ProductBatchRepository batchRepo;
    private final SaleRepository saleRepo;
    private final WasteLogRepository wasteRepo;

    // ── YENİ MƏHSUL YARAT ─────────────────────────────────
    public ProductDto.ProductResponse createProduct(ProductDto.CreateRequest req) {
        if (req.getBarcode() != null && productRepo.existsByBarcode(req.getBarcode()))
            throw new RuntimeException("Bu barkod artıq mövcuddur");

        Product product = Product.builder()
                .name(req.getName())
                .barcode(req.getBarcode())
                .category(req.getCategory())
                .departmentName(req.getDepartmentName())
                .storeName(req.getStoreName())
                .unit(req.getUnit())
                .costPrice(req.getCostPrice())
                .sellPrice(req.getSellPrice())
                .active(true)
                .build();

        return toProductResponse(productRepo.save(product));
    }

    // ── YENİ BATCH — MAL QƏBULU ───────────────────────────
    public String addBatch(ProductDto.AddBatchRequest req) {
        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Məhsul tapılmadı"));

        if (req.getRemovalDate().isBefore(req.getDeliveryDate()))
            throw new RuntimeException("Çıxarılma tarixi gəliş tarixindən əvvəl ola bilməz");

        String batchCode = generateBatchCode(product.getId());

        ProductBatch batch = ProductBatch.builder()
                .product(product)
                .batchCode(batchCode)
                .quantity(req.getQuantity())
                .originalQuantity(req.getQuantity())
                .deliveryDate(req.getDeliveryDate())
                .removalDate(req.getRemovalDate())
                .status(BatchStatus.ACTIVE)
                .addedByUserId(req.getAddedByUserId())
                .build();

        batchRepo.save(batch);
        return batchCode;
    }

    // ── SATIŞ — KASSIR BARKOD OXUDUR ──────────────────────
    @Transactional
    public String processSale(ProductDto.SaleRequest req) {
        Product product = productRepo.findByBarcode(req.getBarcode())
                .orElseThrow(() -> new RuntimeException("Məhsul tapılmadı: " + req.getBarcode()));

        // FIFO — ən köhnə batch-dan çıx
        List<ProductBatch> batches = batchRepo
                .findActiveByProductOrderByDeliveryDate(product.getId());

        if (batches.isEmpty())
            throw new RuntimeException("Stokda məhsul qalmayıb: " + product.getName());

        double remaining = req.getQuantity();

        for (ProductBatch batch : batches) {
            if (remaining <= 0) break;

            double fromBatch = Math.min(batch.getQuantity(), remaining);
            batch.setQuantity(batch.getQuantity() - fromBatch);
            remaining -= fromBatch;

            if (batch.getQuantity() == 0)
                batch.setStatus(BatchStatus.SOLD_OUT);

            batchRepo.save(batch);

            // Satış qeyd et
            Sale sale = Sale.builder()
                    .product(product)
                    .batch(batch)
                    .quantity(fromBatch)
                    .sellPrice(product.getSellPrice() != null ? product.getSellPrice() : 0.0)
                    .returned(false)
                    .storeName(product.getStoreName())
                    .departmentName(product.getDepartmentName())
                    .build();
            saleRepo.save(sale);
        }

        if (remaining > 0)
            throw new RuntimeException("Stokda kifayət qədər məhsul yoxdur. Çatışmayan: " + remaining);

        return product.getName() + " — satış qeyd edildi";
    }

    // ── MƏHSUL QAYITMASI ──────────────────────────────────
    @Transactional
    public String processReturn(ProductDto.ReturnRequest req) {
        Sale sale = saleRepo.findById(req.getSaleId())
                .orElseThrow(() -> new RuntimeException("Satış tapılmadı"));

        if (sale.isReturned())
            throw new RuntimeException("Bu satış artıq qaytarılıb");

        sale.setReturned(true);
        sale.setReturnedAt(LocalDateTime.now());
        saleRepo.save(sale);

        // Batch-a geri qoy
        if (sale.getBatch() != null) {
            ProductBatch batch = sale.getBatch();
            batch.setQuantity(batch.getQuantity() + sale.getQuantity());
            if (batch.getStatus() == BatchStatus.SOLD_OUT)
                batch.setStatus(BatchStatus.ACTIVE);
            batchRepo.save(batch);
        }

        return "Məhsul qaytarıldı və stoka əlavə edildi";
    }

    // ── STOK GÖRÜNÜŞÜ ─────────────────────────────────────
    public List<ProductDto.StockResponse> getStock(String storeName, String departmentName) {
        List<Product> products = productRepo
                .findByStoreNameAndDepartmentName(storeName, departmentName);

        return products.stream().map(p -> {
            List<ProductBatch> batches = batchRepo
                    .findActiveByProductOrderByDeliveryDate(p.getId());

            double totalStock = batches.stream()
                    .mapToDouble(ProductBatch::getQuantity).sum();

            var nearestRemoval = batches.stream()
                    .map(ProductBatch::getRemovalDate)
                    .min(java.time.LocalDate::compareTo)
                    .orElse(null);

            return new ProductDto.StockResponse(
                    p.getId(), p.getName(), p.getBarcode(),
                    p.getCategory().getLabel(), p.getDepartmentName(),
                    totalStock, batches.size(), nearestRemoval
            );
        }).collect(Collectors.toList());
    }

    // ── BATCH CODE GENERASIYA ─────────────────────────────
    // Format: FG-{productId}-{tarix}-{random}
    private String generateBatchCode(Long productId) {
        String date = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("MMdd"));
        String rand = String.format("%03d", new Random().nextInt(999) + 1);
        return String.format("FG-%d-%s-%s", productId, date, rand);
    }

    // ── ENTITY → DTO ──────────────────────────────────────
    private ProductDto.ProductResponse toProductResponse(Product p) {
        return new ProductDto.ProductResponse(
                p.getId(), p.getName(), p.getBarcode(),
                p.getCategory().getLabel(), p.getDepartmentName(),
                p.getStoreName(), p.getUnit(),
                p.getCostPrice(), p.getSellPrice(),
                p.isActive(), p.getCreatedAt()
        );
    }
}