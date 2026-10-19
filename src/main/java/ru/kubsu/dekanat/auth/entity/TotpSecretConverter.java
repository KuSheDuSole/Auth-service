package ru.kubsu.dekanat.auth.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.kubsu.dekanat.auth.security.TotpSecretEncryptor;

@Component
@Converter
@RequiredArgsConstructor
public class TotpSecretConverter implements AttributeConverter<String, String> {

    private final TotpSecretEncryptor encryptor;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : encryptor.decrypt(dbData);
    }

}
