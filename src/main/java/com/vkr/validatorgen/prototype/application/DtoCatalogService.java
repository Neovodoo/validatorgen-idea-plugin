package com.vkr.validatorgen.prototype.application;

import com.vkr.validatorgen.prototype.model.DtoDescriptor;

import java.util.List;

public interface DtoCatalogService {
    List<DtoDescriptor> listAvailableDtos();
}
