package com.example.WalletProject.models.DTO;

import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
//валидация нужна
public class DocumentDto {

    @Pattern(regexp = "[a-z]{2}[0-9]{7}", flags = Pattern.Flag.CASE_INSENSITIVE)
    private final String documentNumber;
    private final LocalDate issueDate;
    //меняем айди на название страны.
    private final Integer countryId;

    public DocumentDto(String documentNumber, LocalDate issueDate, Integer countryId) {
        this.documentNumber = documentNumber;
        this.issueDate = issueDate;
        this.countryId = countryId;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }
    public LocalDate getIssueDate() {
        return issueDate;
    }
    public Integer getCountryId() {
        return countryId;
    }

}