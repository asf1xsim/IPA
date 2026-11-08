package cz.mendelu.ipa.domain.salesdata;

import cz.mendelu.ipa.domain.game.Game;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Entity
@Table(name = "sales_data")
@Data
@NoArgsConstructor
public class SalesData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Game game;

    @DecimalMin("0")
    @Column(name = "na_sales")
    private BigDecimal naSales;

    @DecimalMin("0")
    @Column(name = "eu_sales")
    private BigDecimal euSales;

    @DecimalMin("0")
    @Column(name = "jp_sales")
    private BigDecimal jpSales;
}