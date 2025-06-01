package com.payiskoul.institution.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InstitutionNotFoundException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Institution introuvable",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleInstitutionNotFoundException(
            InstitutionNotFoundException ex, WebRequest request) {
        log.error("Institution introuvable: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InstitutionAlreadyExistsException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "409", description = "Institution déjà existante",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleInstitutionAlreadyExistsException(
            InstitutionAlreadyExistsException ex, WebRequest request) {
        log.error("Institution déjà existante: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(StudentNotFoundException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleStudentNotFoundException(
            StudentNotFoundException ex, WebRequest request) {
        log.error("Étudiant introuvable: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(StudentAlreadyExistsException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "409", description = "Étudiant déjà existant",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleStudentAlreadyExistsException(
            StudentAlreadyExistsException ex, WebRequest request) {
        log.error("Étudiant déjà existant: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(EnrollmentAlreadyExistsException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "409", description = "Inscription déjà existante",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleEnrollmentAlreadyExistsException(
            EnrollmentAlreadyExistsException ex, WebRequest request) {
        log.error("Inscription déjà existante: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ProgramLevelNotFoundException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Niveau de programme introuvable",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleProgramLevelNotFoundException(
            ProgramLevelNotFoundException ex, WebRequest request) {
        log.error("Niveau de programme introuvable: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Erreur de validation des données",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                ErrorCode.VALIDATION_FAILED.name(),
                "Erreur de validation des données d'entrée",
                request.getDescription(false),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "405", description = "Erreur de validation des données",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        Map<String, Object> errors = Map.of(ex.getMethod(), ex.getMessage());
        ex.getMethod();

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                ErrorCode.UNSUPPORTED_METHOD.name(),
                ErrorCode.UNSUPPORTED_METHOD.getDescription(),
                request.getDescription(false),
                errors
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Erreur non gérée: ", ex);

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                "Une erreur inattendue s'est produite",
                request.getDescription(false),
                Map.of("exception", ex.getClass().getSimpleName())
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(ProgramLevelAlreadyExistsException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "409", description = "Niveau de programme déjà existant",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleProgramLevelAlreadyExistsException(
            ProgramLevelAlreadyExistsException ex, WebRequest request) {
        log.error("Niveau de programme déjà existant: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    @ExceptionHandler(InvalidFileFormatException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Format de fichier invalide",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleInvalidFileFormatException(
            InvalidFileFormatException ex, WebRequest request) {
        log.error("Format de fichier invalide: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(FileProcessingException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Erreur de traitement du fichier",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleFileProcessingException(
            FileProcessingException ex, WebRequest request) {
        log.error("Erreur de traitement du fichier: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(MissingRequiredColumnsException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Colonnes requises manquantes",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleMissingRequiredColumnsException(
            MissingRequiredColumnsException ex, WebRequest request) {
        log.error("Colonnes requises manquantes: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ImportDataValidationException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Erreur de validation des données importées",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleImportDataValidationException(
            ImportDataValidationException ex, WebRequest request) {
        log.error("Erreur de validation des données importées: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BusinessException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Erreur de validation des données importées",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.error("Erreur de validation des données importées: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MaxFileSizeExceededException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "413", description = "Taille maximale du fichier dépassée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleMaxFileSizeExceededException(
            MaxFileSizeExceededException ex, WebRequest request) {
        log.error("Taille maximale du fichier dépassée: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "415", description = "Type de fichier non supporté",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleUnsupportedFileTypeException(
            UnsupportedFileTypeException ex, WebRequest request) {
        log.error("Type de fichier non supporté: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    // Gestion des erreurs de Spring pour les fichiers multipart
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "413", description = "Taille maximale du fichier dépassée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex, WebRequest request) {
        log.error("Taille maximale du fichier dépassée: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                ErrorCode.MAX_FILE_SIZE_EXCEEDED.name(),
                "La taille du fichier téléchargé dépasse la limite autorisée",
                request.getDescription(false),
                Map.of("maxSize", ex.getMaxUploadSize())
        );

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    @ExceptionHandler(org.springframework.web.multipart.MultipartException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Erreur dans le traitement du fichier multipart",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleMultipartException(
            org.springframework.web.multipart.MultipartException ex, WebRequest request) {
        log.error("Erreur dans le traitement du fichier multipart: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                ErrorCode.FILE_PROCESSING_ERROR.name(),
                "Erreur lors du traitement du fichier multipart",
                request.getDescription(false),
                Map.of("error", ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ClassroomNotFound.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Salle de classe introuvable",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleClassroomNotFound(
            ClassroomNotFound ex, WebRequest request) {
        log.error("Erreur dans le traitement du fichier multipart: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                request.getDescription(false),
                ex.getDetails()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(PaymentAlreadyProcessedException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Salle de classe introuvable",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handlePaymentAlreadyProcessedException(
            PaymentAlreadyProcessedException ex, WebRequest request) {
        log.error("Erreur dans le traitement du fichier multipart: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                request.getDescription(false),
                ex.getDetails()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(UserRegistrationException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Échec de l'enregistrement de l'utilisateur",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ErrorResponse> handleUserRegistrationException(
            UserRegistrationException ex, WebRequest request) {
        log.error("Échec de l'enregistrement de l'utilisateur: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }


}