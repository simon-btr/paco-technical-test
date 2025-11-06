package technical.test.api.services;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import technical.test.api.mapper.AirportMapper;
import technical.test.api.mapper.FlightMapper;
import technical.test.api.record.AirportRecord;
import technical.test.api.record.FlightRecord;
import technical.test.api.repository.AirportRepository;
import technical.test.api.repository.FlightRepository;
import technical.test.api.representation.FlightRepresentation;
import technical.test.api.representation.SearchCriteriaRepresentation;

@Service
@RequiredArgsConstructor
public class FlightService {
    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final ReactiveMongoTemplate mongoTemplate;
    private final FlightMapper flightMapper;
    private final AirportMapper airportMapper;

    public Flux<FlightRecord> getAllFlights() {
        return flightRepository.findAll();
    }

    public Mono<FlightRecord> save(FlightRecord record) {
        return mongoTemplate.save(record);
    }

    public Flux<FlightRecord> searchByCountries(SearchCriteriaRepresentation criteria) {
        Mono<List<String>> originsMono = criteria.getOriginCountry() != null
                ? airportRepository.findByCountry(criteria.getOriginCountry())
                        .map(AirportRecord::getIata)
                        .collectList()
                : Mono.just(List.of());

        Mono<List<String>> destsMono = criteria.getDestinationCountry() != null
                ? airportRepository.findByCountry(criteria.getDestinationCountry())
                        .map(AirportRecord::getIata)
                        .collectList()
                : Mono.just(List.of());

        return Mono.zip(originsMono, destsMono)
                .flatMapMany(tuple -> {
                    List<String> originIatas = tuple.getT1();
                    List<String> destIatas = tuple.getT2();

                    List<Criteria> filters = new ArrayList<>();
                    if (!originIatas.isEmpty()) {
                        filters.add(Criteria.where("origin").in(originIatas));
                    }
                    if (!destIatas.isEmpty()) {
                        filters.add(Criteria.where("destination").in(destIatas));
                    }

                    Criteria finalCriteria = filters.isEmpty()
                            ? new Criteria()
                            : new Criteria().andOperator(filters);

                    Query query = new Query(finalCriteria);

                    String sortField = normalizeSortField(criteria.getSortBy());
                    Sort.Direction dir = "desc".equalsIgnoreCase(criteria.getSortDir())
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;
                    query.with(Sort.by(dir, sortField));

                    return mongoTemplate.find(query, FlightRecord.class, "flight");
                });
    }

    private String normalizeSortField(String sortBy) {
        if ("origin".equalsIgnoreCase(sortBy))
            return "origin";
        if ("destination".equalsIgnoreCase(sortBy))
            return "destination";
        return "price";
    }

    public Mono<FlightRepresentation> getFlightById(UUID id) {
        return flightRepository.findById(id)
                .switchIfEmpty(
                        Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found for ID " + id)))
                .flatMap(flightRecord -> airportRepository.findAirportRecordByIata(flightRecord.getOrigin())
                        .zipWith(airportRepository.findAirportRecordByIata(flightRecord.getDestination()))
                        .map(tuple -> {
                            AirportRecord origin = tuple.getT1();
                            AirportRecord destination = tuple.getT2();
                            FlightRepresentation repr = flightMapper.convert(flightRecord);
                            repr.setOrigin(airportMapper.convert(origin));
                            repr.setDestination(airportMapper.convert(destination));
                            return repr;
                        }));
    }
}
