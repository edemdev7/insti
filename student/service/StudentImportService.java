package com.payiskoul.institution.student.service;

import com.payiskoul.institution.classroom.model.Classroom;
import com.payiskoul.institution.classroom.repository.ClassroomRepository;
import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.EnrollmentAlreadyExistsException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.program.repository.TrainingOfferRepository;
import com.payiskoul.institution.student.dto.CreateStudentRequest;
import com.payiskoul.institution.student.dto.FailedImportRecord;
import com.payiskoul.institution.student.dto.StudentImportResult;
import com.payiskoul.institution.student.model.Gender;
import com.payiskoul.institution.student.model.Student;
import com.payiskoul.institution.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service d'importation d'étudiants mis à jour pour utiliser le modèle unifié TrainingOffer
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentImportService {

    private final StudentRepository studentRepository;
    private final TrainingOfferRepository trainingOfferRepository; // Remplace ProgramLevelRepository
    private final ClassroomRepository classroomRepository;
    private final StudentService studentService;
    private final EnrollmentService enrollmentService;

    /**
     * Importe des étudiants depuis un fichier CSV
     *
     * @param file        Fichier CSV contenant les données des étudiants
     * @param offerId     ID de l'offre à laquelle inscrire les étudiants (remplace programId)
     * @param classroomId ID de la classe (optionnel)
     * @return Résultat de l'importation
     */
    @Transactional
    public StudentImportResult importStudentsFromCsv(MultipartFile file, String offerId, String classroomId) {
        log.info("Début de l'importation d'étudiants depuis un fichier CSV pour l'offre {}", offerId);

        // Vérifier que l'offre existe (remplace la vérification de programme)
        TrainingOffer trainingOffer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "L'offre spécifiée n'existe pas", Map.of("offerId", offerId)));

        // Vérifier que la classe existe si spécifiée
        if (classroomId != null && !classroomId.isEmpty()) {
            classroomRepository.findById(classroomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND,
                            "La classe spécifiée n'existe pas", Map.of("classroomId", classroomId)));
        }

        List<Map<String, String>> records = parseCsvFile(file);
        List<FailedImportRecord> failedRecords = new ArrayList<>();
        int successCount = 0;

        for (Map<String, String> studentRecord : records) {
            try {
                // Vérifier si un matricule est fourni
                String matricule = studentRecord.getOrDefault("matricule", "").trim();

                if (matricule.isEmpty() || matricule.isBlank()) {
                    // Cas 1: Nouvel étudiant à créer
                    processNewStudent(studentRecord, offerId, classroomId, failedRecords);
                    successCount++;
                } else {
                    // Cas 2: Étudiant existant à inscrire
                    processExistingStudent(matricule, offerId, classroomId, failedRecords);
                    successCount++;
                }
            } catch (Exception e) {
                log.error("Erreur lors de l'importation d'un étudiant: {}", e.getMessage());
                failedRecords.add(new FailedImportRecord(
                        studentRecord,
                        e.getMessage()
                ));
            }
        }

        log.info("Importation terminée. {} étudiants importés avec succès, {} échecs",
                successCount, failedRecords.size());

        return new StudentImportResult(successCount, failedRecords.size(), failedRecords);
    }

    /**
     * Importe des étudiants depuis un fichier Excel
     *
     * @param file        Fichier Excel contenant les données des étudiants
     * @param offerId     ID de l'offre à laquelle inscrire les étudiants (remplace programId)
     * @param classroomId ID de la classe (optionnel)
     * @return Résultat de l'importation
     */
    @Transactional
    public StudentImportResult importStudentsFromExcel(MultipartFile file, String offerId, String classroomId) {
        log.info("Début de l'importation d'étudiants depuis un fichier Excel pour l'offre {}", offerId);

        // Vérifier que l'offre existe
        TrainingOffer trainingOffer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "L'offre spécifiée n'existe pas", Map.of("offerId", offerId)));

        // Vérifier que la classe existe si spécifiée
        if (classroomId != null && !classroomId.isEmpty()) {
            classroomRepository.findById(classroomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND,
                            "La classe spécifiée n'existe pas", Map.of("classroomId", classroomId)));
        }

        List<Map<String, String>> records = parseExcelFile(file);
        List<FailedImportRecord> failedRecords = new ArrayList<>();
        int successCount = 0;

        for (Map<String, String> studentRecord : records) {
            try {
                // Vérifier si un matricule est fourni
                String matricule = studentRecord.getOrDefault("matricule", "").trim();

                if (matricule.isEmpty() || matricule.isBlank()) {
                    // Cas 1: Nouvel étudiant à créer
                    processNewStudent(studentRecord, offerId, classroomId, failedRecords);
                    successCount++;
                } else {
                    // Cas 2: Étudiant existant à inscrire
                    processExistingStudent(matricule, offerId, classroomId, failedRecords);
                    successCount++;
                }
            } catch (Exception e) {
                log.error("Erreur lors de l'importation d'un étudiant: {}", e.getMessage());
                failedRecords.add(new FailedImportRecord(
                        studentRecord,
                        e.getMessage()
                ));
            }
        }

        log.info("Importation terminée. {} étudiants importés avec succès, {} échecs",
                successCount, failedRecords.size());

        return new StudentImportResult(successCount, failedRecords.size(), failedRecords);
    }

    /**
     * Génère un template Excel pour l'importation d'étudiants
     *
     * @param offerId     ID de l'offre (remplace programId)
     * @param classroomId ID de la classe (optionnel)
     * @return Tableau d'octets représentant le fichier Excel
     * @throws IOException En cas d'erreur lors de la génération du fichier
     */
    public byte[] generateExcelTemplate(String offerId, String classroomId) throws IOException {
        log.info("Génération d'un template Excel pour l'offre {}", offerId);

        // Vérifier que l'offre existe
        TrainingOffer trainingOffer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "L'offre spécifiée n'existe pas", Map.of("offerId", offerId)));

        // Récupérer la classe si spécifiée
        Classroom classroom = null;
        if (classroomId != null && !classroomId.isEmpty()) {
            classroom = classroomRepository.findById(classroomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND,
                            "La classe spécifiée n'existe pas", Map.of("classroomId", classroomId)));
        }

        // Création du classeur Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            // Création de la feuille de données
            Sheet sheet = workbook.createSheet("Importation Étudiants");

            // Styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Entête avec informations sur l'offre (remplace programme)
            Row offerRow = sheet.createRow(0);
            Cell offerLabelCell = offerRow.createCell(0);
            offerLabelCell.setCellValue("Offre:");
            offerLabelCell.setCellStyle(headerStyle);

            Cell offerValueCell = offerRow.createCell(1);
            offerValueCell.setCellValue(trainingOffer.getLabel() + " (" + trainingOffer.getCode() + ")");

            // Classe si spécifiée
            if (classroom != null) {
                Row classroomRow = sheet.createRow(1);
                Cell classroomLabelCell = classroomRow.createCell(0);
                classroomLabelCell.setCellValue("Classe:");
                classroomLabelCell.setCellStyle(headerStyle);

                Cell classroomValueCell = classroomRow.createCell(1);
                classroomValueCell.setCellValue(classroom.getName());
            }

            // En-têtes des colonnes
            String[] columns = {"Matricule (optionnel)", "Nom complet", "Genre (MALE/FEMALE)",
                    "Date de naissance (YYYY-MM-DD)", "Email", "Téléphone"};
            Row headerRow = sheet.createRow(classroom != null ? 3 : 2);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 6000);
            }

            // Ajout d'une ligne d'exemple pour un nouvel étudiant
            Row exampleRow = sheet.createRow(classroom != null ? 4 : 3);
            exampleRow.createCell(0).setCellValue(""); // Matricule vide pour nouvel étudiant
            exampleRow.createCell(1).setCellValue("Seka Alexandre");
            exampleRow.createCell(2).setCellValue("MALE");
            exampleRow.createCell(3).setCellValue("2001-05-14");
            exampleRow.createCell(4).setCellValue("seka.alex@example.com");
            exampleRow.createCell(5).setCellValue("+2250701020304");

            // Ajout d'une ligne d'exemple pour un étudiant existant
            Row existingStudentRow = sheet.createRow(classroom != null ? 5 : 4);
            existingStudentRow.createCell(0).setCellValue("PI-CI-25A0001"); // Exemple de matricule existant
            existingStudentRow.createCell(1).setCellValue("Koné Aminata");
            existingStudentRow.createCell(2).setCellValue("FEMALE");
            existingStudentRow.createCell(3).setCellValue("2002-08-22");
            existingStudentRow.createCell(4).setCellValue("kone.aminata@example.com");
            existingStudentRow.createCell(5).setCellValue("+2250708090102");

            // Conversion en tableau d'octets
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // ============ MÉTHODES DE COMPATIBILITÉ ============

    /**
     * Méthode de compatibilité pour l'importation CSV avec programId
     * @deprecated Utiliser importStudentsFromCsv(file, offerId, classroomId)
     */
    @Deprecated
    public StudentImportResult importStudentsFromCsvWithProgramId(MultipartFile file, String programId, String classroomId) {
        log.warn("Utilisation de la méthode dépréciée importStudentsFromCsvWithProgramId - migrer vers importStudentsFromCsv avec offerId");
        return importStudentsFromCsv(file, programId, classroomId);
    }

    /**
     * Méthode de compatibilité pour l'importation Excel avec programId
     * @deprecated Utiliser importStudentsFromExcel(file, offerId, classroomId)
     */
    @Deprecated
    public StudentImportResult importStudentsFromExcelWithProgramId(MultipartFile file, String programId, String classroomId) {
        log.warn("Utilisation de la méthode dépréciée importStudentsFromExcelWithProgramId - migrer vers importStudentsFromExcel avec offerId");
        return importStudentsFromExcel(file, programId, classroomId);
    }

    /**
     * Méthode de compatibilité pour la génération de template avec programId
     * @deprecated Utiliser generateExcelTemplate(offerId, classroomId)
     */
    @Deprecated
    public byte[] generateExcelTemplateWithProgramId(String programId, String classroomId) throws IOException {
        log.warn("Utilisation de la méthode dépréciée generateExcelTemplateWithProgramId - migrer vers generateExcelTemplate avec offerId");
        return generateExcelTemplate(programId, classroomId);
    }

    // ============ MÉTHODES PRIVÉES ============

    /**
     * Parse un fichier CSV et retourne les enregistrements sous forme de liste de maps
     * @param file Fichier CSV à parser
     * @return Liste des enregistrements
     */
    private List<Map<String, String>> parseCsvFile(MultipartFile file) {
        List<Map<String, String>> records = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             InputStreamReader reader = new InputStreamReader(is);
             com.opencsv.CSVReader csvReader = new com.opencsv.CSVReader(reader)) {

            // Lecture des en-têtes
            String[] headers = csvReader.readNext();
            if (headers == null) {
                throw new BusinessException(ErrorCode.INVALID_FILE_FORMAT,
                        "Le fichier CSV ne contient pas d'en-têtes", null);
            }

            // Nettoyer les en-têtes pour enlever les suffixes explicatifs
            for (int i = 0; i < headers.length; i++) {
                headers[i] = cleanHeaderName(headers[i]);
            }

            // Lecture des données
            String[] line;

            while ((line = csvReader.readNext()) != null) {
                if (line.length < headers.length) {
                    continue; // Ignorer les lignes incomplètes
                }

                Map<String, String> entry = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    entry.put(headers[i].trim(), i < line.length ? line[i].trim() : "");
                }

                records.add(entry);
            }

        } catch (Exception e) {
            log.error("Erreur lors de la lecture du fichier CSV: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_FILE_FORMAT,
                    "Erreur lors de la lecture du fichier CSV: " + e.getMessage(), null);
        }

        return records;
    }

    /**
     * Parse un fichier Excel et retourne les enregistrements sous forme de liste de maps
     * @param file Fichier Excel à parser
     * @return Liste des enregistrements
     */
    private List<Map<String, String>> parseExcelFile(MultipartFile file) {
        List<Map<String, String>> lines = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            // Lecture de la première feuille
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Déterminer la ligne d'en-tête (ignorer les lignes d'information sur l'offre)
            Row headerRow = null;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell firstCell = row.getCell(0);
                if (firstCell != null && firstCell.getCellType() == CellType.STRING) {
                    String value = firstCell.getStringCellValue();
                    if (!value.contains(":") && !value.isEmpty()) {
                        headerRow = row;
                        break;
                    }
                }
            }

            if (headerRow == null) {
                throw new BusinessException(ErrorCode.INVALID_FILE_FORMAT,
                        "Le fichier Excel ne contient pas d'en-têtes valides", null);
            }

            // Lecture des en-têtes et nettoyage des suffixes explicatifs
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().isEmpty()) {
                    String headerName = cleanHeaderName(cell.getStringCellValue().trim());
                    headers.add(headerName);
                }
            }

            // Lecture des données
            readData(headers, rowIterator, lines);

        } catch (Exception e) {
            log.error("Erreur lors de la lecture du fichier Excel: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_FILE_FORMAT,
                    "Erreur lors de la lecture du fichier Excel: " + e.getMessage(), null);
        }

        return lines;
    }

    /**
     * Crée une requête de création d'étudiant à partir d'un enregistrement
     *
     * @param studentRecord Enregistrement contenant les données de l'étudiant
     * @param offerId       ID de l'offre (remplace programId)
     * @param classroomId   ID de la classe (optionnel)
     * @return Requête de création d'étudiant
     */
    private CreateStudentRequest createRequestFromRecord(Map<String, String> studentRecord, String offerId, String classroomId) {
        // Récupération des données avec validation
        String fullName = getRequiredField(studentRecord, "fullName", "Nom complet");

        // Parsing du genre
        String genderStr = getRequiredField(studentRecord, "gender", "Genre");
        Gender gender;
        try {
            gender = Gender.valueOf(genderStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "Genre invalide. Doit être MALE ou FEMALE", Map.of("gender", genderStr));
        }

        // Parsing de la date de naissance
        String birthDateStr = getRequiredField(studentRecord, "birthDate", "Date de naissance");
        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(birthDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "Format de date invalide. Utilisez YYYY-MM-DD", Map.of("birthDate", birthDateStr));
        }

        // Validation de l'email
        String email = getRequiredField(studentRecord, "email", "Email");
        if (!email.contains("@") || !email.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "Email manquant ou invalide", Map.of("email", email));
        }

        // Récupération du téléphone
        String phone = getRequiredField(studentRecord, "phone", "Téléphone");

        // Création de la requête (programId devient offerId)
        return new CreateStudentRequest(
                fullName,
                gender,
                birthDate,
                email,
                phone,
                offerId, // Changement: utilise offerId au lieu de programId
                classroomId
        );
    }

    /**
     * Traite un nouvel étudiant (création + inscription)
     */
    private void processNewStudent(Map<String, String> studentRecord, String offerId, String classroomId,
                                   List<FailedImportRecord> failedRecords) {
        // Créer la requête
        CreateStudentRequest request = createRequestFromRecord(studentRecord, offerId, classroomId);

        // Vérifier si l'email existe déjà
        if (studentRepository.existsByEmail(request.email())) {
            failedRecords.add(new FailedImportRecord(
                    studentRecord,
                    "Un étudiant avec cet email existe déjà"
            ));
            return;
        }

        // Créer l'étudiant (avec son inscription)
        studentService.createStudent(request);
        log.info("Nouvel étudiant créé et inscrit à l'offre: {}", request.email());
    }

    /**
     * Traite un étudiant existant (inscription seulement)
     */
    private void processExistingStudent(String matricule, String offerId, String classroomId,
                                        List<FailedImportRecord> failedRecords) {
        // Vérifier si l'étudiant existe avec ce matricule
        Optional<Student> studentOpt = studentRepository.findByMatricule(matricule);

        if (studentOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND,
                    "Aucun étudiant trouvé avec ce matricule", Map.of("matricule", matricule));
        }

        Student student = studentOpt.get();

        // Vérifier si l'étudiant est déjà inscrit à cette offre
        TrainingOffer trainingOffer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        // Construire l'année académique actuelle
        String academicYear = trainingOffer.getAcademicYear();

        // Créer une inscription pour l'étudiant existant
        try {
            enrollmentService.enrollStudentToOffer(
                    student.getId(),
                    offerId,
                    trainingOffer.getInstitutionId(),
                    academicYear,
                    classroomId
            );
            log.info("Étudiant existant inscrit à l'offre: {}", matricule);
        } catch (EnrollmentAlreadyExistsException e) {
            // L'étudiant est déjà inscrit à cette offre pour cette année
            log.warn("L'étudiant est déjà inscrit à cette offre: {}", matricule);
            throw e;
        }
    }

    /**
     * Récupère un champ obligatoire d'un enregistrement
     * @param entry Enregistrement
     * @param key Clé du champ
     * @param fieldName Nom du champ pour les messages d'erreur
     * @return Valeur du champ
     */
    private String getRequiredField(Map<String, String> entry, String key, String fieldName) {
        // Liste des variantes possibles pour les clés
        List<String> possibleKeys = new ArrayList<>();
        possibleKeys.add(key); // Clé originale (ex: "fullName")
        possibleKeys.add(key.substring(0, 1).toUpperCase() + key.substring(1)); // PremièreLettreMajuscule (ex: "FullName")
        possibleKeys.add(fieldName); // Nom du champ (ex: "Nom complet")
        possibleKeys.add(fieldName.toLowerCase()); // Nom du champ en minuscules (ex: "nom complet")

        // Gérer les suffixes des en-têtes générés dans le template
        possibleKeys.add(fieldName + " (optionnel)");
        possibleKeys.add(fieldName + " (MALE/FEMALE)");
        possibleKeys.add(fieldName + " (YYYY-MM-DD)");

        // Check si c'est le matricule
        boolean isMatriculeField = key.equals("matricule") || fieldName.equals("Matricule");

        // Chercher la valeur parmi toutes les variantes possibles
        String value = null;
        for (String possibleKey : possibleKeys) {
            if (entry.containsKey(possibleKey) && !entry.get(possibleKey).isEmpty()) {
                value = entry.get(possibleKey).trim();
                break;
            }
        }

        // Si aucune valeur trouvée
        if (value == null || value.isEmpty()) {
            // Exception pour le matricule qui est optionnel
            if (isMatriculeField) {
                return "";
            }
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    fieldName + " manquant ou vide", Map.of("field", key));
        }

        return value;
    }

    /**
     * Lit les données des lignes Excel
     */
    private void readData(List<String> headers, Iterator<Row> rowIterator, List<Map<String, String>> entries) {
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            // Vérifier si la ligne n'est pas vide
            boolean isEmpty = true;
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.getCell(i);
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    isEmpty = false;
                    break;
                }
            }

            if (isEmpty) {
                continue;
            }

            var line = writeLines(headers, row);
            entries.add(line);
        }
    }

    /**
     * Écrit les lignes de données
     */
    private Map<String, String> writeLines(List<String> headers, Row row) {
        Map<String, String> line = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.getCell(i);
            String value = "";

            if (cell != null) {
                switch (cell.getCellType()) {
                    case STRING:
                        value = cell.getStringCellValue().trim();
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            value = cell.getLocalDateTimeCellValue().toLocalDate().toString();
                        } else {
                            // Gérer les nombres entiers pour éviter les .0
                            double numericValue = cell.getNumericCellValue();
                            if (numericValue == (long) numericValue) {
                                value = String.valueOf((long) numericValue);
                            } else {
                                value = String.valueOf(numericValue);
                            }
                        }
                        break;
                    case BOOLEAN:
                        value = String.valueOf(cell.getBooleanCellValue());
                        break;
                    default:
                        value = "";
                }
            }
            line.put(headers.get(i), value);
        }
        return line;
    }

    /**
     * Nettoie les noms d'en-têtes en supprimant les suffixes explicatifs
     * @param headerName Nom de l'en-tête à nettoyer
     * @return Nom nettoyé
     */
    private String cleanHeaderName(String headerName) {
        String cleaned = headerName;

        // Supprimer les suffixes explicatifs courants
        cleaned = cleaned.replace(" (optionnel)", "");
        cleaned = cleaned.replace(" (MALE/FEMALE)", "");
        cleaned = cleaned.replace(" (YYYY-MM-DD)", "");

        // Mapper aux noms attendus
        if (cleaned.equalsIgnoreCase("Matricule")) {
            return "matricule";
        } else if (cleaned.equalsIgnoreCase("Nom complet")) {
            return "fullName";
        } else if (cleaned.equalsIgnoreCase("Genre")) {
            return "gender";
        } else if (cleaned.equalsIgnoreCase("Date de naissance")) {
            return "birthDate";
        } else if (cleaned.equalsIgnoreCase("Email")) {
            return "email";
        } else if (cleaned.equalsIgnoreCase("Téléphone")) {
            return "phone";
        }

        return cleaned;
    }
}