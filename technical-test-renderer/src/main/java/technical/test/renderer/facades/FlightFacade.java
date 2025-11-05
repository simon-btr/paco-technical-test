package technical.test.renderer.facades;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import technical.test.renderer.services.FlightService;
import technical.test.renderer.viewmodels.FlightViewModel;

@Component
public class FlightFacade {

    private final FlightService flightService;

    public FlightFacade(FlightService flightService) {
        this.flightService = flightService;
    }

    public Flux<FlightViewModel> getFlights(String originCountry,
                                            String destinationCountry,
                                            String sortBy,
                                            String sortDir) {
        return flightService.getFlights(originCountry, destinationCountry, sortBy, sortDir);
    }

    public Mono<FlightViewModel> createFlight(FlightViewModel flightViewModel) {
        return this.flightService.createFlight(flightViewModel);
    }
}
