package technical.test.renderer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import technical.test.renderer.facades.FlightFacade;
import technical.test.renderer.viewmodels.AirportViewModel;
import technical.test.renderer.viewmodels.CreateFlightForm;
import technical.test.renderer.viewmodels.FlightFilterForm;
import technical.test.renderer.viewmodels.FlightViewModel;

@Controller
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class TechnicalController {

    @Autowired
    private FlightFacade flightFacade;

    @GetMapping
    public Mono<String> getMarketPlaceReturnCouponPage(
            @ModelAttribute("filterForm") FlightFilterForm filterForm,
            Model model) {
        model.addAttribute("filterForm", filterForm);
        model.addAttribute("flights", this.flightFacade.getFlights(
            filterForm.getOriginCountry(),
            filterForm.getDestinationCountry(),
            filterForm.getSortBy(),
            filterForm.getSortDir()
        ));
        return Mono.just("pages/index");
    }

    @GetMapping("/admin/flight")
    public String adminForm(Model model) {
        model.addAttribute("createFlightForm", new CreateFlightForm());
        return "pages/admin";
    }

    @PostMapping("/admin/flight")
    public Mono<String> adminCreate(
            @ModelAttribute("createFlightForm") CreateFlightForm form,
            Model model) {
        try {
            double parsedPrice = Double.parseDouble(form.getPrice().trim().replace(',', '.'));
            String oIata = form.getOriginIata().trim().toUpperCase();
            String dIata = form.getDestinationIata().trim().toUpperCase();

            AirportViewModel origin = new AirportViewModel();
            origin.setIata(oIata);
            AirportViewModel destination = new AirportViewModel();
            destination.setIata(dIata);

            FlightViewModel vm = new FlightViewModel();
            vm.setDeparture(form.getDeparture());
            vm.setArrival(form.getArrival());
            vm.setPrice(parsedPrice);
            vm.setImage(form.getImage());
            vm.setOrigin(origin);
            vm.setDestination(destination);

            return flightFacade.createFlight(vm)
                    .map(created -> {
                        model.addAttribute("created", created);
                        model.addAttribute("success", true);
                        model.addAttribute("createFlightForm", new CreateFlightForm());
                        return "pages/admin";
                    })
                    .onErrorResume(ex -> {
                        model.addAttribute("error", "Creation failure : " + ex.getMessage());
                        return Mono.just("pages/admin");
                    });
        } catch (Exception e) {
            model.addAttribute("error", "Input failure : " + e.getMessage());
            return Mono.just("pages/admin");
        }
    }
}
