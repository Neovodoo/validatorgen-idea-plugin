package com.vkr.validatorgen.domain;

import java.nio.file.Path;

public interface GeneratedCodeSaver {
    Path save(DtoSpec dto, String code);
}
