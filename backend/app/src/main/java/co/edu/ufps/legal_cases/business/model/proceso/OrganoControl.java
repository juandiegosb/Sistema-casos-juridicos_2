package co.edu.ufps.legal_cases.business.model.proceso;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.List;

@Entity
@Table(name = "organo_control")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganoControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @JsonIgnore
    @OneToMany(mappedBy = "organoControl")
    private List<Especialidad> especialidades;
}