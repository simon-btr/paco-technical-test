package technical.test.renderer.viewmodels;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class FlightViewModel {
    private UUID id;
    private LocalDateTime departure;
    private LocalDateTime arrival;
    private double price;
    private String image;
    private AirportViewModel origin;
    private AirportViewModel destination;
}
