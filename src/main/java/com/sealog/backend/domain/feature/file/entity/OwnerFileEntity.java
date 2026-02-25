package com.sealog.backend.domain.feature.file.entity;

import com.sealog.backend.domain.base.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class OwnerFileEntity extends BaseTimeEntity {

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "display_order")
    private Integer displayOrder;

}
