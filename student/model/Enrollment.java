package com.payiskoul.institution.student.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "enrollments")
public class Enrollment implements Serializable {
    @Id
    private String id;

    private String studentId;
    private String programLevelId;
    private String institutionId;
    private String classroomId;
    private String academicYear;

    private EnrollmentStatus status;

    @CreatedDate
    private LocalDateTime enrolledAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    public enum EnrollmentStatus {
        ENROLLED, COMPLETED, LEFT, SUSPENDED
    }
}