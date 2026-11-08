package cz.mendelu.ipa.controller;

import cz.mendelu.ipa.utils.data.DataImporter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DataImporter dataImporter;

    public AdminController(DataImporter dataImporter) {
        this.dataImporter = dataImporter;
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public String runDataImporter() {
        return dataImporter.importData();
    }
}