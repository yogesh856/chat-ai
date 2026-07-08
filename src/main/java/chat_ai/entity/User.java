package chat_ai.entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "users")
@Data                  // Lombok: getters, setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;   // yaha hamesha HASHED password store hoga

    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;   // e.g. USER, ADMIN
}
