package com.pikudo.service.archivo;

import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio generico para manejo de archivos (imagenes por ahora).
 * Reutilizable por cualquier modulo que necesite subir una imagen:
 * Producto, Insumo, perfil de Usuario, etc. Evita repetir la logica
 * de guardado/validacion en un controller por cada entidad.
 */
public interface ArchivoService {

    /**
     * Sube un archivo y devuelve la ruta relativa donde quedo guardado
     * (ej. "/uploads/productos/pollo-brasa_a1b2c3.jpg").
     * Genera un nombre unico para evitar colisiones entre archivos
     * con el mismo nombre subidos por distintos usuarios.
     */
    String subir(MultipartFile archivo, String subcarpeta);

    /**
     * Valida que el archivo sea una imagen de un tipo permitido y no supere
     * el tamaño maximo. Lanza BusinessException si no cumple.
     */
    void validarImagen(MultipartFile archivo);

    /**
     * Elimina un archivo previamente guardado, dada su ruta relativa.
     * Util para cuando se reemplaza la imagen de un producto y hay que
     * borrar la anterior, o al eliminar la entidad.
     */
    void eliminar(String rutaRelativa);
}