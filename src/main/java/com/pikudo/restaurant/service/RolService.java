package com.pikudo.restaurant.service;

import com.pikudo.restaurant.dto.rol.RolResponseDTO;
import java.util.List;

public interface RolService {
    List<RolResponseDTO> listarTodos();
}