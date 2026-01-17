package at.fhv.simplyevents.billing.infrastructure.hash;

import at.fhv.simplyevents.billing.domain.exception.InvoiceException;
import at.fhv.simplyevents.billing.domain.model.Invoice;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;


@Service
public class InvoiceHashGenerator {

    public String generateHash(Invoice invoice) {
        try {
            String snapshot = buildSnapshot(invoice);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(snapshot.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new InvoiceException("SHA-256 algorithm not available", e);
        }
    }

    private String buildSnapshot(Invoice invoice) {
        StringBuilder sb = new StringBuilder();

        // Invoice metadata
        if (invoice.getInvoiceNumber() != null) {
            sb.append("invoiceNumber=").append(invoice.getInvoiceNumber().format()).append("|");
        }
        sb.append("createdAt=").append(invoice.getCreatedAt()).append("|");
        sb.append("total=").append(invoice.getTotal().getAmount()).append("|");

        // Lines
        String linesStr = invoice.getLines().stream()
            .map(line -> line.getId() + ":" + line.getDescription() + ":" +
                        line.getQuantity() + ":" + line.getUnitPrice().getAmount())
            .collect(Collectors.joining(";"));
        sb.append("lines=").append(linesStr).append("|");

        // Shares
        String sharesStr = invoice.getShares().stream()
            .map(share -> share.getId() + ":" + share.getUserId() + ":" +
                         share.getAllocatedAmount().getAmount())
            .collect(Collectors.joining(";"));
        sb.append("shares=").append(sharesStr);

        return sb.toString();
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
