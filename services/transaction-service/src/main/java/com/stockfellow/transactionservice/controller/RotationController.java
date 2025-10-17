package com.stockfellow.transactionservice.controller;
import com.stockfellow.transactionservice.dto.CreateRotationDto;
import com.stockfellow.transactionservice.model.Rotation;
import com.stockfellow.transactionservice.service.RotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@Tag(name = "Rotation", description = "Operations related to a rotation of group cycles")
@RequestMapping("/api/rotations")
@CrossOrigin(origins = "*")
public class RotationController {
    @Autowired
    private RotationService rotationService;

    @PostMapping
    @Operation(summary = "", description = "")
    public ResponseEntity<Rotation> createRotation(@Valid @RequestBody CreateRotationDto entity) {
        Rotation created = rotationService.createRotation(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
}
