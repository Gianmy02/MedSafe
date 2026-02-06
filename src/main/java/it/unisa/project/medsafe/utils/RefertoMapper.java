package it.unisa.project.medsafe.utils;

import it.unisa.project.medsafe.dto.RefertoDTO;
import it.unisa.project.medsafe.entity.Referto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper per la conversione tra Entity Referto e RefertoDTO.
 */
@Mapper(componentModel = "spring")
public abstract class RefertoMapper {

    /**
     * Converte un'entity Referto in un RefertoDTO
     */
    public abstract RefertoDTO refertoToRefertoDTO(Referto entity);

    /**
     * Converte un RefertoDTO in un'entity Referto.
     * Non imposta id e dataCaricamento (gestiti dal database).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCaricamento", ignore = true)
    public abstract Referto refertoDTOToReferto(RefertoDTO dto);

    /**
     * Converte una lista di Referto in una lista di RefertoDTO
     */
    public abstract List<RefertoDTO> refertiToRefertiDTO(List<Referto> referti);
}
