package ke.co.machakosfa.mims.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import ke.co.machakosfa.mims.exception.DuplicatePriceException;
import ke.co.machakosfa.mims.exception.InvalidPriceDateException;
import ke.co.machakosfa.mims.model.Crop;
import ke.co.machakosfa.mims.model.Market;
import ke.co.machakosfa.mims.model.PriceRecord;
import ke.co.machakosfa.mims.service.BroadcastService;
import ke.co.machakosfa.mims.service.CropService;
import ke.co.machakosfa.mims.service.FarmerService;
import ke.co.machakosfa.mims.service.MarketService;
import ke.co.machakosfa.mims.service.PriceService;

/**
 * Administrator pages. Every form submission redirects back to its page (Post/Redirect/Get),
 * so refreshing the browser never resubmits a form, and the outcome is shown as a message.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final String LINKED_RECORDS_MESSAGE =
            "This item is linked to other records (for example, prices or farmers) and cannot be removed.";

    private final CropService cropService;
    private final MarketService marketService;
    private final FarmerService farmerService;
    private final PriceService priceService;
    private final BroadcastService broadcastService;

    public AdminController(CropService cropService, MarketService marketService, FarmerService farmerService,
                            PriceService priceService, BroadcastService broadcastService) {
        this.cropService = cropService;
        this.marketService = marketService;
        this.farmerService = farmerService;
        this.priceService = priceService;
        this.broadcastService = broadcastService;
    }

    private String requireAdmin(HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login?role=admin";
        }
        return null;
    }

    private void addSessionAttrs(Model model, HttpSession session) {
        model.addAttribute("name", session.getAttribute("name"));
        model.addAttribute("staffRole", session.getAttribute("staffRole"));
    }

    private void flashSuccess(RedirectAttributes ra, String message) {
        ra.addFlashAttribute("flashType", "success");
        ra.addFlashAttribute("flashMessage", message);
    }

    private void flashError(RedirectAttributes ra, String message) {
        ra.addFlashAttribute("flashType", "error");
        ra.addFlashAttribute("flashMessage", message);
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        addSessionAttrs(model, session);

        List<PriceRecord> all = priceService.findAll();
        model.addAttribute("cropCount", cropService.findAll().size());
        model.addAttribute("marketCount", marketService.findAll().size());
        model.addAttribute("farmerCount", farmerService.findAll().size());
        model.addAttribute("todayCount", priceService.findForDate(LocalDate.now()).size());
        model.addAttribute("recent", all.size() > 6 ? all.subList(0, 6) : all);
        return "admin/dashboard";
    }

    /* ---------- Crops ---------- */

    @GetMapping("/crops")
    public String crops(Model model, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        addSessionAttrs(model, session);
        model.addAttribute("crops", cropService.findAll());
        return "admin/crops";
    }

    @PostMapping("/crops")
    public String addCrop(@RequestParam String name, @RequestParam String unit,
                          HttpSession session, RedirectAttributes ra) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        try {
            Crop crop = cropService.register(name, unit);
            flashSuccess(ra, "“" + crop.getName() + "” was added to the crop list.");
        } catch (IllegalArgumentException e) {
            flashError(ra, e.getMessage());
        }
        return "redirect:/admin/crops";
    }

    @PostMapping("/crops/{id}/delete")
    public String deleteCrop(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        try {
            cropService.delete(id);
            flashSuccess(ra, "The crop was removed.");
        } catch (DataIntegrityViolationException e) {
            flashError(ra, LINKED_RECORDS_MESSAGE);
        }
        return "redirect:/admin/crops";
    }

    /* ---------- Markets ---------- */

    @GetMapping("/markets")
    public String markets(Model model, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        addSessionAttrs(model, session);
        model.addAttribute("markets", marketService.findAll());
        return "admin/markets";
    }

    @PostMapping("/markets")
    public String addMarket(@RequestParam String name, @RequestParam String location,
                            HttpSession session, RedirectAttributes ra) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        try {
            Market market = marketService.register(name, location);
            flashSuccess(ra, "“" + market.getName() + "” was added as a market location.");
        } catch (IllegalArgumentException e) {
            flashError(ra, e.getMessage());
        }
        return "redirect:/admin/markets";
    }

    @PostMapping("/markets/{id}/delete")
    public String deleteMarket(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        try {
            marketService.delete(id);
            flashSuccess(ra, "The market location was removed.");
        } catch (DataIntegrityViolationException e) {
            flashError(ra, LINKED_RECORDS_MESSAGE);
        }
        return "redirect:/admin/markets";
    }

    /* ---------- Prices (validation rules live in PriceService) ---------- */

    @GetMapping("/prices")
    public String prices(Model model, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        addSessionAttrs(model, session);
        addPriceFormAttrs(model);
        return "admin/prices";
    }

    private void addPriceFormAttrs(Model model) {
        model.addAttribute("crops", cropService.findAll());
        model.addAttribute("markets", marketService.findAll());
        List<PriceRecord> recent = priceService.findAll();
        model.addAttribute("recent", recent.size() > 10 ? recent.subList(0, 10) : recent);
        model.addAttribute("today", LocalDate.now());
    }

    @PostMapping("/prices")
    public String submitPrice(@RequestParam Long cropId, @RequestParam Long marketId,
                              @RequestParam BigDecimal price, @RequestParam String priceDate,
                              @RequestParam(defaultValue = "false") boolean replaceExisting,
                              HttpSession session, RedirectAttributes ra) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;

        String enteredBy = (String) session.getAttribute("name");
        try {
            Crop crop = cropService.findById(cropId);
            Market market = marketService.findById(marketId);
            priceService.postPrice(crop, market, price, LocalDate.parse(priceDate), enteredBy, replaceExisting);
            flashSuccess(ra, replaceExisting
                    ? "The price was replaced. Farmers now see the corrected figure."
                    : "The price was saved. Farmers can see it now.");
        } catch (DuplicatePriceException e) {
            flashError(ra, e.getMessage());
            // keep the entry so the administrator can choose to replace the existing price
            ra.addFlashAttribute("dupCropId", cropId);
            ra.addFlashAttribute("dupMarketId", marketId);
            ra.addFlashAttribute("dupPrice", price);
            ra.addFlashAttribute("dupDate", priceDate);
        } catch (InvalidPriceDateException | DateTimeParseException | IllegalArgumentException e) {
            flashError(ra, e.getMessage() != null ? e.getMessage() : "Please check the crop, market, price and date.");
        }
        return "redirect:/admin/prices";
    }

    /* ---------- Farmers ---------- */

    @GetMapping("/farmers")
    public String farmers(Model model, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        addSessionAttrs(model, session);
        model.addAttribute("farmers", farmerService.findAll());
        model.addAttribute("markets", marketService.findAll());
        return "admin/farmers";
    }

    @PostMapping("/farmers")
    public String addFarmer(@RequestParam String name, @RequestParam(required = false) String phone,
                            @RequestParam Long marketId, HttpSession session, RedirectAttributes ra) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        try {
            var farmer = farmerService.register(name, phone, marketService.findById(marketId));
            flashSuccess(ra, "“" + farmer.getName() + "” was registered as a farmer member.");
        } catch (IllegalArgumentException e) {
            flashError(ra, e.getMessage());
        }
        return "redirect:/admin/farmers";
    }

    @PostMapping("/farmers/{id}/delete")
    public String deleteFarmer(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        try {
            farmerService.delete(id);
            flashSuccess(ra, "The farmer record was removed.");
        } catch (DataIntegrityViolationException e) {
            flashError(ra, LINKED_RECORDS_MESSAGE);
        }
        return "redirect:/admin/farmers";
    }

    /* ---------- Broadcast ---------- */

    @GetMapping("/broadcast")
    public String broadcast(Model model, HttpSession session) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        addSessionAttrs(model, session);
        model.addAttribute("todayPrices", priceService.findForDate(LocalDate.now()));
        model.addAttribute("farmerCount", farmerService.findAll().size());
        model.addAttribute("broadcasts", broadcastService.findAll());
        return "admin/broadcast";
    }

    @PostMapping("/broadcast")
    public String dispatch(HttpSession session, RedirectAttributes ra) {
        String guard = requireAdmin(session);
        if (guard != null) return guard;
        List<PriceRecord> todaysRecords = priceService.findForDate(LocalDate.now());
        if (todaysRecords.isEmpty()) {
            flashError(ra, "No prices have been posted today, so there is nothing to send. Post today's prices first.");
            return "redirect:/admin/broadcast";
        }
        int recipientCount = farmerService.findAll().size();
        broadcastService.dispatch(todaysRecords, recipientCount, (String) session.getAttribute("name"));
        flashSuccess(ra, "Today's prices were sent to " + recipientCount + " registered farmer"
                + (recipientCount == 1 ? "." : "s."));
        return "redirect:/admin/broadcast";
    }
}
