package technical.test.renderer.services;

import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import technical.test.renderer.clients.TechnicalApiClient;
import technical.test.renderer.viewmodels.FlightViewModel;

@Service
public class FlightService {
    private final TechnicalApiClient technicalApiClient;

    public FlightService(TechnicalApiClient technicalApiClient) {
        this.technicalApiClient = technicalApiClient;
    }

    public Flux<FlightViewModel> getFlights(String originCountry,
                                            String destinationCountry,
                                            String sortBy,
                                            String sortDir) {
        return technicalApiClient.getFlights(originCountry, destinationCountry, sortBy, sortDir);
    }

    public Mono<FlightViewModel> createFlight(FlightViewModel flightViewModel) {
        return this.technicalApiClient.createFlight(flightViewModel);
    }

    public Mono<FlightViewModel> getFlightById(UUID id) {
        return technicalApiClient.getFlightById(id);
    }
}
