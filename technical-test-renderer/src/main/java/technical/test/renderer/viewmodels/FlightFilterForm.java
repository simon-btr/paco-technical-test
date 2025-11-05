package technical.test.renderer.viewmodels;

import lombok.Data;

@Data
public class FlightFilterForm {
    private String originCountry;
    private String destinationCountry;
    private String sortBy = "price";
    private String sortDir = "asc";
}