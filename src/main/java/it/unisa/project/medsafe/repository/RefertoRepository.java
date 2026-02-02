package it.unisa.project.medsafe.repository;

import it.unisa.project.medsafe.entinty.Referto;
import it.unisa.project.medsafe.entinty.TipoEsame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefertoRepository extends JpaRepository<Referto, Integer> {
    List<Referto> findByCodiceFiscale(String codiceFiscale);

    List<Referto> findByTipoEsame(TipoEsame tipoEsame);

    Referto findByNomeFile(String nomeFile);

    List<Referto> findByAutoreEmail(String autoreEmail);
}