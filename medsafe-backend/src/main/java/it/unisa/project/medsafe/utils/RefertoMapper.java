package it.unisa.project.medsafe.utils;

import it.unisa.project.medsafe.dto.RefertoDTO;
import it.unisa.project.medsafe.entity.Referto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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
    public abstract Referto refertoDTOToReferto(RefertoDTO dto);

    /**
     * Aggiorna un'entity Referto esistente con i dati di un RefertoDTO.
     * Ignora id e dataCaricamento per preservare i dati originali.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCaricamento", ignore = true)
    public abstract void updateRefertoFromDTO(RefertoDTO dto, @MappingTarget Referto entity);

    /**
     * Converte una lista di Referto in una lista di RefertoDTO
     */
    public abstract List<RefertoDTO> refertiToRefertiDTO(List<Referto> referti);
}
