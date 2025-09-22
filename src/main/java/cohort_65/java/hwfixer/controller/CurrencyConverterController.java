package cohort_65.java.hwfixer.controller;

import cohort_65.java.hwfixer.dto.AmountRequest;
import cohort_65.java.hwfixer.dto.ConvertResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CurrencyConverterController {


    private final RestTemplate restTemplate = new RestTemplate();


    private final String apiKey = "cad52fc4b871b526570188f69abe542d";

    @PostMapping(value = "/convert-usd-eur",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConvertResponse> convertUsdToEur(@RequestBody AmountRequest req) {
        return ResponseEntity.ok(convertViaLatest("USD", "EUR", req.getAmount()));
    }

    @PostMapping(value = "/convert-eur-usd",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConvertResponse> convertEurToUsd(@RequestBody AmountRequest req) {
        return ResponseEntity.ok(convertViaLatest("EUR", "USD", req.getAmount()));
    }


    @GetMapping("/convert-usd-eur")
    public ResponseEntity<ConvertResponse> convertUsdToEurGet(@RequestParam BigDecimal amount) {
        return ResponseEntity.ok(convertViaLatest("USD", "EUR", amount));
    }

    @GetMapping("/convert-eur-usd")
    public ResponseEntity<ConvertResponse> convertEurToUsdGet(@RequestParam BigDecimal amount) {
        return ResponseEntity.ok(convertViaLatest("EUR", "USD", amount));
    }


    private ConvertResponse convertViaLatest(String from, String to, BigDecimal amount) {
        try {
            String url = String.format(
                    "http://data.fixer.io/api/latest?access_key=%s&symbols=USD,EUR",
                    apiKey
            );

            Map<?, ?> json = restTemplate.getForObject(url, Map.class);
            if (json == null) {
                throw new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_GATEWAY,
                        "Fixer returned empty response");
            }

            Object success = json.get("success");
            if (Boolean.FALSE.equals(success)) {
                Map<?, ?> err = (Map<?, ?>) json.get("error");
                Object code = err != null ? err.get("code") : "unknown";
                Object type = err != null ? err.get("type") : "unknown";
                throw new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_GATEWAY,
                        "Fixer error: code=" + code + ", type=" + type);
            }

            Map<?, ?> rates = (Map<?, ?>) json.get("rates");
            if (rates == null || !rates.containsKey("USD")) {
                throw new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_GATEWAY,
                        "Fixer response has no USD rate");
            }

            BigDecimal usdPerEur = toBigDecimal(rates.get("USD")); // USD за 1 EUR
            if (usdPerEur == null || usdPerEur.compareTo(BigDecimal.ZERO) == 0) {
                throw new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_GATEWAY,
                        "Invalid USD rate from Fixer");
            }

            BigDecimal rate;
            BigDecimal result;

            if ("EUR".equalsIgnoreCase(from) && "USD".equalsIgnoreCase(to)) {
                rate = usdPerEur;
                result = amount.multiply(rate);
            } else if ("USD".equalsIgnoreCase(from) && "EUR".equalsIgnoreCase(to)) {
                rate = BigDecimal.ONE.divide(usdPerEur, 10, RoundingMode.HALF_UP); // 1 USD = 1/usdPerEur EUR
                result = amount.multiply(rate);
            } else {
                throw new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Unsupported pair: " + from + "->" + to);
            }

            String date = String.valueOf(json.get("date"));
            rate = rate.setScale(6, RoundingMode.HALF_UP);
            result = result.setScale(2, RoundingMode.HALF_UP);

            return new ConvertResponse(from.toUpperCase(), to.toUpperCase(), rate, result, date);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "Fixer call failed: " + ex.getMessage(), ex);
        }
    }

    private BigDecimal toBigDecimal(Object v) {
        return (v == null) ? null : new BigDecimal(String.valueOf(v));
    }
}
