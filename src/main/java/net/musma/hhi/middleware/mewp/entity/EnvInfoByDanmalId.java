package net.musma.hhi.middleware.mewp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@Getter
@Entity
@SqlResultSetMapping(
        name = "EnvInfoByDanmalIdMapping",
        entities = @EntityResult(
                entityClass = EnvInfoByDanmalId.class,
                fields={
                        @FieldResult(name = "val", column = "val")
                }
        )
)
@ToString
public class EnvInfoByDanmalId {

    @Id
    private String val;

}

