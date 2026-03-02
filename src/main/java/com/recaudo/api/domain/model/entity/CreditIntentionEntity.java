package com.recaudo.api.domain.model.entity;

import com.recaudo.api.domain.model.constant.ApprovalStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit_intention")
public class CreditIntentionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zone_id")
    private Long zoneId;

    @Column(name = "document_type")
    private Long documentType;

    @Column(name = "document")
    private String document;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "middlename")
    private String middlename;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "maternal_lastname")
    private String maternalLastname;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "gender")
    private Long gender;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "description")
    private String description;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Column(name = "credit_line_id")
    private Long creditLineId;

    @Column(name = "quota_value")
    private BigDecimal quotaValue;

    @Column(name = "period_id")
    private Long periodId;

    @Column(name = "period_quantity")
    private Long periodQuantity;

    @Column(name = "tax_type_id")
    private Long taxTypeId;

    @Column(name = "tax_value")
    private BigDecimal taxValue;

    @Column(name = "total_intention_value")
    private BigDecimal totalIntentionValue;

    @Column(name = "total_interest_value")
    private BigDecimal totalInterestValue;

    @Column(name = "total_capital_value")
    private BigDecimal totalCapitalValue;

    @Column(name = "item_value")
    private BigDecimal itemValue;

    @Column(name = "initial_value_payment")
    private BigDecimal initialValuePayment;

    @Column(name = "total_financed_value")
    private BigDecimal totalFinancedValue;

    @Column(name = "home_address")
    private String homeAddress;

    @Column(name = "country_id")
    private Long countryId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "municipality_id")
    private Long municipalityId;

    @Column(name = "neighborhood_id")
    private Long neighborhoodId;

    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "user_delete")
    private String userDelete;

    @Column(name = "user_edit")
    private String userEdit;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "referido")
    private Boolean referido;

    @Column(name = "call_success")
    private Boolean callSuccess;

    @Column(name = "initial_quincena")
    private Integer initialQuincena;

    @Column(name = "end_quincena")
    private Integer endQuincena;

    @Column(name = "approval_link", length = 500)
    private String approvalLink;

    @Column(name = "approval_token", length = 100, unique = true)
    private String approvalToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approval_ip", length = 45)
    private String approvalIp;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "date_start")
    private LocalDate dateStart;

    @Column(name = "stationery", precision = 10, scale = 2)
    private BigDecimal stationery;
}
