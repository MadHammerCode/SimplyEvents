package at.fhv.simplyevents.domain.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Role {
    private Long id;
    private String name;
    private Set<Long> permissionIds = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(Set<Long> permissionIds) { this.permissionIds = permissionIds; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
