package technical.test.api.representation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchCriteriaRepresentation {
    private String originCountry;
    private String destinationCountry;
    private String sortBy;
    private String sortDir;
}