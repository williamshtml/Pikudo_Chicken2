package com.pikudo.restaurant.util;

public class JwtUtil {
    /*
     Método estático complementario para extraer limpiamente el token crudo.
     Elimina el prefijo "Bearer " de la cabecera HTTP de manera rápida y segura.
     */
    public static String extraerTokenCrudo(String cabeceraAuthorization) {
        if (cabeceraAuthorization != null && cabeceraAuthorization.startsWith(Constantes.JWT_BEARER_PREFIX)) {
            return cabeceraAuthorization.substring(Constantes.JWT_BEARER_PREFIX.length()).trim();
        }
        return null;
    }

    /*
     Verifica superficialmente si la cabecera HTTP tiene una estructura Bearer válida
     antes de enviarlo al motor criptográfico del JwtService.
     */
    public static boolean tieneFormatoValido(String cabeceraAuthorization) {
        return cabeceraAuthorization != null && cabeceraAuthorization.startsWith(Constantes.JWT_BEARER_PREFIX);
    }

    // Constructor privado para evitar que la clase sea instanciada
    private JwtUtil() {
        throw new IllegalStateException("Clase utilitaria de JWT - No instanciable");
    }
}
