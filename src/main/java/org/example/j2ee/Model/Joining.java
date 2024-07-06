package org.example.j2ee.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name = "joining")
@Data
public class Joining {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
}
