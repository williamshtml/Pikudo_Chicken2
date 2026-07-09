package com.pikudo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> manejarResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", HttpStatus.NOT_FOUND.value());
        respuesta.put("error", "Recurso no encontrado");
        respuesta.put("message", ex.getMessage());
        return new ResponseEntity<>(respuesta, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> manejarBusinessException(BusinessException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", HttpStatus.BAD_REQUEST.value());
        respuesta.put("error", "Error de regla de negocio");
        respuesta.put("message", ex.getMessage());
        return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> manejarUnauthorized(UnauthorizedException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", HttpStatus.UNAUTHORIZED.value());
        respuesta.put("error", "No autorizado");
        respuesta.put("message", ex.getMessage());
        return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> manejarBadCredentials(org.springframework.security.authentication.BadCredentialsException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", HttpStatus.UNAUTHORIZED.value());
        respuesta.put("error", "Credenciales incorrectas");
        respuesta.put("message", "El usuario o la contraseña proporcionados son incorrectos.");
        return new ResponseEntity<>(respuesta, HttpStatus.UNAUTHORIZED);
    }

    // NUEVO: captura los errores de @Valid (@NotBlank, @Size, @NotNull, etc. en los DTOs)
    // Devuelve un mapa campo -> mensaje, para que el frontend Angular pueda
    // mostrar el error exacto junto al input correspondiente.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", HttpStatus.BAD_REQUEST.value());
        respuesta.put("error", "Datos inválidos");
        respuesta.put("message", "Uno o más campos no cumplen las validaciones requeridas");
        respuesta.put("errores", errores);
        return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
    }

    // Atrapamos cualquier otro error inesperado del servidor (Ej: errores de SQL)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErroresGlobales(Exception ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        respuesta.put("error", "Error interno del servidor");
        respuesta.put("message", "Ocurrió un error inesperado. Inténtelo más tarde.");
        return new ResponseEntity<>(respuesta, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}