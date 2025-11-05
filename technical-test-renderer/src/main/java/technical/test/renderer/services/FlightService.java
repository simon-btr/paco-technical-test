package technical.test.renderer.services;

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

    public Flux<FlightViewModel> getFlights() {
        return this.technicalApiClient.getFlights();
    }

    public Mono<FlightViewModel> createFlight(FlightViewModel flightViewModel) {
        return this.technicalApiClient.createFlight(flightViewModel);
    }
}
