package cz.mendelu.ipa.domain.platform;

import cz.mendelu.ipa.domain.game.Game;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "platforms")
@Data
@NoArgsConstructor
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String manufacturer;

    @OneToMany(mappedBy = "platform", cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    private Set<Game> games = new HashSet<>();

    public Platform(String name) {
        this.name = name;
    }
}