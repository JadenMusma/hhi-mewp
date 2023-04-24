package net.musma.hhi.middleware.mewp.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@SqlResultSetMapping(
        name = "EnvInfoMapping",
        entities = @EntityResult(
                entityClass = EnvInfo.class,
                fields={
                        @FieldResult(name = "danmalid", column = "danmalid"),
                        @FieldResult(name = "ipaddr", column = "ipaddr"),
                        @FieldResult(name = "val", column = "val"),
                        @FieldResult(name = "scVal", column = "sc_val"),
                        @FieldResult(name = "eqpknd", column = "eqpknd"),
                        @FieldResult(name = "lsVal", column = "ls_val"),
                        @FieldResult(name = "lsChk", column = "ls_chk"),
                        @FieldResult(name = "scChk", column = "sc_chk")
                }
        )
)
@ToString
public class EnvInfo {

    @Id
    private String danmalid;

    private String ipaddr;

    private String val;

    private String scVal;

    private String eqpknd;

    private String lsVal;

    private String lsChk;

    @Setter
    private String scChk;

}

