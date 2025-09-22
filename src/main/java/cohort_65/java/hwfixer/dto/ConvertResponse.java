package cohort_65.java.hwfixer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvertResponse {
    private String from;
    private String to;
    private BigDecimal rate;
    private BigDecimal result;
    private String date;
}
