package io.loghub.loghub_api.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.loghub.loghub_api.dto.LogEvent;
import io.loghub.loghub_api.dto.LogEventResponse;
import io.loghub.loghub_api.dto.SdkInfo;
import io.loghub.loghub_api.entity.LogEventEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LogEventMapper {

    private static final Logger log = LoggerFactory.getLogger(LogEventMapper.class);

    private final ObjectMapper objectMapper;

    public LogEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public LogEventEntity toEntity(LogEvent dto) {
        LogEventEntity entity = new LogEventEntity();
        entity.setApplication(dto.application());
        entity.setEnvironment(dto.environment());
        entity.setLevel(dto.level());
        entity.setMessage(dto.message());
        entity.setTimestamp(dto.timestamp());
        entity.setTraceId(dto.traceId());

        if (dto.metadata() != null) {
            try {
                entity.setMetadata(objectMapper.writeValueAsString(dto.metadata()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize metadata for log event (application={}), dropping it",
                        dto.application(), e);
                entity.setMetadata(null);
            }
        }

        if (dto.sdk() != null) {
            entity.setSdkLanguage(dto.sdk().language());
            entity.setSdkVersion(dto.sdk().version());
        }

        return entity;
    }

    public LogEventResponse toResponse(LogEventEntity entity) {
        Map<String, Object> metadata = null;
        if (entity.getMetadata() != null) {
            try {
                metadata = objectMapper.readValue(entity.getMetadata(), new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                log.warn("Failed to deserialize metadata for log event (id={}), dropping it",
                        entity.getId(), e);
                metadata = null;
            }
        }

        SdkInfo sdk = null;
        if (entity.getSdkLanguage() != null && entity.getSdkVersion() != null) {
            sdk = new SdkInfo(entity.getSdkLanguage(), entity.getSdkVersion());
        }

        return new LogEventResponse(
                entity.getId(),
                entity.getApplication(),
                entity.getEnvironment(),
                entity.getLevel(),
                entity.getMessage(),
                entity.getTimestamp(),
                entity.getTraceId(),
                metadata,
                sdk
        );
    }
}

