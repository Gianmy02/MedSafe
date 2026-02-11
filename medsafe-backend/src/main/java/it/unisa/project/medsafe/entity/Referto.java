package it.unisa.project.medsafe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Referto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String nomePaziente;

    @Column(nullable = false, length = 16)
    private String codiceFiscale;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoEsame tipoEsame;

    @Column(length = 4000)
    private String testoReferto;

    @Column(length = 4000)
    private String conclusioni;

    @Column(nullable = false, length = 1000)
    private String fileUrlImmagine;

    @Column(nullable = false, length = 1000)
    private String urlPdfGenerato;

    @Column(nullable = false, unique = true)
    private String nomeFile;

    @Column(nullable = false)
    private String autoreEmail;

    @Column(updatable = false)
    private LocalDateTime dataCaricamento;
}
