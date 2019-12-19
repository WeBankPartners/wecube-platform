package com.webank.wecube.platform.auth.server.dto;

import java.util.ArrayList;
import java.util.List;

public class ScopedAuthoritiesClaimDto {
    private String name;
    private List<ScopedAuthoritiesDto> scopedAuthoritiesDtos = new ArrayList<ScopedAuthoritiesDto>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ScopedAuthoritiesDto> getScopedAuthoritiesDtos() {
        return scopedAuthoritiesDtos;
    }

    public void setScopedAuthoritiesDtos(List<ScopedAuthoritiesDto> scopedAuthoritiesDtos) {
        this.scopedAuthoritiesDtos = scopedAuthoritiesDtos;
    }

    public void addScopedAuthoritiesDto(ScopedAuthoritiesDto dto) {
        this.scopedAuthoritiesDtos.add(dto);
    }

    @Override
    public String toString() {
        return "ScopedAuthoritiesClaimDto [name=" + name + ", scopedAuthoritiesDtos=" + scopedAuthoritiesDtos + "]";
    }

    
}
