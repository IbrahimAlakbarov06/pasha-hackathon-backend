package com.bravo.brain.service;

import com.bravo.brain.domain.entity.*;
import com.bravo.brain.domain.repository.*;
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
    private final DepartmentRepository departmentRepo;
    private final NotificationService notificationService;

    // ── YENİ MƏHSUL YARAT ─────────────────────────────────
    public ProductDto.ProductResponse createProduct(ProductDto.CreateRequest req) {
        Department department = departmentRepo.findById(req.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Şöbə tapılmadı"));

        if (req.getBarcode() != null && productRepo.existsByBarcode(req.getBarcode()))
            throw new RuntimeException("Bu barkod artıq mövcuddur");

        Product product = Product.builder()
                .name(req.getName())
                .barcode(req.getBarcode())
                .category(req.getCategory())
                .department(department)
                .imageBase64(req.getImageBase64())
                .minimumStock(req.getMinimumStock())
                .unit(req.getUnit())
                .costPrice(req.getCostPrice())
                .sellPrice(req.getSellPrice())
                .active(true)
                .build();

        Product saved = productRepo.save(product);

        // Barcode yoxdursa avtomatik yarat
        if (saved.getBarcode() == null) {
            saved.setBarcode(generateBarcode(saved.getCategory(), saved.getId()));
            saved = productRepo.save(saved);
        }

        return toResponse(saved);
    }

    // ── BARKODLA MƏHSUL GƏTİR — scan endpoint ─────────────
    public ProductDto.ProductResponse getByBarcode(String barcode) {
        Product product = productRepo.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Məhsul tapılmadı: " + barcode));
        return toResponse(product);
    }

    // ── BARCODE LABEL — çap üçün ──────────────────────────
    public ProductDto.BarcodeLabelResponse getBarcodeLabel(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Məhsul tapılmadı"));

        String barcodeImage = generateBarcodeImage(product.getBarcode());

        return new ProductDto.BarcodeLabelResponse(
                product.getId(),
                product.getName(),
                product.getBarcode(),
                barcodeImage,     // artıq null deyil
                product.getCategory().getLabel(),
                product.getDepartment().getName(),
                product.getDepartment().getStoreName(),
                product.getSellPrice()
        );
    }

    // ── ŞÖBƏ ÜZRƏ MƏHSULLAR ───────────────────────────────
    public List<ProductDto.ProductResponse> getByDepartmentId(Long departmentId) {
        return productRepo.findByDepartmentId(departmentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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

    // ── SATIŞ ─────────────────────────────────────────────
    @Transactional
    public String processSale(ProductDto.SaleRequest req) {
        Product product = productRepo.findByBarcode(req.getBarcode())
                .orElseThrow(() -> new RuntimeException("Məhsul tapılmadı: " + req.getBarcode()));

        List<ProductBatch> batches = batchRepo.findActiveByProductOrderByDeliveryDate(product.getId());

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

            Sale sale = Sale.builder()
                    .product(product)
                    .batch(batch)
                    .quantity(fromBatch)
                    .sellPrice(product.getSellPrice() != null ? product.getSellPrice() : 0.0)
                    .returned(false)
                    .build();
            saleRepo.save(sale);
        }

        if (remaining > 0)
            throw new RuntimeException("Stokda kifayət qədər məhsul yoxdur. Çatışmayan: " + remaining);

        // Low stock yoxlama
        if (product.getMinimumStock() != null) {
            List<ProductBatch> remaining2 = batchRepo.findActiveByProductOrderByDeliveryDate(product.getId());
            double totalLeft = remaining2.stream().mapToDouble(ProductBatch::getQuantity).sum();
            if (totalLeft <= product.getMinimumStock()) {
                notificationService.sendLowStockAlert(product, totalLeft);
            }
        }

        return product.getName() + " — satış qeyd edildi";
    }

    // ── QAYITMA ───────────────────────────────────────────
    @Transactional
    public String processReturn(ProductDto.ReturnRequest req) {
        Sale sale = saleRepo.findById(req.getSaleId())
                .orElseThrow(() -> new RuntimeException("Satış tapılmadı"));

        if (sale.isReturned())
            throw new RuntimeException("Bu satış artıq qaytarılıb");

        sale.setReturned(true);
        sale.setReturnedAt(LocalDateTime.now());
        saleRepo.save(sale);

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
                .findByDepartment_StoreNameAndDepartment_Name(storeName, departmentName);

        return products.stream().map(p -> {
            List<ProductBatch> batches = batchRepo.findActiveByProductOrderByDeliveryDate(p.getId());
            double totalStock = batches.stream().mapToDouble(ProductBatch::getQuantity).sum();
            var nearestRemoval = batches.stream()
                    .map(ProductBatch::getRemovalDate)
                    .min(java.time.LocalDate::compareTo)
                    .orElse(null);

            return new ProductDto.StockResponse(
                    p.getId(), p.getName(), p.getBarcode(),
                    p.getCategory().getLabel(),
                    p.getDepartment().getId(),
                    p.getDepartment().getName(),
                    p.getDepartment().getStoreName(),
                    totalStock, batches.size(), nearestRemoval
            );
        }).collect(Collectors.toList());
    }

    // ── BARCODE GENERATE ──────────────────────────────────
    // Format: BRV-{categoryCode}-{productId}-{random4}
    private String generateBarcode(com.bravo.brain.model.enums.ProductCategory category, Long productId) {
        String categoryCode = switch (category) {
            case FRUIT         -> "FRT";
            case VEGETABLE     -> "VEG";
            case MEAT          -> "MET";
            case DAIRY         -> "DRY";
            case BREAD         -> "BRD";
            case EGG           -> "EGG";
            case CONFECTIONERY -> "CNF";
            case BEVERAGE      -> "BEV";
            case OTHER         -> "OTH";
        };
        String random = String.format("%04d", new Random().nextInt(9000) + 1000);
        return String.format("BRV-%s-%03d-%s", categoryCode, productId, random);
    }

    // ── BATCH CODE GENERATE ───────────────────────────────
    private String generateBatchCode(Long productId) {
        String date = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("MMdd"));
        String rand = String.format("%03d", new Random().nextInt(999) + 1);
        return String.format("FG-%d-%s-%s", productId, date, rand);
    }

    private String generateBarcodeImage(String barcodeText) {
        try {
            com.google.zxing.Writer writer = new com.google.zxing.oned.Code128Writer();
            com.google.zxing.common.BitMatrix bitMatrix =
                    writer.encode(barcodeText, com.google.zxing.BarcodeFormat.CODE_128, 400, 100);

            java.awt.image.BufferedImage image =
                    com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix);

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", baos);

            return "data:image/png;base64," +
                    java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Barcode şəkli yaradılmadı: " + e.getMessage());
        }
    }

    // ── ENTITY → DTO ──────────────────────────────────────
    private ProductDto.ProductResponse toResponse(Product p) {
        return new ProductDto.ProductResponse(
                p.getId(),
                p.getName(),
                p.getBarcode(),
                p.getCategory().getLabel(),
                p.getDepartment().getId(),
                p.getDepartment().getName(),
                p.getDepartment().getStoreName(),
                p.getImageBase64(),
                p.getMinimumStock(),
                p.getUnit(),
                p.getCostPrice(),
                p.getSellPrice(),
                p.isActive(),
                p.getCreatedAt()
        );
    }
}