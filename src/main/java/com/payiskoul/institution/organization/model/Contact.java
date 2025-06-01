package com.payiskoul.institution.organization.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact implements Serializable {
    private String email;
    private String phone;
}