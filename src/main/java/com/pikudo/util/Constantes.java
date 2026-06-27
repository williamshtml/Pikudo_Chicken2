package com.pikudo.util;

public class Constantes {
    // Prefijos y Cabeceras de Seguridad
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_BEARER_PREFIX = "Bearer ";

    // Roles del Sistema (Sincronizados con Rol.TipoRol)
    public static final String ROLE_ADMIN = "ROLE_ADMINISTRADOR";
    public static final String ROLE_CAJERO = "ROLE_CAJERO";
    public static final String ROLE_MOZO = "ROLE_MOZO";

    // Mensajes de Error Estándar (Respuestas API)
    public static final String ERROR_RECURSO_NO_ENCONTRADO = "El recurso solicitado no fue encontrado en el sistema.";
    public static final String ERROR_ACCESO_DENEGADO = "No tienes los permisos necesarios para realizar esta acción.";
    public static final String ERROR_TOKEN_INVALIDO = "El token de seguridad proporcionado es inválido o ha expirado.";
    public static final String ERROR_CREDENCIALES_INCORRECTAS = "El usuario o la contraseña introducidos son incorrectos.";
    
    // Mensajes de Validación de Negocio (Módulo Pedidos/Comprobantes)
    public static final String MSG_PEDIDO_MINIMO = "El pedido debe contener al menos un producto de la carta.";
    public static final String MSG_FACTURA_REQUERIDA = "Para generar una FACTURA es obligatorio ingresar el RUC y la Razón Social.";

    // Constructor privado para evitar que la clase sea instanciada
    private Constantes() {
        throw new IllegalStateException("Clase utilitaria de constantes - No instanciable");
    }
}
