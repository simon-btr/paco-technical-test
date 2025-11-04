package technical.test.api.facade;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import technical.test.api.mapper.AirportMapper;
import technical.test.api.mapper.FlightMapper;
import technical.test.api.record.AirportRecord;
import technical.test.api.record.FlightRecord;
import technical.test.api.representation.FlightRepresentation;
import technical.test.api.services.AirportService;
import technical.test.api.services.FlightService;

@Component
@RequiredArgsConstructor
public class FlightFacade {
    private final FlightService flightService;
    private final AirportService airportService;
    private final FlightMapper flightMapper;
    private final AirportMapper airportMapper;

    public Flux<FlightRepresentation> getAllFlights() {
        return flightService.getAllFlights()
                .flatMap(flightRecord -> airportService.findByIataCode(flightRecord.getOrigin())
                        .zipWith(airportService.findByIataCode(flightRecord.getDestination()))
                        .flatMap(tuple -> {
                            AirportRecord origin = tuple.getT1();
                            AirportRecord destination = tuple.getT2();
                            FlightRepresentation flightRepresentation = this.flightMapper.convert(flightRecord);
                            flightRepresentation.setOrigin(this.airportMapper.convert(origin));
                            flightRepresentation.setDestination(this.airportMapper.convert(destination));
                            return Mono.just(flightRepresentation);
                        }));
    }

    public Mono<FlightRepresentation> createFlight(FlightRepresentation dto) {
        final String originIata = dto.getOrigin().getIata();
        final String destIata = dto.getDestination().getIata();

        return Mono.zip(
                airportService.findByIataCode(originIata),
                airportService.findByIataCode(destIata))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unknown IATA code for origin and/or destination")))
                .flatMap(tuple -> {
                    FlightRecord toSave = flightMapper.convert(dto);
                    if (toSave.getId() == null) {
                        toSave.setId(UUID.randomUUID());
                    }
                    return flightService.save(toSave)
                            .map(saved -> {
                                FlightRepresentation rep = flightMapper.convert(saved);
                                rep.setOrigin(airportMapper.convert(tuple.getT1()));
                                rep.setDestination(airportMapper.convert(tuple.getT2()));
                                return rep;
                            });
                });
    }

}
