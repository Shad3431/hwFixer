package cohort_65.java.hwfixer.controller;

import cohort_65.java.hwfixer.dto.AmountRequest;
import cohort_65.java.hwfixer.dto.ConvertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CurrencyConverterController {

    private final RestTemplate restTemplate;

    private String apiKey = "cad52fc4b871b526570188f69abe542d";

    @PostMapping(value = "/convert-usd-eur", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConvertResponse> convertUsdToEur(@RequestBody AmountRequest req) {
        return ResponseEntity.ok(callFixerConvert("USD", "EUR", req.getAmount()));
    }

    @PostMapping(value = "/convert-eur-usd", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConvertResponse> convertEurToUsd(@RequestBody AmountRequest req) {
        return ResponseEntity.ok(callFixerConvert("EUR", "USD", req.getAmount()));
    }

    private ConvertResponse callFixerConvert(String from, String to, BigDecimal amount) {
        String url = String.format(
                "https://data.fixer.io/api/convert?access_key=%s&from=%s&to=%s&amount=%s",
                apiKey, from, to, amount
        );
        Map<?,?> json = restTemplate.getForObject(url, Map.class);

        Map<?,?> info = json != null ? (Map<?,?>) json.get("info") : null;
        BigDecimal rate = info != null && info.get("rate") != null
                ? new BigDecimal(String.valueOf(info.get("rate")))
                : null;
        BigDecimal result = json != null && json.get("result") != null
                ? new BigDecimal(String.valueOf(json.get("result")))
                : null;
        String date = json != null ? (String) json.get("date") : null;

        return new ConvertResponse(from, to, rate, result, date);
    }
}
