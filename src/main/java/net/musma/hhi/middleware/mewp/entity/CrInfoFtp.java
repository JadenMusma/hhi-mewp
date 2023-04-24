package net.musma.hhi.middleware.mewp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@Getter
@Entity
@SqlResultSetMapping(
        name = "CrInfoFtpMapping",
        entities = @EntityResult(
                entityClass = CrInfoFtp.class,
                fields={
                        @FieldResult(name = "val", column = "val")
                }
        )
)
@ToString
public class CrInfoFtp {

    @Id
    private String val;

}

