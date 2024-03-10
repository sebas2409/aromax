package org.example.notino;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NotinoPerfume {
    private String brand;
    private String name;
    private String description;
    private String link;
    private List<String> fraganceType;
    private List<String> headNotes;
    private List<String> heartNotes;
    private List<String> baseNotes;
    private String imageSrc;
    private double price;

    public NotinoPerfume(String brand, String name, String description, String link, double price) {
        this.brand = brand;
        this.name = name;
        this.description = description;
        this.link = link;
        this.price = price;
    }
}
