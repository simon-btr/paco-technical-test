package technical.test.renderer.clients;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import technical.test.renderer.properties.TechnicalApiProperties;
import technical.test.renderer.viewmodels.FlightViewModel;

@Component
@Slf4j
public class TechnicalApiClient {

    private final TechnicalApiProperties technicalApiProperties;
    private final WebClient webClient;

    public TechnicalApiClient(TechnicalApiProperties technicalApiProperties, final WebClient.Builder webClientBuilder) {
        this.technicalApiProperties = technicalApiProperties;
        this.webClient = webClientBuilder.baseUrl(technicalApiProperties.getUrl()).build();
    }

    public Flux<FlightViewModel> getFlights(String originCountry,
            String destinationCountry,
            String sortBy,
            String sortDir) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(technicalApiProperties.getFlightPath())
                        .queryParamIfPresent("originCountry", Optional.ofNullable(originCountry))
                        .queryParamIfPresent("destinationCountry", Optional.ofNullable(destinationCountry))
                        .queryParam("sortBy", sortBy)
                        .queryParam("sortDir", sortDir)
                        .build())
                .retrieve()
                .bodyToFlux(FlightViewModel.class);
    }

    public Mono<FlightViewModel> createFlight(FlightViewModel payload) {
        return webClient
                .post()
                .uri(technicalApiProperties.getUrl() + technicalApiProperties.getFlightPath())
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(FlightViewModel.class);
    }

    public Mono<FlightViewModel> getFlightById(UUID id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(technicalApiProperties.getFlightPath() + "/{id}")
                        .build(id))
                .retrieve()
                .bodyToMono(FlightViewModel.class);
    }
}
