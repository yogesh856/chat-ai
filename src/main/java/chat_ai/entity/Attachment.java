package chat_ai.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = true)
    private Message message;

    private String fileName;
    private String fileType;   // e.g. "image/png", "application/pdf"
    private String filePath;   // server pe kaha stored hai
    private Long fileSize;
}
