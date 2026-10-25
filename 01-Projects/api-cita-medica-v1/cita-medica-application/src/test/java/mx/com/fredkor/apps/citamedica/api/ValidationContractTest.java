package mx.com.fredkor.apps.citamedica.api;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import mx.com.fredkor.apps.citamedica.catalogos.application.service.SucursalService;
import mx.com.fredkor.apps.citamedica.catalogos.web.controller.SucursalController;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.request.SucursalRequest;
import mx.com.fredkor.apps.citamedica.core.application.service.CitaService;
import mx.com.fredkor.apps.citamedica.core.web.controller.CitaController;
import mx.com.fredkor.apps.citamedica.core.web.dto.request.CitaRequest;
import mx.com.fredkor.apps.citamedica.usuario.web.dto.request.UsuarioRequest;
import mx.com.fredkor.apps.citamedica.shared.exception.ResourceNotFoundException;
import mx.com.fredkor.apps.citamedica.shared.web.exception.GlobalExceptionHandler;
import mx.com.fredkor.apps.citamedica.platform.configuration.ApiErrorDocumentation;
import io.swagger.v3.oas.models.*;
import org.junit.jupiter.api.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.MockMakers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ValidationContractTest {
    private MockMvc mvc;
    private SucursalService service;
    private CitaService citas;
    private LocalValidatorFactoryBean mvcValidator;
    private ValidatorFactory factory;

    @BeforeEach
    void setup() {
        service = mock(SucursalService.class, withSettings().mockMaker(MockMakers.PROXY));
        citas = mock(CitaService.class, withSettings().mockMaker(MockMakers.PROXY));
        factory = Validation.buildDefaultValidatorFactory();
        mvcValidator = new LocalValidatorFactoryBean();
        mvcValidator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new SucursalController(service), new CitaController(citas))
            .setControllerAdvice(new GlobalExceptionHandler()).setValidator(mvcValidator).build();
    }

    @AfterEach
    void close() { factory.close(); mvcValidator.close(); }

    @Test
    void invalidBodyAndMalformedJsonReturn400() throws Exception {
        mvc.perform(post("/api/v1/sucursales").contentType("application/json")
            .content("{\"nombre\":\"   \",\"direccion\":\"\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.fieldErrors.length()").value(2));
        mvc.perform(post("/api/v1/sucursales").contentType("application/json").content("{"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("BAD_REQUEST"));
        verifyNoInteractions(service);
    }

    @Test
    void invalidParametersAreRejectedBeforeService() throws Exception {
        for (String query : List.of("page=-1", "size=0", "size=101", "orderBy=password", "orderDir=otro")) {
            mvc.perform(get("/api/v1/sucursales?" + query)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }
        mvc.perform(get("/api/v1/sucursales/-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/sucursales/abc")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void notFoundConflictAndForbiddenHaveConsistentStatus() throws Exception {
        when(service.getById(1)).thenThrow(new ResourceNotFoundException("Sucursal no encontrada"));
        mvc.perform(get("/api/v1/sucursales/1")).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
        when(service.create(any())).thenThrow(new DataIntegrityViolationException("detalle SQL privado"));
        mvc.perform(post("/api/v1/sucursales").contentType("application/json")
            .content("{\"nombre\":\"Centro\",\"direccion\":\"Reforma 1\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("La operación entra en conflicto con los datos existentes."));
        doThrow(new AccessDeniedException("detalle privado")).when(service).delete(1);
        mvc.perform(delete("/api/v1/sucursales/1")).andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void userAndAppointmentRulesRejectInvalidData() {
        var validator = factory.getValidator();
        assertFalse(validator.validate(new UsuarioRequest("usuario", "Nombre", "Apellido", null,
            "correo-invalido", List.of(-1), null)).isEmpty());
        var inicio = LocalDateTime.of(2027, 1, 20, 9, 0);
        var valid = new CitaRequest(1, 6, 1, inicio, inicio.plusMinutes(30),
            new BigDecimal("500.00"), new BigDecimal("80.00"), new BigDecimal("580.00"));
        assertTrue(validator.validate(valid).isEmpty());
        var invalid = new CitaRequest(-1, 6, 1, inicio, inicio.minusMinutes(1),
            new BigDecimal("500.001"), new BigDecimal("80"), new BigDecimal("10"));
        var fields = validator.validate(invalid).stream().map(v -> v.getPropertyPath().toString()).toList();
        assertTrue(fields.containsAll(List.of("medicoSucursalId", "horarioValido", "totalValido", "precio")));
        assertFalse(validator.validate(new SucursalRequest("x".repeat(101), "Dirección")).isEmpty());
    }

    @Test
    void appointmentBodyValidationWorksThroughHttp() throws Exception {
        mvc.perform(post("/api/v1/citas").contentType("application/json").content("""
            {"medicoSucursalId":1,"pacienteId":6,"estatusCitaId":1,
             "fechaHoraInicio":"2027-01-20T09:00:00","fechaHoraTermino":"2027-01-20T08:00:00",
             "precio":500,"iva":80,"total":10}
            """))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.length()").value(2));
        verifyNoInteractions(citas);
    }

    @Test
    void openApiDocumentsErrorSchemasAndExamples() {
        var operation = new Operation();
        var api = new OpenAPI().paths(new Paths().addPathItem("/api/v1/citas", new PathItem().post(operation)));
        new ApiErrorDocumentation().errorResponses().customise(api);
        assertNotNull(api.getComponents().getSchemas().get("ApiErrorResponse"));
        assertNotNull(api.getComponents().getSchemas().get("FieldValidationError"));
        for (String code : List.of("400", "401", "403", "404", "409", "500")) {
            var content = operation.getResponses().get(code).getContent().get("application/json");
            assertNotNull(content.getSchema());
            assertNotNull(content.getExample());
        }
    }
}
