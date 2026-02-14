package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.AmortizationTypeGateway;
import com.recaudo.api.domain.model.dto.response.AmortizationItemDto;
import com.recaudo.api.domain.model.dto.response.AmortizationResponseDto;
import com.recaudo.api.domain.model.dto.response.AmortizationTypeResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.AmortizationRequestDto;
import com.recaudo.api.domain.model.dto.rest_api.AmortizationTypeCreateDto;
import com.recaudo.api.domain.model.entity.AmortizationTypeEntity;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.AmortizationTypeRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AmortizationTypeAdapter implements AmortizationTypeGateway {

    AmortizationTypeRepository amortizationRepository;

     @Override
     public List<AmortizationTypeResponseDto> getAll() {
        return amortizationRepository.findAllByStatusTrue(Sort.by(Sort.Direction.DESC, "id"))                .stream()
                .map(data -> AmortizationTypeResponseDto.builder()
                        .id(data.getId())
                        .code(data.getCode())
                        .name(data.getName())
                        .description(data.getDescription())
                        .procedure(data.getProcedureName())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build())
                .toList();
     }

    @Override
    public AmortizationTypeResponseDto getById(Long id) {
        return amortizationRepository.findById(id)
                .map(data -> AmortizationTypeResponseDto.builder()
                        .id(data.getId())
                        .code(data.getCode())
                        .name(data.getName())
                        .description(data.getDescription())
                        .procedure(data.getProcedureName())
                        .status(data.isStatus() ? "ACTIVO" : "INACTIVO")
                        .build()
                )
                .orElseThrow(() -> new BadRequestException("AmortizationType con id " + id + " no encontrado"));
    }

    @Override
    public AmortizationTypeResponseDto create(AmortizationTypeCreateDto data) {

        if (amortizationRepository.existsByName(data.getName()))
            throw new BadRequestException("Ya existe un tipo de amortizacion con este nombre");

         if(amortizationRepository.existsByCode(data.getCode()))
             throw new BadRequestException("Ya existe este codigo de amortizacion");

        AmortizationTypeEntity amortizationTypeEntity = AmortizationTypeEntity.builder()
                .code(data.getCode().toUpperCase())
                .name(data.getName().toUpperCase())
                .description(data.getDescription().toUpperCase())
                .procedureName(data.getProcedure().toUpperCase())
                .userCreate(getUsernameToken())
                .createdAt(LocalDateTime.now())
                .build();
        amortizationRepository.save(amortizationTypeEntity);

         return AmortizationTypeResponseDto.builder()
                 .id(amortizationTypeEntity.getId())
                 .code(amortizationTypeEntity.getCode())
                 .description(amortizationTypeEntity.getDescription())
                 .procedure(amortizationTypeEntity.getProcedureName())
                 .status(amortizationTypeEntity.isStatus() ? "ACTIVO" : "INACTIVO")
                 .build();
    }

    @Override
    public AmortizationTypeResponseDto edit(Long id, AmortizationTypeCreateDto data) {
        AmortizationTypeEntity entity = amortizationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("AmortizationType con id " + id + " no encontrado"));

        // validación si el nuevo código ya existe en otro registro
        if (!entity.getCode().equals(data.getCode()) && amortizationRepository.existsByCode(data.getCode())) {
            throw new BadRequestException("Ya existe un tipo de amortización con este código");
        }

        entity.setCode(data.getCode().toUpperCase());
        entity.setName(data.getName().toUpperCase());
        entity.setDescription(data.getDescription().toUpperCase());
        entity.setProcedureName(data.getProcedure().toUpperCase());
        entity.setUserEdit(getUsernameToken());
        entity.setEditedAt(LocalDateTime.now());

        amortizationRepository.save(entity);

        return AmortizationTypeResponseDto.builder()
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .procedure(entity.getProcedureName())
                .status(entity.isStatus() ? "ACTIVO" : "INACTIVO")
                .build();
    }

    @Override
    public AmortizationTypeResponseDto delete(Long id) {
        AmortizationTypeEntity entity = amortizationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("AmortizationType con id " + id + " no encontrado"));

        // Eliminación lógica
        entity.setStatus(false);
        entity.setUserEdit(getUsernameToken());
        entity.setDeletedAt(LocalDateTime.now());

        amortizationRepository.save(entity);

        return AmortizationTypeResponseDto.builder()
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .procedure(entity.getProcedureName())
                .status("INACTIVO")
                .build();
    }

    @Override
    public AmortizationResponseDto calculate(AmortizationRequestDto amortizationRequestDto) {
        String code = amortizationRequestDto.getCode().toUpperCase();
        double capital = amortizationRequestDto.getCapital();
        double interes = amortizationRequestDto.getInteres();
        int periodos = amortizationRequestDto.getPeriodos();
        int conversionFactor = amortizationRequestDto.getConversionFactor();

        List<AmortizationItemDto> resultado;

        switch (code) {
            case "AME": // Sistema Americano
                resultado = sistemaAmericano(capital, interes, periodos);
                break;

            case "FRA": // Sistema Francés
                // Si el sistema francés requiere conversión de interés anual → mensual
                double interesMensual = interes / conversionFactor;
                resultado = sistemaFrances(capital, interesMensual, periodos);
                break;

            case "ALE": // Sistema Alemán
                resultado = sistemaAleman(capital, interes, periodos);
                break;

            default:
                throw new IllegalArgumentException("Código de sistema de amortización no válido: " + code);
        }

        // Calcular total a pagar (suma de todas las cuotas)
        BigDecimal totalPagar = resultado.stream()
                .map(item -> BigDecimal.valueOf(item.getCuota()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);



        // Retornamos el resultado dentro de un wrapper DTO
        AmortizationResponseDto responseDto = new AmortizationResponseDto();
        responseDto.setAmortizationTable(resultado);
        responseDto.setTotalPagar(totalPagar);
        responseDto.setCode(code);
        responseDto.setMessage("Cálculo realizado correctamente para el sistema " + code);

        return responseDto;
     }

    public List<AmortizationItemDto> sistemaAmericano(double capital, double interesMensual, int periodos) {
        List<AmortizationItemDto> response = new ArrayList<>();

        interesMensual = interesMensual / 100;
        double saldo = capital;

        for (int i = 1; i <= periodos; i++) {
            double interes = saldo * interesMensual;
            double amortizacion = (i == periodos) ? capital : 0;
            double cuota = interes + amortizacion;
            saldo -= amortizacion;

            response.add(new AmortizationItemDto(i, cuota, interes, amortizacion, Math.max(saldo, 0)));
        }

        return response;
    }
    public List<AmortizationItemDto> sistemaFrances(double capital, double interesMensual, int periodos) {
        List<AmortizationItemDto> response = new ArrayList<>();

        // tasa por periodo (ej. si pasas 8 -> 8% mensual)
        BigDecimal tasa = BigDecimal.valueOf(interesMensual).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal bdCapital = BigDecimal.valueOf(capital);
        int n = periodos;

        // Si la tasa es 0 -> cuota = capital / n
        BigDecimal cuota;
        if (tasa.compareTo(BigDecimal.ZERO) == 0) {
            cuota = bdCapital.divide(BigDecimal.valueOf(n), 10, RoundingMode.HALF_UP);
        } else {
            // cuota = C * [ i * (1+i)^n ] / [ (1+i)^n - 1 ]
            BigDecimal unoMasI = BigDecimal.ONE.add(tasa);
            BigDecimal pow = unoMasI.pow(n); // (1+i)^n
            BigDecimal numerador = bdCapital.multiply(tasa).multiply(pow);
            BigDecimal denominador = pow.subtract(BigDecimal.ONE);
            cuota = numerador.divide(denominador, 10, RoundingMode.HALF_UP);
        }

        // saldo inicial
        BigDecimal saldo = bdCapital;

        for (int i = 1; i <= n; i++) {
            // interés del periodo = saldo * tasa
            BigDecimal interes = saldo.multiply(tasa);

            // amortización = cuota - interés
            BigDecimal amortizacion = cuota.subtract(interes);

            // En el último periodo ajustamos para evitar residuos por redondeo:
            if (i == n) {
                // forzamos que la amortización sea exactamente el saldo (resto)
                amortizacion = saldo;
                // recalculamos la cuota final como interés + amortización (por si hay ajuste)
                cuota = interes.add(amortizacion);
                saldo = BigDecimal.ZERO;
            } else {
                saldo = saldo.subtract(amortizacion);
            }

            // Redondeo para presentación (igual que Excel)
            double cuotaRounded = cuota.setScale(2, RoundingMode.HALF_UP).doubleValue();
            double interesRounded = interes.setScale(2, RoundingMode.HALF_UP).doubleValue();
            double amortizacionRounded = amortizacion.setScale(2, RoundingMode.HALF_UP).doubleValue();
            double saldoRounded = saldo.setScale(2, RoundingMode.HALF_UP).doubleValue();

            response.add(new AmortizationItemDto(i, cuotaRounded, interesRounded, amortizacionRounded, Math.max(saldoRounded, 0.0)));
        }

        return response;
    }
    public List<AmortizationItemDto> sistemaAleman(double capital, double interesPeriodo, int periodos) {
        List<AmortizationItemDto> response = new ArrayList<>();

        BigDecimal tasa = BigDecimal.valueOf(interesPeriodo).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal bdCapital = BigDecimal.valueOf(capital);
        BigDecimal bdPeriodos = BigDecimal.valueOf(periodos);
        BigDecimal amortizacionConstante = bdCapital.divide(bdPeriodos, 10, RoundingMode.HALF_UP);
        BigDecimal saldo = bdCapital;

        for (int i = 1; i <= periodos; i++) {
            BigDecimal interes = saldo.multiply(tasa);
            BigDecimal amortizacion = amortizacionConstante;
            BigDecimal cuota = interes.add(amortizacion);
            saldo = saldo.subtract(amortizacion);

            // Ajuste final (saldo = 0 en la última)
            if (i == periodos) {
                if (saldo.abs().compareTo(BigDecimal.valueOf(0.01)) <= 0) {
                    saldo = BigDecimal.ZERO;
                }
            }

            double cuotaRounded = cuota.setScale(2, RoundingMode.HALF_UP).doubleValue();
            double interesRounded = interes.setScale(2, RoundingMode.HALF_UP).doubleValue();
            double amortizacionRounded = amortizacion.setScale(2, RoundingMode.HALF_UP).doubleValue();
            double saldoRounded = saldo.setScale(2, RoundingMode.HALF_UP).doubleValue();

            response.add(new AmortizationItemDto(i, cuotaRounded, interesRounded, amortizacionRounded, Math.max(saldoRounded, 0.0)));
        }

        return response;
    }
    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }

}
