package kz.salyqtez.broker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.List;

public class TicketConverter implements AttributeConverter<List<TicketDto>, String> {
    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(List<TicketDto> customerInfo) {

        String customerInfoJson = null;
        try {
            customerInfoJson = objectMapper.writeValueAsString(customerInfo);
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
        }

        return customerInfoJson;
    }

    @Override
    public List<TicketDto> convertToEntityAttribute(String customerInfoJSON) {

        List<TicketDto> customerInfo = null;
        try {
            customerInfo = objectMapper.readValue(customerInfoJSON, List.class);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return customerInfo;
    }

}
