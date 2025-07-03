package ru.otus.inventory.service.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "inventory", schema = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @Column(name = "product_id")
    private Integer productId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int quantity;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int threshold;

    public void decrementQuantity(Integer quantity) {
        if (this.quantity >= quantity) {
            this.quantity = this.quantity - quantity;
        } else {
            this.quantity = 0;
        }
    }
}