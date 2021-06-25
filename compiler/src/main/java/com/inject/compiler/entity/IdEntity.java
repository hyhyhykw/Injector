package com.inject.compiler.entity;

import java.util.Objects;

/**
 * Created time : 2021/6/25 9:54.
 *
 * @author 10585
 */
public class IdEntity {

    public final String id;
    public final boolean isAndroidRes;

    public IdEntity(String id, boolean isAndroidRes) {
        this.id = id;
        this.isAndroidRes = isAndroidRes;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdEntity idEntity = (IdEntity) o;
        return isAndroidRes == idEntity.isAndroidRes &&
                Objects.equals(id, idEntity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, isAndroidRes);
    }
}