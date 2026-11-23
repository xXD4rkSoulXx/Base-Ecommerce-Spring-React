package com.d4rksoul.base.ecommerce.spring.react.pdf;

import com.d4rksoul.base.ecommerce.spring.react.config.ShippingOriginProperties;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ShippingSlipPdfGenerator {

    private static final Font TITLE_FONT = FontFactory
        .getFont(FontFactory.HELVETICA_BOLD, 20);
    private static final Font HEADING_FONT = FontFactory
        .getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font BODY_FONT = FontFactory
        .getFont(FontFactory.HELVETICA, 11);
    private static final Font DESTINATION_FONT = FontFactory
        .getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm 'UTC'")
        .withZone(ZoneOffset.UTC);

    private final ShippingOriginProperties origin;

    public byte[] generateShippingSlip(Order order, List<OrderShipment> shipments) {
        List<Line> lines = order.getOrderItems().stream()
            .map(item -> new Line(item, item.getQuantity()))
            .toList();

        return render(document -> {
            addHeader(
                document,
                "SHIPPING SLIP",
                "Order #" + order.getId() + " · " + DATE_FORMAT.format(order.getPaidAt())
            );
            addAddresses(
                document,
                new Party("FROM", originLines(), BODY_FONT),
                new Party("SHIP TO", recipientLines(order), DESTINATION_FONT)
            );
            addShipping(document, order, shipments);
            addItems(document, lines);
        });
    }

    public byte[] generateReturnSlip(OrderReturn orderReturn, List<OrderReturnItem> returnItems) {
        Order order = orderReturn.getOrder();
        List<Line> lines = returnItems.stream()
            .map(returnItem ->
                new Line(returnItem.getOrderItem(), returnItem.getQuantity())
            )
            .toList();

        return render(document -> {
            addHeader(
                document,
                "RETURN SLIP",
                "Return #" + orderReturn.getId() + " · Order #" + order.getId()
            );
            addAddresses(
                document,
                new Party("FROM", recipientLines(order), BODY_FONT),
                new Party("RETURN TO", originLines(), DESTINATION_FONT)
            );
            addReason(document, orderReturn);
            addItems(document, lines);
        });
    }

    private byte[] render(DocumentWriter writer) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (
            Document document = new Document(
                PageSize.A4, 40, 40, 40, 40
            )
        ) {
            PdfWriter.getInstance(document, output);
            document.open();
            writer.write(document);
        } catch (DocumentException exception) {
            throw new IllegalStateException("Could not generate the PDF.", exception);
        }

        return output.toByteArray();
    }

    private void addHeader(
        Document document, String title, String subtitle
    ) throws DocumentException {
        document.add(new Paragraph(title, TITLE_FONT));
        document.add(new Paragraph(subtitle, BODY_FONT));
    }

    private void addAddresses(
        Document document, Party left, Party right
    ) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(18);

        table.addCell(partyCell(left));
        table.addCell(partyCell(right));

        document.add(table);
    }

    private void addShipping(
        Document document, Order order, List<OrderShipment> shipments
    ) throws DocumentException {
        Paragraph carrier = new Paragraph(
            "Carrier: " + order.getShippingCarrier() + " " + order.getShippingService(), BODY_FONT
        );
        carrier.setSpacingBefore(14);
        document.add(carrier);

        String tracking = shipments.stream()
            .map(OrderShipment::getTrackingNumber)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(", "));
        if (!tracking.isEmpty()) {
            document.add(new Paragraph("Tracking: " + tracking, BODY_FONT));
        }
    }

    private void addReason(Document document, OrderReturn orderReturn) throws DocumentException {
        Paragraph reason = new Paragraph(
            "Reason: " + orderReturn.getReason().name().replace('_', ' '), BODY_FONT
        );
        reason.setSpacingBefore(14);
        document.add(reason);
    }

    private void addItems(Document document, List<Line> lines) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.2f, 5f, 1.4f, 2f, 1f});
        table.setSpacingBefore(18);

        for (String heading : List.of("REF", "PRODUCT", "SIZE", "COLOR", "QTY")) {
            table.addCell(new PdfPCell(new Paragraph(heading, HEADING_FONT)));
        }

        for (Line line : lines) {
            OrderItem item = line.item();

            table.addCell(new PdfPCell(new Paragraph(item.getProductSku().getProduct().getRef(), BODY_FONT)));
            table.addCell(new PdfPCell(new Paragraph(item.getProductName(), BODY_FONT)));
            table.addCell(new PdfPCell(new Paragraph(item.getSize().getLabel(), BODY_FONT)));
            table.addCell(new PdfPCell(new Paragraph(item.getColor().name(), BODY_FONT)));
            PdfPCell quantity = new PdfPCell(new Paragraph(String.valueOf(line.quantity()), BODY_FONT));
            quantity.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(quantity);
        }

        document.add(table);

        int units = lines.stream().mapToInt(Line::quantity).sum();
        double kilograms = lines.stream()
            .mapToDouble(line -> line.item().getWeightGrams() * line.quantity())
            .sum() / 1000;

        Paragraph total = new Paragraph(
            String.format(Locale.ROOT, "Total: %d units · %.2f kg", units, kilograms), BODY_FONT
        );
        total.setSpacingBefore(10);
        document.add(total);
    }

    private PdfPCell partyCell(Party party) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(10);
        cell.addElement(new Paragraph(party.heading(), HEADING_FONT));

        for (String line : party.lines()) {
            cell.addElement(new Paragraph(line, party.font()));
        }

        return cell;
    }

    private List<String> originLines() {
        return compact(
            origin.name(),
            origin.addressLine1(),
            origin.postalCode() + " " + origin.city(),
            origin.state(),
            origin.country(),
            origin.phone()
        );
    }

    private List<String> recipientLines(Order order) {
        return compact(
            order.getRecipientName(),
            order.getAddressLine1(),
            order.getAddressLine2(),
            order.getPostalCode() + " " + order.getCity(),
            order.getState(),
            order.getCountry(),
            order.getPhone()
        );
    }

    private List<String> compact(String... lines) {
        return Stream.of(lines)
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .toList();
    }

    @FunctionalInterface
    private interface DocumentWriter {

        void write(Document document) throws DocumentException;
    }

    private record Party(
        String heading,

        List<String> lines,

        Font font
    ) {}

    private record Line(
        OrderItem item,

        int quantity
    ) {}
}
