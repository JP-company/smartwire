package jpcompany.smartwire.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @ToString
public class Member {
    private Integer id;
    private String loginId;
    private String loginPassword;
    private String companyName;
    private String email;
    private String phoneNumber;
    private Boolean termOfUse;
    private String authToken;
    private Boolean haveMachine;
    private String role;
    private LocalDateTime createdDateTime;
    private LocalDateTime updatedDateTime;
}
