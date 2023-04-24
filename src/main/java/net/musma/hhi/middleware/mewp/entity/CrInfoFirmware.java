package net.musma.hhi.middleware.mewp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@Getter
@Entity
@SqlResultSetMapping(
        name = "CrInfoFirmwareMapping",
        entities = @EntityResult(
                entityClass = CrInfoFirmware.class,
                fields={
                        @FieldResult(name = "val", column = "val")
                }
        )
)
@ToString
public class CrInfoFirmware {

    @Id
    private String val;

}

